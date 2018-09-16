package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.Coordinator;
import edgenodes.model.CityNodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class InitializationThread extends Thread {
	private Socket connectionSocket = null;
	private SmartCity.Node called = null;
	private SmartCity.Node caller = null;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	public InitializationThread (Socket s, SmartCity.Node nodeCalled, SmartCity.Node nodeCaller) {
		this.connectionSocket = s;
		this.called = nodeCalled;
		this.caller = nodeCaller;
		try {
			inputStream = new DataInputStream(this.connectionSocket.getInputStream());
			outputStream = new DataOutputStream(this.connectionSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Non è stato possibile aprire la connessione con un nodo");
		}
	}

	public void run () {
		try {
			SmartCity.MessageRequest req = SmartCity.MessageRequest.newBuilder().setNode(this.caller).setTypemessage(SmartCity.MessageType.HELLO).build();
			outputStream.writeInt(req.toByteArray().length);
			outputStream.write(req.toByteArray());
			byte[] fromOtherNode = new byte[inputStream.readInt()];
			inputStream.read(fromOtherNode);
			SmartCity.HelloResponse resp = SmartCity.HelloResponse.parseFrom(fromOtherNode);
			if (resp.getIscoordinator()) {
				Coordinator.getInstance().setCoordinator(called);
			}
			connectionSocket.close();
		} catch (IOException e) {
			System.out.println("Errore :- si è verificato un problema con la connessione con il nodo " + called.getId());
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico durante la presentazione del nodo");
		}

	}

}
