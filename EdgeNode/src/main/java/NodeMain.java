import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.print.attribute.standard.Media;
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
    private static SmartCity.Node coordinator=null;
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
            SmartCity.Nodes nodes = intializeNodeServerSide(node);
            //presentarsi agli altri nodi in broadcast
            integrateInto(node,nodes);
            //TODO se l'integrazione non è andata bene deve prima eliminarsi lato server e poi chiudersi.
            ServerSocket selfSocket = new ServerSocket(node.getOtherNodesPort());
            while(true){
                Socket connectionSocket = selfSocket.accept();
                DataInputStream inputStream = new DataInputStream(connectionSocket.getInputStream());
                byte[] message = new byte[inputStream.readInt()];
                inputStream.read(message,0,message.length);
                DataOutputStream outputStream = new DataOutputStream(connectionSocket.getOutputStream());
                SmartCity.HelloMessage request = SmartCity.HelloMessage.parseFrom(message);
                SmartCity.HelloMessage response = SmartCity.HelloMessage.newBuilder().setTypemessage(SmartCity.MessageType.WELCOME).build();
                if(coordinator.getId()==node.getId()){
                    response = response.toBuilder().setNode(coordinator).build();
                }
                byte[] output = response.toByteArray();
                outputStream.writeInt(output.length);
                outputStream.write(output);

                System.out.println();
            }
            //tutti i nodi quando ricevono un avviso di ingresso, salvano il nodo nella loro lista di nodi attivi se è maggiore del loro ID
            // Se il nodo coordinatore ha ricevuto l'avviso di ingresso, deve rispondere anche con il suo ID
        } catch(Exception e){
            System.out.println("Errore inzializzazione dati nodo edge");
            System.exit(0);
        }


    }

    public static void integrateInto(SmartCity.Node node, SmartCity.Nodes nodes) throws IOException {
        try{
            if(nodes.getNodesList().isEmpty()){
                NodeMain.coordinator=node;
                return;
            }
            for(SmartCity.Node nd: nodes.getNodesList()){
                //devo mandare a tutti un hello
                //TODO gestire i casi in cui un nodo non è raggiungibile
                //TODO questo nodo non deve far crashare tutto
                Socket ndSocket = new Socket(nd.getSelfIp(),nd.getOtherNodesPort());
                DataOutputStream outToNodes = new DataOutputStream(ndSocket.getOutputStream());
                DataInputStream inFromOtherNodes = new DataInputStream(ndSocket.getInputStream());
                SmartCity.HelloMessage req = SmartCity.HelloMessage.newBuilder().setNode(node).setTypemessage(SmartCity.MessageType.HELLO).build();
                outToNodes.writeInt(req.toByteArray().length);
                outToNodes.write(req.toByteArray());
                byte[] fromOtherNode = new byte[inFromOtherNodes.readInt()];
                inFromOtherNodes.read(fromOtherNode);
                SmartCity.HelloMessage resp = SmartCity.HelloMessage.parseFrom(fromOtherNode);
                if(resp.getNode()!=null){
                    NodeMain.coordinator=resp.getNode();
                }
                ndSocket.close();
            }
            System.out.println("Il coordinatore è: "+coordinator);

        } catch(Exception e){
            System.out.println("Errore creazione connessione");

            throw e;
        }

    }
    public static SmartCity.Nodes intializeNodeServerSide(SmartCity.Node node){
        Random rand = new Random();
        int retry=0, xPos, yPos;
        try{
            Client client = Client.create();
            WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT);
            ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, node.toByteArray());
            //Genero le posizioni x,y random comprese tra 0-99 entrambe

            while(retry<10){
                if(response.getStatus()!=ClientResponse.Status.OK.getStatusCode()){
                    retry++;
                    //TODO dovrei controllare che in realtà sia stato rifiutato per la posizione e non per ID già esistente
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
                return null;
            }
            //dovrei proseguire continuando a rimanere in ascolto per misurazioni
            SmartCity.Nodes nodes = SmartCity.Nodes.parseFrom(response.getEntity(byte[].class));
            return nodes;
        } catch(Exception e){
            System.out.println("Errore inserimento nodo nella mappa");
            return null;
        }
    }
}
