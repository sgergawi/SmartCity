package analista.controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.Scanner;
public class AnalistaController {
    private final String CLOUDHOST = "http://localhost";
    private final int CLOUDPORT = 8480;
    private final String ROOT="/cloud-server";
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
        WebResource webResource = client.resource(CLOUDHOST+":"+CLOUDPORT+"/"+ROOT+"/nodes");
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if(response.getStatus()==200){
            String output=response.getEntity(String.class);
            System.out.println("Stato attuale città: "+output);
        } else if(response.getStatus()==404){
            System.out.println("Dati non trovati");
        } else{
            System.out.println("Si è verificato un errore.");
        }
    }

    public void getEdgeNodeStats(){

        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);
            System.out.print("Inserire l'id del nodo edge: ");
            String edgeId = scanner.next();

            Client client = Client.create();
            WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+"/"+ROOT+"/"+edgeId+"/measurements");
            ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if(ifOKResponse(response)){
                String output = response.getEntity(String.class);
                System.out.println("Statistiche del nodo "+edgeId+": "+output);
            }
        } catch(Exception io){
            System.out.println("Errore nella ricezione dei dati in input");
        }

    }

    public void getGlobalAndLocalStats(){
        try{
            Scanner scanner = new Scanner(System.in);
            int statsNumber = getStatsNumber(scanner);
            Client client = Client.create();
            WebResource resource = client.resource(CLOUDHOST+":"+CLOUDPORT+"/"+ROOT+"/measurements");
            ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
            if(ifOKResponse(response)){
                String output = response.getEntity(String.class);
                System.out.println("Statistiche globali e locali: "+output);
            }
        } catch(Exception e){
            System.out.println("Errore nella ricezione dei dati in input");
        }
    }

    private boolean ifOKResponse(ClientResponse response) {
        if(response.getStatus()==200){
            return false;
        } else if(response.getStatus()==404){
            System.out.println("Dati non trovati");
        } else{
            System.out.println("Si è verificato un errore.");
        }
        return false;
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
