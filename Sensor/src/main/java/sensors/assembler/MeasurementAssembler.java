package sensors.assembler;

import cloudserver.model.SmartCity;
import lib.Measurement;

public class MeasurementAssembler {

	public static SmartCity.NodeMeasurement assembleFrom (Measurement m) {
		SmartCity.NodeMeasurement measurement = SmartCity.NodeMeasurement.newBuilder().build();
		if (m != null) {
			measurement = measurement.toBuilder().setTimestamp(m.getTimestamp()).setValue(m.getValue()).build();
		}
		return measurement;
	}
}
