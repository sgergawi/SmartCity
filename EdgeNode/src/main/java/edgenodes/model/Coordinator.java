package edgenodes.model;

import cloudserver.model.SmartCity;

public class Coordinator {

	private SmartCity.Node coordinatorNode = null;
	private SmartCity.Node fatherNode = null;
	private static Coordinator coord;

	public static Coordinator getInstance () {
		if (coord == null) {
			coord = new Coordinator();
		}
		return coord;
	}

	private Coordinator () {
	}

	public synchronized SmartCity.Node getCoordinator () {
		return coordinatorNode;
	}

	public synchronized SmartCity.Node getFatherNode () {
		return fatherNode;
	}

	public synchronized void setCoordinator (SmartCity.Node node) {
		this.coordinatorNode = node;
	}

	public synchronized void setFatherNode (SmartCity.Node node) {
		this.fatherNode = node;
	}

	@Override
	public String toString () {
		return this.coordinatorNode != null ? this.coordinatorNode.toString() : null;
	}

}
