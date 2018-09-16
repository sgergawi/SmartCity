package edgenodes.model;

import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class SocketsPool {
	private static SocketsPool instance;
	private List<Socket> otherNodesSockets;
	private List<Socket> otherSensorsSockets;

	public static synchronized SocketsPool getInstance () {
		if (instance == null) {
			instance = new SocketsPool();
		}
		return instance;
	}

	private SocketsPool () {
		this.otherNodesSockets = new Vector<>();
		this.otherSensorsSockets = new Vector<>();
	}

	public synchronized void addNodesSocket (Socket s) {
		this.otherNodesSockets.add(s);
	}

	public synchronized void addSensorsSocket (Socket s) {
		this.otherSensorsSockets.add(s);
	}
	
	public synchronized List<Socket> getSensorsSockets () {
		return this.otherSensorsSockets;
	}

	public synchronized List<Socket> getOtherNodesSockets () {
		return this.otherNodesSockets;
	}
}
