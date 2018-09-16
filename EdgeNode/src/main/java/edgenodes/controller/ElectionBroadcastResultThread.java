package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.CityNodes;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ElectionBroadcastResultThread extends Thread {
	private SmartCity.Node newCoordinator, nodeToBeCalled;
	private SmartCity.MessageRequest msg;

	public ElectionBroadcastResultThread (SmartCity.MessageRequest msg, SmartCity.Node newCoordinator, SmartCity.Node nodeToBeCalled) {
		this.msg = msg;
		this.newCoordinator = newCoordinator;
		this.nodeToBeCalled = nodeToBeCalled;
	}

	@Override
	public void run () {
		try {
			//System.out.println("Aggiornamento coordinatore mandato al nodo " + nodeToBeCalled.getId() + " msg: " + msg);
			Socket socket = new Socket(nodeToBeCalled.getSelfIp(), nodeToBeCalled.getOtherNodesPort());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
			outputStream.writeInt(msg.toByteArray().length);
			outputStream.write(msg.toByteArray());
			socket.close();
			System.out.println("Thread " + Thread.currentThread().getId() + " aggiornamento coordinatore mandato a " + nodeToBeCalled.getId());
		} catch (IOException e) {
			CityNodes.getInstance().removeCityNode(nodeToBeCalled);
			System.out.println("Errore :- si è verificato un errore durante la comunicazione del nuovo coordinatore al nodo " + this.nodeToBeCalled.getId());
		} catch (Exception e) {
			CityNodes.getInstance().removeCityNode(nodeToBeCalled);
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione del coordinatore al nodo " + this.nodeToBeCalled.getId());
		}
	}
}
