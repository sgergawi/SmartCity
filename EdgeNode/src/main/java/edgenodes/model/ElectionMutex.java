package edgenodes.model;

public class ElectionMutex {
	private boolean token;
	private long threadid;
	private static ElectionMutex electionInProgress;

	public synchronized static ElectionMutex getInstance () {
		if (electionInProgress == null) {
			electionInProgress = new ElectionMutex();
		}
		return electionInProgress;
	}

	private ElectionMutex () {
		token = false;
	}

	public void enter () {
		synchronized (ElectionLock.getInstance()) {
			if (token && threadid != Thread.currentThread().getId()) {
				try {
					//System.out.println("entro nella critica");
					ElectionLock.getInstance().wait();
					//System.out.println("Esco dalla critica");
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
			token = false;
			ElectionLock.getInstance().notify();
		}
	}
}
