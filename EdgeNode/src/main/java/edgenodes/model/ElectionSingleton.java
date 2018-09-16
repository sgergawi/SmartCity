package edgenodes.model;

public class ElectionSingleton {
	private static ElectionSingleton instance;

	private ElectionSingleton () {

	}

	public synchronized static ElectionSingleton getInstance () {
		if (instance == null) {
			instance = new ElectionSingleton();
		}
		return instance;
	}
}
