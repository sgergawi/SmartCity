package edgenodes.model;

import java.util.List;
import java.util.Vector;

public class PendingThreadsSemaphore {

	private static PendingThreadsSemaphore instance;
	private List<Thread> inpending;

	public static synchronized PendingThreadsSemaphore getInstance () {
		if (instance == null) {
			instance = new PendingThreadsSemaphore();
		}
		return instance;
	}

	private PendingThreadsSemaphore () {
		this.inpending = new Vector<>();
	}

	public synchronized void blockMeIfElection () {
		while (ThreadsPool.getInstance().isElectionInPending()) {
			try {
				System.out.println("Thread " + Thread.currentThread().getId() + " è in pending");
				this.inpending.add(Thread.currentThread());
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void exit () {
		this.inpending.forEach(t -> {
			try {
				t.notify();
			} catch (IllegalMonitorStateException e) {

			}
		});
		System.out.println("Risveglio tutti");
	}
}
