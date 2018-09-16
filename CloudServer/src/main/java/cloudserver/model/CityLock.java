package cloudserver.model;

public class CityLock extends MyLock {
	private static CityLock instance;

	public synchronized static CityLock getInstance () {
		if (instance == null) {
			instance = new CityLock();
		}
		return instance;
	}

	private CityLock () {

	}
}
