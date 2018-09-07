package edgenodes;
import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edgenodes.controller.*;
import edgenodes.model.Coordinator;
import edgenodes.model.GlobalStatistic;
import edgenodes.model.MajorNodes;
import edgenodes.model.Semaphore;
import jdk.nashorn.internal.objects.Global;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.util.Vector;

public class NodeMain {
    private final static String CLOUDHOST = "http://localhost";
    private final static int CLOUDPORT=8480;
    private final static String ROOT="/cloud-server/nodes";
    private final static String SELFIP="localhost";

    /**
     * Inizializzazione del nodo: richiede all'utente l'inserimento dell'id del nodo ed estrae casualmente le due
     * coordinate per posizionarlo sulla mappa della città.
     * @param args
     */
    public static void main(String[] args){
        Random rand = new Random();
        int nodeId, nodesPort, sensorsPort, xPos, yPos;
        SmartCity.Node node, father;
        try{

            System.out.println("Inserire id del nodo");
            Scanner scanner = new Scanner(System.in);
            nodeId = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per gli altri nodi edge: ");
            nodesPort = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per i sensori: ");
            sensorsPort = scanner.nextInt();
            xPos = rand.nextInt(100);
            yPos = rand.nextInt(100);
            //Provo a piazzare il nodo nella rete
            node = SmartCity.Node.newBuilder().setId(nodeId).setOtherNodesPort(nodesPort).setSensorsPort(sensorsPort).setSelfIp(SELFIP).setXPos(xPos).setYPos(yPos).build();
            SmartCity.Nodes nodes = initializeNodeServerSide(node);
            //presentarsi agli altri nodi in broadcast
            integrateInto(node,nodes);
            startToWork(node);
        } catch(Exception e){
            System.out.println("Errore inizializzazione dati nodo edge");
            System.exit(0);
        }


    }
    /**
     * Il nodo inizia il suo lavoro ponendosi in ascolto sulla porta per gli altri nodi
     * e sulla porta per gli altri sensori.
     * Il nodesDispatcher è un thread che appena parte rimane in ascolto sulla porta dedicata ai nodi e per ogni
     * request ricevuta inizializza altri thread per la gestione dei compiti richiesti. Stesso lavoro viene fatto
     * dal sensorsDispatcher. Sono stati utilizzati due thread per i dispatcher in modo tale che lavorino
     * in parallelo. Se dovessero esserci problemi di esecuzione (non dovrebbero come da traccia) il nodo dovrebbe
     * stopparsi e rimuoversi dalla mappa.
     * @param node
     */
    public static void startToWork(SmartCity.Node node) throws IOException {

        try{
            ServerSocket selfSocket = new ServerSocket(node.getOtherNodesPort());
            NodesDispatcher nodeDispatcher = new NodesDispatcher(node, selfSocket);
            nodeDispatcher.start();
            ServerSocket selfSensorsSocket = new ServerSocket(node.getSensorsPort());
            SensorsDispatcher sensorsDispatcher = new SensorsDispatcher(node, selfSensorsSocket);
            sensorsDispatcher.start();
            System.out.println("Coordinatore: "+Coordinator.getInstance().getCoordinator());
            if(node.getId()==Coordinator.getInstance().getCoordinator().getId()){
                GlobalStatisticsThread thread = new GlobalStatisticsThread(node);
                thread.start();
                thread.join();
            }
            nodeDispatcher.join();
            sensorsDispatcher.join();

        } catch(Exception e){
            System.out.println("Il nodo edge: "+node.getId()+" non è riuscito a gestire qualche messaggio "+e);
        }
        deleteNodeServerSide(node);


    }

    /**
     * Il metodo consente di connettere questo nodo con tutti gli altri già registrati nella città
     * in modo tale da salvarsi internamente i nodi con ID maggiore al suo e in modo tale da presentarsi
     * agli altri nodi per farsi conoscere e per conoscere il coordinatore.
     * Gli altri nodi lo salveranno nella loro lista se l'id del presente nodo è superiore al loro.
     * Ogni connessione con gli altri nodi è gestita da thread diversi in modo tale da effettuare comunicazioni in parallelo
     * come da traccia del progetto.
     *
     * @param node nodo da inserire
     * @param nodes nodi totali della mappa
     */

    public static void integrateInto(SmartCity.Node node, SmartCity.Nodes nodes) throws IOException {

        if(nodes.getNodesList().isEmpty()){
            Coordinator.getInstance().setCoordinator(node);
            return;
        }
        /*nodes.getNodesList().removeIf(nd -> nd.getId()==node.getId());*/
        List<Socket> socketOpened = new Vector<>();
        List<InitializationThread> threads = new Vector<>();
        for(SmartCity.Node nd: nodes.getNodesList()){
            try{
                Socket ndSocket = new Socket(nd.getSelfIp(),nd.getOtherNodesPort());
                socketOpened.add(ndSocket);
                InitializationThread thread = new InitializationThread(ndSocket,nd, node);
                threads.add(thread);
                thread.start();
            } catch(Exception e){
                System.out.println("Errore creazione connessione con nodo: "+nd.getId());
            }
        }
        try{
            for(Thread thread: threads){
                thread.join();
            }
            for(Socket socket: socketOpened){
                socket.close();
            }
        } catch(InterruptedException e){
            System.out.println("Thread interrupted");
        } catch(IOException e){
            System.out.println("Socket badly closed");
        }


    }

