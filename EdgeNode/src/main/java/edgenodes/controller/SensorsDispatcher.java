package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.SocketsPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SensorsDispatcher extends Thread {

	private SmartCity.Node node;
	private ServerSocket sensorsServerConnection;

	public SensorsDispatcher (SmartCity.Node node, ServerSocket connection) {
		this.node = node;
		this.sensorsServerConnection = connection;
	}

	@Override
	public void run () {
		while (true) {
			try {
				Socket connectionSocket = sensorsServerConnection.accept();
				SocketsPool.getInstance().addSensorsSocket(connectionSocket);
				SensorCommunicationThread thread = new SensorCommunicationThread(node, connectionSocket);
				thread.start();
			} catch (IOException e) {
				System.out.println("Errore :- errore nella comunicazione con un sensore");
			} catch (Exception e) {
				System.out.println("Errore :- si è verificato un errore generico.");
			}

		}
	}
}
