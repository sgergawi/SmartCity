package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.SocketsPool;
import edgenodes.model.ThreadsPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodesDispatcher extends Thread {
	private ServerSocket nodesServerSocket;
	private SmartCity.Node node;

	public NodesDispatcher (SmartCity.Node node, ServerSocket connection) {
		this.node = node;
		this.nodesServerSocket = connection;
	}

	@Override
	public void run () {
		while (true) {
			try {
				Socket connectionSocket = nodesServerSocket.accept();
				SocketsPool.getInstance().addNodesSocket(connectionSocket);
				NodeCommunicationThread thread = new NodeCommunicationThread(connectionSocket, node);
				thread.start();
			} catch (IOException e) {
				System.out.println("Errore :- si è verificato un errore durante la comunicazione con il nodo " + node.getId());
			} catch (Exception e) {
				System.out.println("Errore :- si è verificato un errore generico.");
			}

		}
	}
}
