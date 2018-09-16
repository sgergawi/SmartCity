package edgenodes.model;

public class MeasurementsBufferLocker {

	private static MeasurementsBufferLocker instance;

	public static synchronized MeasurementsBufferLocker getInstance () {
		if (instance == null) {
			instance = new MeasurementsBufferLocker();
		}
		return instance;
	}

	private MeasurementsBufferLocker () {
	}
}
