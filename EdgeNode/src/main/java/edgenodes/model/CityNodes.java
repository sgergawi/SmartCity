package edgenodes.model;

import cloudserver.model.SmartCity;

import java.util.List;
import java.util.Vector;

public class CityNodes {
	private List<SmartCity.Node> allNodes;

	private static CityNodes instance;

	public synchronized static CityNodes getInstance () {
		if (instance == null) {
			instance = new CityNodes();
		}
		return instance;
	}

	private CityNodes () {
		allNodes = new Vector<>();
	}

	public synchronized void addCityNode (SmartCity.Node node) {
		this.allNodes.removeIf(n -> n.getId() == node.getId());
		this.allNodes.add(node);
	}

	public synchronized void addAllCityNodes (List<SmartCity.Node> nodes) {
		nodes.stream().forEach(n -> this.addCityNode(n));
	}

	public synchronized void removeCityNode (SmartCity.Node node) {
		this.allNodes.removeIf(n -> n.getId() == node.getId());
	}

	@Override
	public String toString () {
		return this.allNodes != null ? this.allNodes.toString() : null;
	}

	public List<SmartCity.Node> getAllNodes () {
		return this.allNodes;
	}


}
