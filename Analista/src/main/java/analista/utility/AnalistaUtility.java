package analista.utility;

import com.sun.jersey.api.client.ClientResponse;

import java.util.Scanner;

public class AnalistaUtility {
    public static int getStatsNumber(Scanner scanner) throws Exception {
        System.out.print("Inserire il numero di statistiche desiderate: ");
        try{
            int statsNumber = scanner.nextInt();
            return statsNumber;
        } catch(Exception e) {
           throw e;
        }
    }

    public static boolean ifOKResponse(ClientResponse response) {
        if(response.getStatus()== ClientResponse.Status.OK.getStatusCode()){
            return true;
        } else if(response.getStatus()==ClientResponse.Status.NOT_FOUND.getStatusCode()){
            System.out.println("Dati non trovati");
        } else{
            System.out.println("Si è verificato un errore.");
        }
        return false;
    }
}
