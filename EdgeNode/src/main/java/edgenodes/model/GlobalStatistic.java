package edgenodes.model;

import cloudserver.model.SmartCity;
import edgenodes.utility.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GlobalStatistic {
	private HashMap<SmartCity.Node, List<SmartCity.NodeMeasurement>> nodesLocals;
	private List<SmartCity.NodeMeasurement> aggregatedGlobals;
	private SmartCity.NodeMeasurement global;
	private List<SmartCity.NodeMeasurement> globalsReceived;
	private List<SmartCity.NodeMeasurement> globalsCalculated;
	private static GlobalStatistic instance;

	public static synchronized GlobalStatistic getInstance () {
		if (instance == null) {
			instance = new GlobalStatistic();
		}
		return instance;
	}

	private GlobalStatistic () {
		this.nodesLocals = new HashMap<>();
		this.aggregatedGlobals = new Vector<>();
		this.globalsReceived = new Vector<>();
		this.globalsCalculated = new Vector<>();
	}

	public synchronized void addLocalStatistics (SmartCity.Node node, SmartCity.NodeMeasurement statistic) {
		if (nodesLocals.containsKey(node)) {
			System.out.println(nodesLocals.get(node));
			System.out.println(statistic);
			nodesLocals.get(node).add(statistic);
		} else {
			Vector<SmartCity.NodeMeasurement> nodeStat = new Vector<>();
			nodeStat.add(statistic);
			nodesLocals.put(node, nodeStat);
		}
	}

	public synchronized void setAllLocalStatistics (SmartCity.Node node, List<SmartCity.NodeMeasurement> locals) {
		if (nodesLocals.containsKey(node)) {
			nodesLocals.replace(node, locals);
		} else {
			Vector<SmartCity.NodeMeasurement> nodeStat = new Vector<>();
			nodeStat.addAll(locals);
			nodesLocals.put(node, nodeStat);
		}
	}

	public synchronized void setGlobal (SmartCity.NodeMeasurement global) {
		this.global = global;
	}

	public synchronized void updateGlobal () {
		if (!aggregatedGlobals.isEmpty()) {
			Double sum = this.aggregatedGlobals.stream().map(stat -> stat.getValue()).reduce((a, b) -> a + b).orElse(0.);
			this.global = SmartCity.NodeMeasurement.newBuilder().setValue(sum / this.aggregatedGlobals.size()).setTimestamp(Utility.generateTimestamp()).build();
			this.globalsCalculated.add(this.global);
		}

	}

	public void addAggregatedGlobals (SmartCity.NodeMeasurement global) {
		this.aggregatedGlobals.add(global);
	}

	public SmartCity.NodeMeasurement getGlobal () {
		return this.global;
	}

	public HashMap<SmartCity.Node, List<SmartCity.NodeMeasurement>> getNodesLocals () {
		return this.nodesLocals;
	}

	public List<SmartCity.NodeLocalStatistics> getNodeslocalsMsg () {
		List<SmartCity.NodeLocalStatistics> msgs = new Vector<>();
		for (SmartCity.Node node : this.nodesLocals.keySet()) {
			msgs.add(SmartCity.NodeLocalStatistics.newBuilder().setNode(node).addAllLocals(this.nodesLocals.get(node)).build());
		}
		return msgs;
	}

	public boolean isThereAnyLocal () {
		List<SmartCity.NodeMeasurement> locals = new Vector<>();
		this.getNodesLocals().values().forEach(locals::addAll);
		return !locals.isEmpty();
	}

	public synchronized void clearGlobals () {
		this.aggregatedGlobals.clear();
	}

	public synchronized void clearLocals () {
		this.nodesLocals.clear();
	}

	public synchronized void addGlobalsReceived (SmartCity.NodeMeasurement global) {
		if (global != null && global.getTimestamp() != 0 && global.getValue() != 0.) {
			this.globalsReceived.add(global);
		}

	}

	public synchronized SmartCity.NodeMeasurement getLastGlobalReceived () {
		if (globalsReceived != null && !globalsReceived.isEmpty()) {
			this.globalsReceived.sort(Utility.getComparator());
			return this.globalsReceived.get(this.globalsReceived.size() - 1);
		}
		return null;
	}

	public synchronized SmartCity.NodeMeasurement getLastGlobalCalculated () {
		if (globalsCalculated != null && !globalsCalculated.isEmpty()) {
			this.globalsCalculated.sort(Utility.getComparator());
			return this.globalsCalculated.get(this.globalsCalculated.size() - 1);
		}
		return null;
	}

	public List<SmartCity.NodeMeasurement> getGlobalsReceived () {
		return this.globalsReceived;
	}

	@Override
	public String toString () {
		return "GlobalStatistic{" +
				"nodesLocals=" + nodesLocals +
				", aggregatedGlobals=" + aggregatedGlobals +
				", global=" + global +
				'}';
	}
}
