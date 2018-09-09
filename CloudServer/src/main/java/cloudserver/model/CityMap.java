package cloudserver.model;

import cloudserver.utility.CloudServerUtility;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {

	private List<CityNode> cityNodes;
	private List<SmartCity.NodeMeasurement> globalStats;
	private CityNode root;
	private static CityMap map;
	private static int maxChildsNum = 3;


	public synchronized static CityMap getInstance () {
		if (map == null) {
			map = new CityMap();
		}
		return map;
	}

	public synchronized static void setMaxChildsNum (int totChilds) {
		maxChildsNum = totChilds;
	}

	private CityMap () {
		this.cityNodes = new Vector<CityNode>();
		this.globalStats = new Vector<SmartCity.NodeMeasurement>();
	}

	public SmartCity.Nodes getNodes () {
		return cityNodes == null ? null : SmartCity.Nodes.newBuilder().addAllNodes(cityNodes.stream().map(nd -> nd.getNode()).collect(Collectors.toList())).build();
	}

	public synchronized void addNode (SmartCity.Node node, SmartCity.NodeMeasurements measurements) {
		this.cityNodes.add(new CityNode(node, measurements));
	}

	public CityNode getTreeRoot () {
		return this.root;
	}

	public SmartCity.Node addChildNode (CityNode currentRoot, CityNode node) {
		//CityNode nodeToBeInserted = new CityNode(node, SmartCity.NodeStatistics.newBuilder().addAllStatistics(new Vector<>()).build());
		if (currentRoot == null) {
			this.root = node;
			return node.getNode();
		}
		List<CityNode> childs = currentRoot.getChildNodes();
		Integer minDistance = null;
		int distanceFromRoot = CloudServerUtility.getNodesDistance(node.getNode(), currentRoot.getNode().getXPos(), currentRoot.getNode().getYPos());
		CityNode closerNode = null;
		int distance;
		for (CityNode child : childs) {
			distance = CloudServerUtility.getNodesDistance(node.getNode(), child.getNode().getXPos(), child.getNode().getYPos());
			if (minDistance == null) {
				minDistance = distance;
				closerNode = child;
			} else {
				minDistance = Integer.min(minDistance, distance);
				closerNode = Integer.compare(minDistance, distance) > 0 ? child : closerNode;
			}
		}
		if (childs.size() < maxChildsNum && (minDistance == null || distanceFromRoot <= minDistance)) {
			System.out.println("Inserisco il nodo nei figli di: " + currentRoot.getNode().getId());
			childs.add(node);
			return currentRoot.getNode();
		} else {
			System.out.println("Voglio aggiungerlo sotto il nodo: " + closerNode);
			return this.addChildNode(closerNode, node);
		}
	}

	public synchronized void removeNodeFromTree (CityNode currentFather, CityNode currentNode, int nodeId) {
		if (currentNode == null || (currentNode.getNode().getId() != nodeId && currentNode.getChildNodes().isEmpty())) {
			//Il nodo non c'è
			return;
		}
		if (currentNode.getNode().getId() == nodeId) {
			currentFather.getChildNodes().removeIf(cityNode -> cityNode.getNode().getId() == nodeId);
			currentNode.getChildNodes().forEach(citynode -> this.map.addChildNode(this.root, citynode));
		}
		currentNode.getChildNodes().forEach(cityNode -> removeNodeFromTree(currentNode, cityNode, nodeId));
	}

	public synchronized void removeNode (int nodeId) {
		this.cityNodes.removeIf(cn -> cn.getNode().getId() == nodeId);
	}

	public List<CityNode> getCityNodes () {
		return this.cityNodes;
	}

	public synchronized void addGlobal (SmartCity.NodeMeasurement global) {
		//TODO qua il synchronized va messo sulle globals
		if (global != null) {
			this.globalStats.add(global);
		}
	}

	public List<SmartCity.Node> getLeafNodes (CityNode currentRoot) {
		if (currentRoot == null) {
			return new Vector<>();
		}
		if (currentRoot.getChildNodes().isEmpty()) {
			return Arrays.asList(currentRoot.getNode());
		}
		List<SmartCity.Node> result = new Vector<>();
		for (CityNode node : currentRoot.getChildNodes()) {
			result.addAll(getLeafNodes(node));
		}
		return result;
	}

	public SmartCity.Node getNodeFather (CityNode currentFather, CityNode currentNode, int nodeId) {
		if (currentNode == null || (currentNode.getNode().getId() != nodeId && currentNode.getChildNodes().isEmpty())) {
			return null;
		}
		if (currentNode.getNode().getId() == nodeId) {
			return currentFather.getNode();
		}
		for (CityNode child : currentNode.getChildNodes()) {
			SmartCity.Node result = getNodeFather(currentNode, child, nodeId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public List<SmartCity.NodeMeasurement> getGlobals () {
		return this.globalStats;
	}

	@Override
	public String toString () {
		return this.cityNodes.toString() + ", global statistics: " + globalStats;
	}
}
