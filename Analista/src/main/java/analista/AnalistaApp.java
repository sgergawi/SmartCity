package analista;

import analista.controller.AnalistaController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AnalistaApp {
	public static void main (String[] args) {
		while (true) {

			System.out.println("Scegliere una delle seguenti opzioni: ");
			System.out.println("1. Conoscere lo stato attuale della città");
			System.out.println("2. Conoscere le ultime N statistiche (con timestamp) da uno specifico nodo");
			System.out.println("3. Conoscere le ultime N statistiche (con timestamp) globali e locali della città");
			System.out.println("4. Deviazione std e media delle ultime N statistiche prodotte da uno specifico nodo");
			System.out.println("5. Deviazione std e media delle ultime N statistiche globali della città");
			System.out.println("6. Exit");
			System.out.print("Opzione scelta: ");
			try {
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
				String option = bufReader.readLine();
				Integer choice = Integer.valueOf(option);
				AnalistaController functs = new AnalistaController();

				switch (choice) {
					case 1:
						functs.getCityState();
						break;
					case 2:
						functs.getEdgeNodeStats();
						break;
					case 3:
						functs.getGlobalAndLocalStats();
						break;
					case 4:
						functs.getStdDevMeanSingleNode();
						break;
					case 5:
						functs.getStdDevMeanNodes();
						break;
					case 6:
						System.exit(0);
					default:
						System.out.println("Errore :- Input non riconosciuto");
						break;
				}

				System.out.println("--------------------------------");
				System.out.println("\n\n");
			} catch (IOException io) {
				System.out.println("Errore :- si è verificato un problema nella ricezione dell'input");
			} catch (Exception e) {
				System.out.println("Errore :- si è verificato un errore inaspettato");
			}
		}

	}
}
