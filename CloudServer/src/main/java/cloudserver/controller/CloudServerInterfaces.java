package cloudserver.controller;


import cloudserver.model.*;
import cloudserver.utility.CloudServerUtility;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/cloud-server/nodes")
public class CloudServerInterfaces {

	/**
	 * Restituisce la situazione complessiva della mappa della città ovvero l'elenco dei nodi e le loro posizioni ma se
	 * vengono forniti anche xPos e yPos vuol dire che è stato chiamato da un sensore e quindi si deve restituire solo
	 * il nodo più vicino rispetto a quelle coordinate.
	 *
	 * @param xPos
	 * @param yPos
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response getNodesState (@QueryParam("xcoord") Integer xPos, @QueryParam("ycoord") Integer yPos) {
		CityMap map = CityMap.getInstance();
		if (xPos != null && yPos != null) {
			//se vengono specificati vuol dire che mi sta chiamando un sensore quindi
			//vuole conoscere quali sono i nodi più vicini a lui in base all'alberatura dei nodi -> cerco solo le foglie!!!
			List<SmartCity.Node> nodes = map.getLeafNodes(map.getTreeRoot()).stream().filter(node -> CloudServerUtility.getNodesDistance(node, xPos, yPos) < 20).sorted(CloudServerUtility.getNodesDistanceComparator(xPos, yPos)).collect(Collectors.toList());
			if (nodes != null && !nodes.isEmpty()) {
				byte[] toSend = nodes.get(0).toByteArray();
				return Response.ok().entity(toSend).header(HttpHeaders.CONTENT_LENGTH, toSend.length).build();
			} else {
				return Response.status(404).build();
			}
		}
		List<SmartCity.Node> nodes = map.getNodes() != null ? map.getNodes().getNodesList() : new Vector<>();
		if (nodes.isEmpty()) {
			return Response.status(404).build();
		} else {
			return Response.ok().entity(map.getNodes().toByteArray()).build();
		}
	}

	/**
	 * Inserisce il nodo nella mappa se non vi sono nodi già presenti in quel raggio o se vi sono già altri nodi presenti
	 * distanti massimo 20 da quella posizione.
	 *
	 * @param input
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response addNode (byte[] input) {
		CityMap map = CityMap.getInstance();
		List<SmartCity.Node> copy = new Vector<>();
		SmartCity.InitializationMessage response = SmartCity.InitializationMessage.newBuilder().build();
		SmartCity.Node father;
		try {
			SmartCity.Node node = SmartCity.Node.parseFrom(input);
			System.out.println(node);
			List<SmartCity.Node> nodes = map.getNodes().getNodesList();
			copy.addAll(nodes);
			List<SmartCity.Node> nodesEqual = nodes.stream().filter(nd -> nd.getId() == node.getId()).collect(Collectors.toList());
			List<SmartCity.Node> nodesAround = nodes.stream().filter(nd -> CloudServerUtility.getNodesDistance(nd, node.getXPos(), node.getYPos()) < 20).collect(Collectors.toList());
			if (nodesEqual != null && !nodesEqual.isEmpty()) {
				System.out.println("Il nodo " + node.getId() + " è gia presente");
				response = response.toBuilder().setErrortype(SmartCity.ErrorType.DUPLICATED_ID).build();
				return Response.status(Response.Status.BAD_REQUEST).entity(response.toByteArray()).build();
			}
			if (nodesAround != null && !nodesAround.isEmpty()) {
				System.out.println("Il nodo " + node.getId() + " è vicino ad altri nodi");
				response = response.toBuilder().setErrortype(SmartCity.ErrorType.COORD_NOT_ALLOWED).build();
				System.out.println(response);
				return Response.status(Response.Status.BAD_REQUEST).entity(response.toByteArray()).build();
			}
			map.addNode(node, SmartCity.NodeMeasurements.newBuilder().addAllStatistics(new Vector<>()).build());
			CityNode nodeToBeInserted = new CityNode(node, SmartCity.NodeMeasurements.newBuilder().addAllStatistics(new Vector<>()).build());
			father = map.addChildNode(map.getTreeRoot(), nodeToBeInserted);
		} catch (Exception e) {
			System.out.println("Errore durante l'aggiunta del nodo ");
			e.printStackTrace();
			response = response.toBuilder().setErrortype(SmartCity.ErrorType.UNEXPECTED_ERROR).build();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.toByteArray()).build();
		}
		System.out.println("Finito");
		if (copy.isEmpty()) {
			response = response.toBuilder().setResponse(SmartCity.NodeInsertedResponse.newBuilder().setFather(father).build()).build();
			return Response.ok().entity(response.toByteArray()).build();
		} else {
			SmartCity.Nodes allnodes = SmartCity.Nodes.newBuilder().addAllNodes(copy).build();
			response = response.toBuilder().setResponse(SmartCity.NodeInsertedResponse.newBuilder().setFather(father).setAllNodes(allnodes).build()).build();
			return Response.ok().entity(response.toByteArray()).build();
		}

	}

	/**
	 * @param nodeId
	 * @return
	 */
	@DELETE
	@Path("/{nodeid}")
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response deleteNode (@PathParam("nodeid") int nodeId) {
		try {
			CityMap map = CityMap.getInstance();
			if (!map.getNodes().getNodesList().stream().anyMatch(node -> node.getId() == nodeId)) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			map.removeNode(nodeId);
			map.removeNodeFromTree(null, map.getTreeRoot(), nodeId);
			return Response.ok().build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@GET
	@Path("/{nodeId}/father")
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response getNodeFather (@PathParam("nodeId") int nodeId) {
		try {
			CityMap map = CityMap.getInstance();
			SmartCity.Node father = map.getNodeFather(null, map.getTreeRoot(), nodeId);
			return Response.ok().entity(father.toByteArray()).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Restituisce le ultime N statistiche globali e locali della città con timestamp
	 *
	 * @param n
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Path("/measurements")
	public Response getMeasurements (@DefaultValue("10") @QueryParam("n") int n) {
		List<SmartCity.NodeMeasurement> globals = CityMap.getInstance().getGlobals().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
		List<SmartCity.NodeMeasurement> firstnglobals = globals.size() > n ? globals.subList(0, n) : globals;
		List<SmartCity.NodeMeasurement> firstnlocals = new Vector<>();
		CityMap.getInstance().getCityNodes().stream().map(node -> node.getNodeStatistics().getStatisticsList()).forEach(firstnlocals::addAll);
		System.out.println("first globals: " + firstnglobals + ", locals: " + firstnlocals);
		firstnlocals = firstnlocals.stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
		firstnlocals = firstnlocals.size() > n ? firstnlocals.subList(0, n) : firstnlocals;
		SmartCity.NodeMeasurements globalStats = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(firstnglobals).build();
		SmartCity.NodeMeasurements localsStats = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(firstnlocals).build();
		System.out.println("globals da mandare: " + globalStats + ", localStats:" + localsStats);
		return Response.ok().entity(SmartCity.LastLocalsGlobals.newBuilder().setGlobals(globalStats).setLocals(localsStats).build().toByteArray()).build();
	}

	/**
	 * Riceve le statistiche locali raccolte dal coordinatore e una nuova statistica
	 * globale.
	 *
	 * @return
	 */
	@Path("/measurements")
	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response refreshMeasurements (byte[] statistics) {
		try {
			SmartCity.LocalsGlobalsMessage msg = SmartCity.LocalsGlobalsMessage.parseFrom(statistics);
			CityMap.getInstance().addGlobal(msg.getGlobal());
			for (CityNode node : CityMap.getInstance().getCityNodes()) {
				SmartCity.NodeLocalStatistics localStats = msg.getNodesLocalsList().stream().filter(nd -> nd.getNode().getId() == node.getNode().getId()).findFirst().orElse(null);
				if (localStats != null && localStats.getLocalsList() != null) {
					node.addAllStatistics(localStats.getLocalsList());
				}
			}
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore nella ricezione del messaggio");
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		System.out.println("Mappa aggiornata: " + CityMap.getInstance());
		return Response.ok().build();
	}

	/**
	 * Restituisce le ultime N statistiche generate da uno specifico nodo.
	 *
	 * @param n
	 * @param nodeId
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Path("/{nodeid}/measurements")
	public Response getNodeMeasurements (@DefaultValue("10") @QueryParam("n") int n, @PathParam("nodeid") int nodeId) {
		CityMap map = CityMap.getInstance();
		List<CityNode> nodes = map.getCityNodes();
		CityNode myNode = nodes != null ? nodes.stream().filter(nd -> nd.getNode().getId() == nodeId).findFirst().orElseGet(null) : null;
		if (myNode == null || myNode.getNodeStatistics() == null) {
			System.out.println("Non ci sono statistiche per quel nodo");
			return Response.status(404).build();
		}
		List<SmartCity.NodeMeasurement> nodeMeasurements = myNode.getNodeStatistics().getStatisticsList().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
		List<SmartCity.NodeMeasurement> filtered = nodeMeasurements.size() >= n ? nodeMeasurements.subList(0, n) : nodeMeasurements;
		System.out.println("Statistiche per quel nodo: " + filtered);
		byte[] toSend = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(filtered).build().toByteArray();
		return Response.ok().entity(toSend).build();
	}

	/**
	 * Restituisce deviazione std e media delle ultime N statistiche con timestamp di un
	 * determinato nodo edge.
	 *
	 * @param n
	 * @param nodeId
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Path("/{nodeid}/statistics")
	public Response getNodeStats (@DefaultValue("10") @QueryParam("n") int n, @PathParam("nodeid") int nodeId) {
		CityMap map = CityMap.getInstance();
		CityNode node = map.getCityNodes().stream().filter(nd -> nd.getNode().getId() == nodeId).findFirst().orElse(null);
		if (node == null) {
			System.out.println("Nessun nodo trovato");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		List<SmartCity.NodeMeasurement> stats = node.getNodeStatistics().getStatisticsList().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
		stats = stats.size() > n ? stats.subList(0, n) : stats;
		Double mean = CloudServerUtility.getMean(stats);
		Double devstd = CloudServerUtility.getDevstd(stats, mean);
		System.out.println("Media: " + mean + ", devstd: " + devstd);
		return Response.ok().entity(SmartCity.AggregatedStatistic.newBuilder().setDevstd(devstd).setMean(mean).build().toByteArray()).build();
	}

	/**
	 * Restituisce deviazione standard e media delle ultime n statistiche globali della città.
	 *
	 * @param n
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	@Path("/statistics")
	public Response getCityStats (@DefaultValue("10") @QueryParam("n") int n) {
		CityMap map = CityMap.getInstance();
		List<SmartCity.NodeMeasurement> globals = map.getGlobals().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
		globals = globals.size() > n ? globals.subList(0, n) : globals;
		if (globals.isEmpty()) {
			System.out.println("Non ci sono globals");
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		double mean = CloudServerUtility.getMean(globals);
		double devstd = CloudServerUtility.getDevstd(globals, mean);
		System.out.println("Media: " + mean);
		System.out.println("Devstd: " + devstd);
		return Response.ok().entity(SmartCity.AggregatedStatistic.newBuilder().setDevstd(devstd).setMean(mean).build().toByteArray()).build();
	}

}
