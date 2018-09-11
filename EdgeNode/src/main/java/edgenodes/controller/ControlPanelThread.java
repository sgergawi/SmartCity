package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.model.GlobalStatistic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ControlPanelThread extends Thread {
	private SmartCity.Node node;

	public ControlPanelThread (SmartCity.Node node) {
		this.node = node;
	}

	@Override
	public void run () {
		boolean anotherChoice = true;
		while (anotherChoice) {

			System.out.println("1. Rimuovi nodo");
			System.out.println("2. Visualizza statistiche globali ricevute dal coordinatore");
			System.out.println("3. Visualizza statistiche locali calcolate dal nodo");
			System.out.println("4. Exit");
			System.out.print("Opzione scelta: ");
			try {
				BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
				String option = bufReader.readLine();
				if (option != null && option.trim().equalsIgnoreCase("Q")) {
					System.out.println("L'applicativo sta per terminare ...");
					anotherChoice = false;
				} else {
					Integer choice = Integer.valueOf(option);
					switch (choice) {
						case 1:
							NodeMain.deleteNodeServerSide(this.node);
							break;
						case 2:
							System.out.println(GlobalStatistic.getInstance().getGlobalsReceived());
							break;
						case 3:
							//Restituire info locali;
							break;
						case 4:
							anotherChoice = false;
							break;
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
