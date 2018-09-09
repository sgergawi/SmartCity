import lib.PM10Simulator;
import sensors.stream.ConcreteSimulatorStream;
import sensors.stream.ServerCommunication;

import java.util.Scanner;

public class SensorMain {
    final static String HOST = "localhost";
    final static int PORT = 8480;

    /**
     * Inizializzazione del sensore, richiedendo in input l'identificativo.
     * @param args
     */
    public static void main(String[] args){
        System.out.println("Inserire il numero di sensori richiesti");
        Scanner scanner = new Scanner(System.in);
        int sensorsnum = scanner.nextInt();
        ConcreteSimulatorStream concrete = new ConcreteSimulatorStream(HOST, PORT);
        PM10Simulator simulator= new PM10Simulator( "1",concrete);
        simulator.start();
        ServerCommunication closerNodeUpdateThread = new ServerCommunication(concrete);
        closerNodeUpdateThread.start();
    }

}
