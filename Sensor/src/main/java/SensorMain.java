import lib.PM10Simulator;
import sensors.stream.ConcreteSimulatorStream;

import java.util.Scanner;

public class SensorMain {
    final static String HOST = "localhost";
    final static int PORT = 8480;

    public static void main(String[] args){
        System.out.println("Inserire il numero di sensori richiesti");
        Scanner scanner = new Scanner(System.in);
        int sensorsnum = scanner.nextInt();
        ConcreteSimulatorStream concrete = new ConcreteSimulatorStream(HOST, PORT);

       PM10Simulator simulator= new PM10Simulator( "1",concrete);
       simulator.run();

    }
}