    /**
     * Tenta di connettersi al server cloud e ritenta per un massimo di 10 volte. Se non riesce ad ottenere un OK
     * il processo del nodo deve terminare. Tenta con posizioni diverse. Il server insieme allo stato http restituisce anche
     * un codice più esplicativo per far capire al nodo il motivo per cui non è andata bene l'inizializzazione.
     * Se il motivo è dovuto alle posizioni oppure a un problema generico del server allora il nodo edge ritenterà per un
     * massimo di 10 volte altrimenti termina subito.
     *
     * @param node
     */
    public static SmartCity.Nodes initializeNodeServerSide(SmartCity.Node node) throws IOException {

        Random rand = new Random();
        int retry=0, xPos, yPos;

        Client client = Client.create();
        WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT);
        //Tenta l'inserimento del nodo passato in input
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, node.toByteArray());
        //Tento un massimo di 10 volte l' inserimento del nodo nella mappa se il primo tentativo non è andato a buon fine
        //generando posizioni x,y casuali
        while(retry<10){

            if(response.getStatus()!=ClientResponse.Status.OK.getStatusCode()){
                //Controllo che in realtà sia stato rifiutato per la posizione e non per ID già esistente
                byte[] responseMsgByteArr = response.getEntity(byte[].class);
                SmartCity.InitializationMessage responseMsg = SmartCity.InitializationMessage.parseFrom(responseMsgByteArr);
                if(responseMsg.getErrortype()!=SmartCity.ErrorType.COORD_NOT_ALLOWED){
                    retry=10;
                } else{
                    retry++;
                }
                //Genero le posizioni x,y random comprese tra 0-99 entrambe
                xPos = rand.nextInt(100);
                yPos = rand.nextInt(100);
                node = node.toBuilder().setXPos(xPos).setYPos(yPos).build();
                response = resource.post(ClientResponse.class, node.toByteArray());
            } else{
                break;
            }
        }
        if(retry>=10){
            System.out.println("Non è stato possibile connettersi con il cloud server");
            response.close();
            //TODO dovrei aggiungere delle eccezioni specifiche
            throw new IOException();
        }
        SmartCity.InitializationMessage msgFromCloud = SmartCity.InitializationMessage.parseFrom(response.getEntity(byte[].class));
        SmartCity.Nodes nodes = msgFromCloud.getResponse().getAllNodes();
        SmartCity.Node father = msgFromCloud.getResponse().getFather();
        System.out.println("Mio padre sarà: "+father.getId());
        response.close();
        return nodes;
    }

    public static void sendGlobalStatisticsToServer(){
        //Tenta l'inserimento del nodo passato in input
        if(GlobalStatistic.getInstance().getGlobal()!=null && GlobalStatistic.getInstance().isThereAnyLocal()){
            Client client = Client.create();
            WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT+"/measurements");
            SmartCity.LocalsGlobalsMessage msg = SmartCity.LocalsGlobalsMessage.newBuilder().setGlobal(GlobalStatistic.getInstance().getGlobal()).addAllNodesLocals(GlobalStatistic.getInstance().getNodeslocalsMsg()).build();
            ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, msg.toByteArray());
            response.close();
        }

    }
    /**
     * La rimozione del nodo dalla mappa dovrebbe essere fatta in safe mode.
     * Il nodo deve comunicare la sua chiusura solo al server.
     * @param node
     */
    public static void deleteNodeServerSide(SmartCity.Node node){

        Client client = Client.create();
        WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT+"/"+node.getId());
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).delete(ClientResponse.class);
        response.close();
        System.exit(1);
    }

    /**
     * Implementazione dell'algoritmo Bully di elezione.
     */
    public static void startElection(SmartCity.Node node){
        List<SmartCity.Node> majors = MajorNodes.getInstance().getMajorThanMe();
        SmartCity.MessageRequest msg = SmartCity.MessageRequest.newBuilder().setNode(node).setTypemessage(SmartCity.MessageType.ELECTIONTIME).build();
        Semaphore semaphore = new Semaphore();
        List<ElectionThread> threads = new Vector<>();
        for(SmartCity.Node major: majors){
            ElectionThread thread = new ElectionThread(msg, major, semaphore);
            threads.add(thread);
            thread.start();
        }
        //TODO bisogna controllare se almeno uno risponde entro un tot di secondi, se nessuno lo fa allora questo nodo
       /* try{
            System.out.println("Aspetto un po");
            Thread.sleep(10000);
        } catch(Exception e){
            e.printStackTrace();
        }*/
       if(!threads.isEmpty()) {
           semaphore.startCritical(threads.size());
       }
        if(threads.isEmpty() || threads.stream().allMatch(thr -> thr.isAlive())){
            //Vuol dire che è scattato il timeout e ha vinto questo thread
            System.out.println("Ho vinto io "+node.getId());
            //TODO si deve autoproclamare coord e broadcastarlo a tutti
            Coordinator.getInstance().setCoordinator(node);
            broadcastElectionResult(node);
        }else{
            System.out.println("Ha vinto qualche altro nodo che ha risposto");
        }

    }

    public static void broadcastElectionResult(SmartCity.Node newCoordinator){
        System.out.println("BEGIN BROADCAST");
        Client client = Client.create();
        WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT);
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
        byte[] cityNodes = response.getEntity(byte[].class);
        try{
            SmartCity.Nodes nodes = SmartCity.Nodes.parseFrom(cityNodes);
            SmartCity.MessageRequest msg = SmartCity.MessageRequest.newBuilder().setTypemessage(SmartCity.MessageType.ELECTIONRESULT).setNode(newCoordinator).build();
            for(SmartCity.Node node: nodes.getNodesList()){
                if(node.getId()!=newCoordinator.getId()){
                    ElectionBroadcastResultThread thread = new ElectionBroadcastResultThread(msg, newCoordinator,node);
                    thread.start();
                }
            }
        } catch(Exception e){
            System.out.println("Errore nella ricezione dei nodi rimanenti nella mappa");
        }
        response.close();
    }
}
