package analista.functionalities;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.Scanner;
public class FunctController {

    public int getStatsNumber(Scanner scanner) throws Exception {
        System.out.print("Inserire il numero di statistiche desiderate: ");
        try{
            int statsNumber = scanner.nextInt();
            return statsNumber;
        } catch(Exception e) {
           throw e;
        }
    }
    public void getCityState(){
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/cloud-server/nodes");
        ClientResponse response = webResource.accept(MediaType.TEXT_XML).get(ClientResponse.class);
        String output=response.getEntity(String.class);
        System.out.println("Ho ricevuto: "+output);
    }

    public void getEdgeNodeStats(){

        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);
            System.out.print("Inserire l'id del nodo edge: ");
            String edgeId = scanner.next();
        } catch(Exception io){
            System.out.println("Errore nella ricezione dei dati in input");
        }

    }

    public void getGlobalAndLocalStats(){
        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);

        } catch(Exception e){
            System.out.println("Errore nella ricezione dei dati in input");
        }
    }

    public void getStdDevMeanSingleNode(){
        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);
            System.out.print("Inserire l'ID del nodo edge interessato: ");
            String nodeId = scanner.next();
        } catch( Exception io){
            System.out.println("Errore nella ricezione dei dati in input");
        }
    }
    public void getStdDevMeanNodes(){
        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);
        } catch( Exception io){
            System.out.println("Errore nella ricezione dei dati in input");
        }
    }
}
