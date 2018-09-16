package edgenodes.model;

import java.util.List;
import java.util.Vector;

public class ThreadsPool {
	private static ThreadsPool instance;

	private List<Thread> sensorCommunicationThreads;
	private List<Thread> nodeCommunicationThreads;
	private Thread consoleThread;
	private Thread globalStatisticThread;
	private boolean isElectionInPending = false;

	public static ThreadsPool getInstance () {
		if (instance == null) {
			instance = new ThreadsPool();
		}
		return instance;
	}

	private ThreadsPool () {
		sensorCommunicationThreads = new Vector<>();
		nodeCommunicationThreads = new Vector<>();
	}

	public synchronized boolean isElectionInPending () {
		return this.isElectionInPending;
	}

	public synchronized void setIsElectionInPending (boolean isElect) {
		System.out.println("Thread " + Thread.currentThread().getId() + "is election in pending: " + isElect);
		this.isElectionInPending = isElect;
	}

	public synchronized void addSensorCommunicationThread (Thread thread) {
		this.sensorCommunicationThreads.add(thread);
	}

	public synchronized void addNodeCommunicationThread (Thread thread) {
		this.nodeCommunicationThreads.add(thread);
	}

	public synchronized List<Thread> getSensorCommunicationThreads () {
		return this.sensorCommunicationThreads;
	}

	public synchronized List<Thread> getNodeCommunicationThread () {
		return this.nodeCommunicationThreads;
	}

	public synchronized Thread getConsoleThread () {
		return this.consoleThread;
	}

	public synchronized void setConsoleThread (Thread newConsole) {
		this.consoleThread = newConsole;
	}

	public synchronized Thread getGlobalStatisticThread () {
		return this.globalStatisticThread;
	}

	public synchronized void setGlobalStatisticThread (Thread newGlobal) {
		this.globalStatisticThread = newGlobal;
	}

}
