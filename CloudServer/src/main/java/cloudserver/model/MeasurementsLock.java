package cloudserver.model;

public class MeasurementsLock extends MyLock {

	private static MeasurementsLock measurementsLock;

	public synchronized static MeasurementsLock getInstance () {
		if (measurementsLock == null) {
			measurementsLock = new MeasurementsLock();
		}
		return measurementsLock;
	}

	private MeasurementsLock () {

	}
}
