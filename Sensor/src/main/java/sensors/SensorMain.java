package sensors;

import lib.PM10Simulator;
import sensors.stream.ConcreteSimulatorStream;
import sensors.stream.ServerCommunication;

import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class SensorMain {
	final static String HOST = "localhost";
	final static int PORT = 8480;

	/**
	 * Inizializzazione del sensore, richiedendo in input l'identificativo.
	 *
	 * @param args
	 */
	public static void main (String[] args) {
		System.out.println("Inserire il numero di sensori richiesti");
		Scanner scanner = new Scanner(System.in);
		int sensorsnum = scanner.nextInt();
		List<PM10Simulator> simulatorList = new Vector<>();
		for (int i = 0; i < sensorsnum; i++) {
			ConcreteSimulatorStream concrete = new ConcreteSimulatorStream(HOST, PORT);
			PM10Simulator simulator = new PM10Simulator(String.valueOf(i), concrete);
			simulator.start();
			simulatorList.add(simulator);
			ServerCommunication closerNodeUpdateThread = new ServerCommunication(concrete);
			closerNodeUpdateThread.start();
		}

		/*PM10Simulator simulator = new PM10Simulator("1", concrete);
		simulator.start();*/
	}

}
