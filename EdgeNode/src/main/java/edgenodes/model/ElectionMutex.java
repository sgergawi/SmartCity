package edgenodes.model;

public class ElectionInProgressSemaphore {
	private boolean token;
	private long threadid;
	private static ElectionInProgressSemaphore electionInProgress;

	public synchronized static ElectionInProgressSemaphore getInstance () {
		if (electionInProgress == null) {
			electionInProgress = new ElectionInProgressSemaphore();
		}
		return electionInProgress;
	}

	private ElectionInProgressSemaphore () {
		token = false;
	}

	public void enter () {
		synchronized (ElectionLock.getInstance()) {
			if (token && threadid != Thread.currentThread().getId()) {
				try {
					System.out.println("entro nella critica");
					ElectionLock.getInstance().wait();
					System.out.println("Esco dalla critica");
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println("Errore :- si è verificato un errore durante la wait sul semaforo");
				}
			}
			token = true;
			threadid = Thread.currentThread().getId();
		}
	}

	public void exit () {
		synchronized (ElectionLock.getInstance()) {
			System.out.println("Ho chiamato la notify");
			token = false;
			ElectionLock.getInstance().notify();
			System.out.println("Ho svegliato qualcuno");
		}
	}
}
