import java.util.Scanner;

public class NodeMain {
    private final String CLOUDHOST = "http://localhost";
    private final int CLOUDPORT = 8480;
    private final String ROOT="/cloud-server";
    public static void main(String[] args){

        try{
            System.out.println("Inserire id del nodo");
            Scanner scanner = new Scanner(System.in);
            int nodeId = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per gli altri nodi edge: ");
            int nodesPort = scanner.nextInt();
            System.out.println("Inserire porta di ascolto per i sensori: ");
            int sensorsPort = scanner.nextInt();

        } catch(Exception e){
            System.out.println("Errore inizializzazione nodo edge");
        }

    }
}
