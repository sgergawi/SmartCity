package edgenodes.model;

import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class SocketsPool {
	private static SocketsPool instance;
	private List<Socket> otherNodesSockets;
	private List<Socket> otherSensorsSockets;
	private List<Socket> electionSockets;

	public static synchronized SocketsPool getInstance () {
		if (instance == null) {
			instance = new SocketsPool();
		}
		return instance;
	}

	private SocketsPool () {
		this.otherNodesSockets = new Vector<>();
		this.otherSensorsSockets = new Vector<>();
		this.electionSockets = new Vector<>();
	}

	public synchronized void addNodesSocket (Socket s) {
		this.otherNodesSockets.add(s);
	}

	public synchronized void addSensorsSocket (Socket s) {
		this.otherSensorsSockets.add(s);
	}

	public synchronized void addElectionSockets (Socket s) {
		this.electionSockets.add(s);
	}

	public synchronized List<Socket> getElectionSockets () {
		return this.electionSockets;
	}

	public synchronized List<Socket> getSensorsSockets () {
		return this.otherSensorsSockets;
	}

	public synchronized List<Socket> getOtherNodesSockets () {
		return this.otherNodesSockets;
	}
}
