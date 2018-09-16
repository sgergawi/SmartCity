package cloudserver.model;

import cloudserver.utility.CloudServerUtility;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {

	private List<CityNode> cityNodes;
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
	}

	public synchronized SmartCity.Nodes getNodes () {
		return cityNodes == null ? null : SmartCity.Nodes.newBuilder().addAllNodes(cityNodes.stream().map(nd -> nd.getNode()).collect(Collectors.toList())).build();
	}

	public synchronized void addNode (SmartCity.Node node, SmartCity.NodeMeasurements measurements) {
		this.cityNodes.add(new CityNode(node, measurements));
	}

	public synchronized CityNode getTreeRoot () {
		return this.root;
	}

	public void setTreeRoot (SmartCity.Node newCoord) {
		CityNode treeroot = new CityNode(newCoord, SmartCity.NodeMeasurements.newBuilder().addAllStatistics(new Vector<>()).build());
		this.root = treeroot;
	}

	public void setTreeRoot (CityNode citynode) {
		this.root = citynode;
	}

	public synchronized SmartCity.Node addChildNode (CityNode currentRoot, CityNode node) {
		//CityNode nodeToBeInserted = new CityNode(node, SmartCity.NodeStatistics.newBuilder().addAllStatistics(new Vector<>()).build());
		if (currentRoot == null) {
			this.setTreeRoot(node);
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
			//System.out.println("Inserisco il nodo nei figli di: " + currentRoot.getNode().getId());
			childs.add(node);
			return currentRoot.getNode();
		} else {
			//System.out.println("Voglio aggiungerlo sotto il nodo: " + closerNode);
			return this.addChildNode(closerNode, node);
		}
	}

	public synchronized void removeNodeFromTree (CityNode currentFather, CityNode currentNode, int nodeId) {
		if (currentNode == null || (currentNode.getNode().getId() != nodeId && currentNode.getChildNodes().isEmpty())) {
			//Il nodo non c'è
			return;
		}
		if (this.root.getNode().getId() == nodeId) {
			//System.out.println("root: " + this.root.getNode());
			//System.out.println("current node: " + currentNode.getNode());
			this.setTreeRoot((CityNode) null);
			return;
		}
		List<CityNode> childs = new Vector<>();
		childs.addAll(currentNode.getChildNodes());

		if (currentNode.getNode().getId() == nodeId) {
			if (currentFather != null) {
				currentFather.getChildNodes().removeIf(cityNode -> cityNode.getNode().getId() == nodeId);
			}
			currentNode.getChildNodes().forEach(citynode -> this.map.addChildNode(this.root, citynode));
		} else {
			for (CityNode cityNode : childs) {
				removeNodeFromTree(currentNode, cityNode, nodeId);
			}
		}
	}

	public synchronized void removeNode (int nodeId) {
		this.cityNodes.removeIf(cn -> cn.getNode().getId() == nodeId);
		this.removeNodeFromTree(null, this.getTreeRoot(), nodeId);
	}

	public List<CityNode> getCityNodes () {
		return this.cityNodes;
	}


	public synchronized List<SmartCity.Node> getLeafNodes (CityNode currentRoot) {
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

	public synchronized SmartCity.Node getNodeFather (CityNode currentFather, CityNode currentNode, int nodeId) {
		if (currentNode == null || (currentNode.getNode().getId() != nodeId && currentNode.getChildNodes().isEmpty())) {
			return null;
		}
		if (currentNode.getNode().getId() == nodeId) {
			return currentFather == null ? currentNode.getNode() : currentFather.getNode();
		}
		for (CityNode child : currentNode.getChildNodes()) {
			SmartCity.Node result = getNodeFather(currentNode, child, nodeId);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public String toString () {
		return "CityMap{" +
				"cityNodes=" + cityNodes +
				", root=" + root +
				'}';
	}
}
