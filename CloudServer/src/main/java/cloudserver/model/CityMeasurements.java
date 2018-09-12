package cloudserver.model;

import java.util.List;
import java.util.Vector;

public class CityMeasurements {

	private List<SmartCity.NodeMeasurement> globalStats;
	private static CityMeasurements measurementsInstance;

	public synchronized static CityMeasurements getInstance () {
		if (measurementsInstance == null) {
			measurementsInstance = new CityMeasurements();
		}
		return measurementsInstance;
	}

	private CityMeasurements () {
		this.globalStats = new Vector<SmartCity.NodeMeasurement>();
	}

	public synchronized void addGlobal (SmartCity.NodeMeasurement global) {
		if (global != null) {
			this.globalStats.add(global);
		}
	}

	public synchronized List<SmartCity.NodeMeasurement> getGlobals () {
		return this.globalStats;
	}

	@Override
	public String toString () {
		return "CityMeasurements{" +
				"globalStats=" + globalStats +
				'}';
	}
}
