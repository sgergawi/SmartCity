package cloudserver.model;

public class CityMutex {
	private boolean token;
	private long threadid;
	private static CityMutex cityMutex;

	public synchronized static CityMutex getInstance () {
		if (cityMutex == null) {
			cityMutex = new CityMutex();
		}
		return cityMutex;
	}

	private CityMutex () {
		token = false;
	}

	public void enter () {
		synchronized (CityMutex.getInstance()) {
			if (token && threadid != Thread.currentThread().getId()) {
				try {
					//System.out.println("entro nella critica");
					CityMutex.getInstance().wait();
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
		synchronized (CityMutex.getInstance()) {
			token = false;
			CityMutex.getInstance().notify();
		}
	}
}
