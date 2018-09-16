package sensors.model;

import cloudserver.model.SmartCity;

public class CloserNode extends MyLock {
	private static CloserNode closer;
	private SmartCity.Node node = null;

	public static synchronized CloserNode getInstance () {
		if (closer == null) {
			closer = new CloserNode();
		}
		return closer;
	}

	private CloserNode () {
	}

	;

	public void setNode (SmartCity.Node node) {
		this.node = node;
	}

	public SmartCity.Node getNode () {
		return this.node;
	}

}
