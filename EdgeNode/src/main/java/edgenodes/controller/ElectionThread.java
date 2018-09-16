package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.CityNodes;
import edgenodes.model.ElectionSemaphore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ElectionThread extends Thread {
	private SmartCity.Node majorNode;
	private SmartCity.MessageRequest msg;
	private ElectionSemaphore semaphore;

	public ElectionThread (SmartCity.MessageRequest msg, SmartCity.Node major, ElectionSemaphore semaphore) {
		this.msg = msg;
		this.majorNode = major;
		this.semaphore = semaphore;
	}

	@Override
	public void run () {
		try {
			Socket nodeSocket = new Socket(this.majorNode.getSelfIp(), this.majorNode.getOtherNodesPort());
			DataOutputStream outputStream = new DataOutputStream(nodeSocket.getOutputStream());
			outputStream.writeInt(msg.toByteArray().length);
			outputStream.write(msg.toByteArray());
			System.out.println("Aspetto l'ok dal nodo " + this.majorNode.getId());

			DataInputStream inputStream = new DataInputStream(nodeSocket.getInputStream());
			byte[] response = new byte[inputStream.readInt()];
			inputStream.read(response);
			System.out.println("OK ricevuto da: " + SmartCity.MessageRequest.parseFrom(response).getNode().getId());
			this.semaphore.exit();
			nodeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			CityNodes.getInstance().removeCityNode(this.majorNode);
			System.out.println("Errore :- non è stato possibile comunicare con il nodo " + this.majorNode.getId());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Errore :- si è verificato un errore generico durante la comunicazione con il nodo " + this.majorNode.getId() + " in fase di elezione.");
			CityNodes.getInstance().removeCityNode(this.majorNode);
		}

	}
}
