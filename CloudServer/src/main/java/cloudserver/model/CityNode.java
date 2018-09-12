package cloudserver.model;

import java.util.List;
import java.util.Vector;

public class CityNode {
	private SmartCity.Node node;
	private List<CityNode> childNodes;
	private SmartCity.NodeMeasurements nodeStatistics;

	public synchronized SmartCity.Node getNode () {
		return this.node;
	}

	public synchronized SmartCity.NodeMeasurements getNodeStatistics () {
		return this.nodeStatistics;
	}

	public CityNode (SmartCity.Node node, SmartCity.NodeMeasurements statistics) {
		this.node = node;
		this.nodeStatistics = statistics;
		this.childNodes = new Vector<>();
	}

	public CityNode () {
	}

	public synchronized void addAllStatistics (List<SmartCity.NodeMeasurement> stats) {
		//List<SmartCity.NodeMeasurement> currentStats = this.nodeStatistics.getStatisticsList();
		this.nodeStatistics = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(stats).build();
	}

	public List<CityNode> getChildNodes () {
		return this.childNodes;
	}

	@Override
	public String toString () {
		return "CityNode{" +
				"node=" + node +
				", childNodes=" + childNodes +
				", nodeStatistics=" + nodeStatistics +
				'}';
	}
}
