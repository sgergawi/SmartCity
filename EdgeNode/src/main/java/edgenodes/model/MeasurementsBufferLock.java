package edgenodes.model;

public class MeasurementsBufferLock extends MyLock {

	private static MeasurementsBufferLock instance;

	public static synchronized MeasurementsBufferLock getInstance () {
		if (instance == null) {
			instance = new MeasurementsBufferLock();
		}
		return instance;
	}

	private MeasurementsBufferLock () {
	}
}
