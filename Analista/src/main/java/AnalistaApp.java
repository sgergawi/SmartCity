import analista.controller.AnalistaController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AnalistaApp {
    public static void main(String[] args){
        boolean anotherChoice= true;
        while(anotherChoice){

            System.out.println("Scegliere una delle seguenti opzioni: ");
            System.out.println("1. Conoscere lo stato attuale della città");
            System.out.println("2. Conoscere le ultime N statistiche (con timestamp) da uno specifico nodo");
            System.out.println("3. Conoscere le ultime N statistiche (con timestamp) globali e locali della città");
            System.out.println("4. Deviazione std e media delle ultime N statistiche prodotte da uno specifico nodo");
            System.out.println("5. Deviazione std e media delle ultime N statistiche globali della città");
            System.out.print("Opzione scelta: ");
            try {
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
                String option = bufReader.readLine();
                if(option!=null && option.trim().equalsIgnoreCase("Q")){
                    System.out.println("L'applicativo sta per terminare ...");
                    anotherChoice = false;
                } else{
                    Integer choice = Integer.valueOf(option);
                    AnalistaController functs = new AnalistaController();

                    switch (choice){
                        case 1:
                            functs.getCityState();
                            break;
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        default:
                            System.out.println("Input non riconosciuto");
                            break;
                    }
                }
                System.out.println("--------------------------------");
                System.out.println("\n\n");


            } catch (IOException io) {
                System.out.println("Errore nella ricezione dell'input.");
            } catch (Exception e) {
                System.out.println("Si è verificato un errore.");
            }
        }

    }
}
