import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edgenodes.controller.*;
import edgenodes.model.Coordinator;

import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Random;

public class NodeMain {
    private final static String CLOUDHOST = "http://localhost";
    private final static int CLOUDPORT=8480;
    private final static String ROOT="/cloud-server/nodes";
    private final static String SELFIP="localhost";

    public static void main(String[] args){
        Random rand = new Random();
        int nodeId, nodesPort, sensorsPort, xPos, yPos;
        SmartCity.Node node;
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
    public static void startToWork(SmartCity.Node node) throws IOException {
        try{
            ServerSocket selfSocket = new ServerSocket(node.getOtherNodesPort());
            NodesDispatcher nodeDispatcher = new NodesDispatcher(node, selfSocket);
            nodeDispatcher.start();
            System.out.println("Sto per aprire la socket per i sensori");
            ServerSocket selfSensorsSocket = new ServerSocket(node.getSensorsPort());
            SensorsDispatcher sensorsDispatcher = new SensorsDispatcher(node, selfSensorsSocket);
            sensorsDispatcher.start();
        } catch(Exception e){
            System.out.println("Il nodo edge: "+node.getId()+" non è riuscito a gestire qualche messaggio");
            deleteNodeServerSide(node);
        }
    }
    public static void integrateInto(SmartCity.Node node, SmartCity.Nodes nodes) throws IOException {
        /**
         Il metodo consente di connettere questo nodo con tutti gli altri già registrati nella città
         in modo tale da salvarsi internamente i nodi con ID maggiore al suo e in modo tale da presentarsi
         agli altri nodi per farsi conoscere e per conoscere il coordinatore.
         Gli altri nodi lo salveranno nella loro lista se l'id del presente nodo è superiore al loro.
         Ogni connessione con gli altri nodi è gestita da thread diversi in modo tale da effettuare comunicazioni in parallelo
         come da traccia del progetto.
         */
        if(nodes.getNodesList().isEmpty()){
            Coordinator.getInstance().setCoordinator(node);
            return;
        }
        for(SmartCity.Node nd: nodes.getNodesList()){
            try{
                Socket ndSocket = new Socket(nd.getSelfIp(),nd.getOtherNodesPort());
                InitializationThread thread = new InitializationThread(ndSocket,nd, node);
                thread.start();
            } catch(Exception e){
                System.out.println("Errore creazione connessione con nodo: "+nd.getId());
            }
        }
    }

    public static SmartCity.Nodes initializeNodeServerSide(SmartCity.Node node) throws IOException {
        /**
         * Tenta di connettersi al server cloud e ritenta per un massimo di 10 volte. Se non riesce ad ottenere un OK
         * il processo del nodo deve terminare. Tenta con posizioni diverse. Il server insieme allo stato http restituisce anche
         * un codice più esplicativo per far capire al nodo il motivo per cui non è andata bene l'inizializzazione.
         * Se il motivo è dovuto alle posizioni oppure a un problema generico del server allora il nodo edge ritenterà per un
         * massimo di 10 volte altrimenti termina subito.
         */
        Random rand = new Random();
        int retry=0, xPos, yPos;

        Client client = Client.create();
        WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT);
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, node.toByteArray());
        while(retry<10){
            if(response.getStatus()!=ClientResponse.Status.OK.getStatusCode()){
                //Genero le posizioni x,y random comprese tra 0-99 entrambe
                //Controllo che in realtà sia stato rifiutato per la posizione e non per ID già esistente
                byte[] responseMsgByteArr = response.getEntity(byte[].class);
                SmartCity.InitializationMassage responseMsg = SmartCity.InitializationMassage.parseFrom(responseMsgByteArr);
                if(responseMsg.getErrortype()!=SmartCity.ErrorType.COORD_NOT_ALLOWED){
                    retry=10;
                } else{
                    retry++;
                }
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
        SmartCity.Nodes nodes = SmartCity.InitializationMassage.parseFrom(response.getEntity(byte[].class)).getNodes();
        response.close();
        return nodes;
    }

    public static void deleteNodeServerSide(SmartCity.Node node){
        Client client = Client.create();
        WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT+"/"+node.getId());
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).delete(ClientResponse.class);
        response.close();
    }
}
