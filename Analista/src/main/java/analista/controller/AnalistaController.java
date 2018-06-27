package analista.controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.Scanner;
public class AnalistaController {

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
        MultivaluedMap<String,String> params = new MultivaluedMapImpl();
        ((MultivaluedMapImpl) params).add("xcoord","50");
        ((MultivaluedMapImpl) params).add("ycoord","50");

        WebResource webResource = client.resource("http://localhost:8480/cloud-server/nodes").queryParams(params);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
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
