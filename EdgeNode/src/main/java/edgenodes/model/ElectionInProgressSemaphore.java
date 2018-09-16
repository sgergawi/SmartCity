package edgenodes.model;

import java.util.List;
import java.util.Vector;

public class ElectionInProgressSemaphore {

	private static ElectionInProgressSemaphore instance;

	//private List<Thread> inpending;
	public static synchronized ElectionInProgressSemaphore getInstance () {
		if (instance == null) {
			instance = new ElectionInProgressSemaphore();
		}
		return instance;
	}

	private ElectionInProgressSemaphore () {
		//this.inpending = new Vector<>();
	}

	public synchronized void blockMeIfElectionInProgress () {
		while (ThreadsPool.getInstance().isElectionInPending()) {
			try {
				System.out.println("Thread " + Thread.currentThread().getId() + " è in pending");
				//this.inpending.add(Thread.currentThread());
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void exit () {
		/*this.inpending.forEach(t -> {
			try {
				t.notify();
			} catch (IllegalMonitorStateException e) {

			}
		});*/
		this.notify();
	}
}
