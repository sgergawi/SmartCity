package edgenodes.model;

public class ElectionLock {
	private static ElectionLock instance;

	private ElectionLock () {

	}

	public synchronized static ElectionLock getInstance () {
		if (instance == null) {
			instance = new ElectionLock();
		}
		return instance;
	}
}
