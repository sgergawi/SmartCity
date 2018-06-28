import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.print.attribute.standard.Media;
import javax.ws.rs.core.MediaType;
import java.util.Scanner;
import java.util.Random;

public class NodeMain {
    private final static String CLOUDHOST = "http://localhost";
    private final static int CLOUDPORT=8480;
    private final static String ROOT="/cloud-server/nodes";
    private final static String SELFIP="http://localhost:8080";
    public static void main(String[] args){
        Random rand = new Random();
        int retry=0;
        try{
            System.out.println("Inserire id del nodo");
            Scanner scanner = new Scanner(System.in);
            int nodeId = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per gli altri nodi edge: ");
            int nodesPort = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per i sensori: ");
            int sensorsPort = scanner.nextInt();
            //Genero le posizioni x,y random comprese tra 0-99 entrambe
            int xPos = rand.nextInt(100);
            int yPos = rand.nextInt(100);

            //Provo a piazzare il nodo nella rete
            SmartCity.Node node = SmartCity.Node.newBuilder().setId( nodeId).setOtherNodesPort(nodesPort).setSensorsPort(sensorsPort).setSelfIp(SELFIP).build();
            Client client = Client.create();
            WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+ROOT);
            ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).post(ClientResponse.class, node.toByteArray());
            while(retry<10){
                if(response.getStatus()!=ClientResponse.Status.OK.getStatusCode()){
                    retry++;
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
                return;
            }
            //dovrei proseguire continuando a rimanere in ascolto per misurazioni
            SmartCity.Nodes nodes = SmartCity.Nodes.parseFrom(response.getEntity(byte[].class));
        } catch(Exception e){
            System.out.println(e);
            System.out.println("Errore inizializzazione nodo edge");
        }

    }
}
