package cloudserver.controller;


import cloudserver.model.*;
import cloudserver.utility.CloudServerUtility;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.spi.http.HttpContext;
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
	public Response getNodesState (@QueryParam("xcoord") Integer xPos, @QueryParam("ycoord") Integer yPos, @HeaderParam("accept") String media) {
		try {
			CityMap map = CityMap.getInstance();
			List<SmartCity.Node> nodes = null;
			if (xPos != null && yPos != null) {
				//se vengono specificati vuol dire che mi sta chiamando un sensore quindi
				//vuole conoscere quali sono i nodi più vicini a lui in base all'alberatura dei nodi -> cerco solo le foglie!!!
				CityMutex.getInstance().enter();
				nodes = map.getLeafNodes(map.getTreeRoot()).stream().filter(node -> CloudServerUtility.getNodesDistance(node, xPos, yPos) < 20).sorted(CloudServerUtility.getNodesDistanceComparator(xPos, yPos)).collect(Collectors.toList());
				if (nodes != null && !nodes.isEmpty()) {
					byte[] toSend = nodes.get(0).toByteArray();
					CityMutex.getInstance().exit();
					return Response.ok().entity(toSend).build();
				} else {
					CityMutex.getInstance().exit();
					return Response.status(Response.Status.NOT_FOUND).build();
				}
			}
			nodes = map.getNodes() != null ? map.getNodes().getNodesList() : new Vector<>();
			CityMutex.getInstance().exit();
			if (nodes.isEmpty()) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				return Response.ok().entity(map.getNodes().toByteArray()).build();
			}
		} catch (Exception e) {
			CityMutex.getInstance().exit();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
			CityMutex.getInstance().enter();
			List<SmartCity.Node> nodes = map.getNodes().getNodesList();
			copy.addAll(nodes);
			List<SmartCity.Node> nodesEqual = nodes.stream().filter(nd -> nd.getId() == node.getId()).collect(Collectors.toList());
			List<SmartCity.Node> nodesAround = nodes.stream().filter(nd -> CloudServerUtility.getNodesDistance(nd, node.getXPos(), node.getYPos()) < 20).collect(Collectors.toList());
			if (nodesEqual != null && !nodesEqual.isEmpty()) {
				System.out.println("Il nodo " + node.getId() + " è gia presente");
				response = response.toBuilder().setErrortype(SmartCity.ErrorType.DUPLICATED_ID).build();
				CityMutex.getInstance().exit();
				return Response.status(Response.Status.BAD_REQUEST).entity(response.toByteArray()).build();
			}
			if (nodesAround != null && !nodesAround.isEmpty()) {
				System.out.println("Il nodo " + node.getId() + " è vicino ad altri nodi");
				response = response.toBuilder().setErrortype(SmartCity.ErrorType.COORD_NOT_ALLOWED).build();
				//System.out.println(response);
				CityMutex.getInstance().exit();
				return Response.status(Response.Status.BAD_REQUEST).entity(response.toByteArray()).build();
			}
			map.addNode(node, SmartCity.NodeMeasurements.newBuilder().addAllStatistics(new Vector<>()).build());
			CityNode nodeToBeInserted = new CityNode(node, SmartCity.NodeMeasurements.newBuilder().addAllStatistics(new Vector<>()).build());
			father = map.addChildNode(map.getTreeRoot(), nodeToBeInserted);
			CityMutex.getInstance().exit();
			if (copy.isEmpty()) {
				response = response.toBuilder().setResponse(SmartCity.NodeInsertedResponse.newBuilder().setFather(father).build()).build();
				return Response.ok().entity(response.toByteArray()).build();
			} else {
				SmartCity.Nodes allnodes = SmartCity.Nodes.newBuilder().addAllNodes(copy).build();
				response = response.toBuilder().setResponse(SmartCity.NodeInsertedResponse.newBuilder().setFather(father).setAllNodes(allnodes).build()).build();
				return Response.ok().entity(response.toByteArray()).build();
			}
		} catch (Exception e) {
			CityMutex.getInstance().exit();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
			System.out.println("Thread " + Thread.currentThread().getId() + " Rimozione nodo: " + nodeId);
			CityMutex.getInstance().enter();
			CityMap map = CityMap.getInstance();
			if (!map.getNodes().getNodesList().stream().anyMatch(node -> node.getId() == nodeId)) {
				CityMutex.getInstance().exit();
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			map.removeNode(nodeId);
			CityMutex.getInstance().exit();
			return Response.ok().build();
		} catch (Exception e) {
			CityMutex.getInstance().exit();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

	@GET
	@Path("/{nodeId}/father")
	@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
	public Response getNodeFather (@PathParam("nodeId") int nodeId) {
		try {
			System.out.println("Thread " + Thread.currentThread().getId() + " Trovo il padre di " + nodeId);
			CityMutex.getInstance().enter();
			System.out.println("Thread " + Thread.currentThread().getId() + " Entro nel lock");
			CityMap map = CityMap.getInstance();
			SmartCity.Node father = map.getNodeFather(null, map.getTreeRoot(), nodeId);
			CityMutex.getInstance().exit();
			if (father == null) {
				//System.out.println(nodeId + " mi ha chiesto chi è suo padre");
				System.out.println("Thread " + Thread.currentThread().getId() + " Non ha un padre");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			System.out.println("Thread " + Thread.currentThread().getId() + " Suo padre è: " + father.getId());
			return Response.ok().entity(father.toByteArray()).build();
		} catch (Exception e) {
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			CityMutex.getInstance().exit();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}


	@POST
	@Path("/coordinator")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response refreshCoordinator (byte[] coordinator) {
		try {
			System.out.println("Thread " + Thread.currentThread().getId() + " Aggiornamento coordinatore");
			CityMutex.getInstance().enter();
			CityMap cityMap = CityMap.getInstance();
			SmartCity.Node node = SmartCity.Node.parseFrom(coordinator);

			if (node != null && (cityMap.getTreeRoot() == null || node.getId() != cityMap.getTreeRoot().getNode().getId())) {
				List<CityNode> citynodes = null;
				citynodes = cityMap.getCityNodes();

				//Rimuovo il vecchio padre perchè se è stata richiamata la refresh vuol dire che non è piu presente
				//citynodes.removeIf(cityNode -> cityNode.getNode().getId() == cityMap.getTreeRoot().getNode().getId());
				//this.deleteNode(cityMap.getTreeRoot().getNode().getId());
				CityNode olderRoot = cityMap.getTreeRoot();
				cityMap.setTreeRoot(node);
				CityNode currentRoot = cityMap.getTreeRoot();
				//Non voglio considerare anche la root tra i nodi da aggiungere all'albero
				citynodes = citynodes.stream().filter(cityNode -> cityNode.getNode().getId() != currentRoot.getNode().getId() && cityNode.getNode().getId() != olderRoot.getNode().getId()).collect(Collectors.toList());
				citynodes.stream().forEach(cityNode -> cityMap.addChildNode(currentRoot, cityNode));
				System.out.println("Thread " + Thread.currentThread().getId() + " nuovo albero: " + cityMap.getTreeRoot());
			}
			CityMutex.getInstance().exit();
			System.out.println("Thread " + Thread.currentThread().getId() + " Il coordinatore diventa: " + node.getId());
			return Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			CityMutex.getInstance().exit();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
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
		try {
			List<SmartCity.NodeMeasurement> globals = null;
			MeasurementsLock.getInstance().lock();
			globals = CityMeasurements.getInstance().getGlobals().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());

			List<SmartCity.NodeMeasurement> firstnglobals = (globals == null || globals.isEmpty()) ? new Vector<>() : (globals.size() > n ? globals.subList(0, n) : globals);
			List<SmartCity.NodeMeasurement> firstnlocals = new Vector<>();
			CityMap.getInstance().getCityNodes().stream().map(node -> node.getNodeStatistics().getStatisticsList()).forEach(firstnlocals::addAll);
			MeasurementsLock.getInstance().unlock();
			//System.out.println("first globals: " + firstnglobals + ", locals: " + firstnlocals);
			firstnlocals = firstnlocals.stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
			firstnlocals = firstnlocals.size() > n ? firstnlocals.subList(0, n) : firstnlocals;
			SmartCity.NodeMeasurements globalStats = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(firstnglobals).build();
			SmartCity.NodeMeasurements localsStats = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(firstnlocals).build();
			//System.out.println("globals da mandare: " + globalStats + ", localStats:" + localsStats);
			return Response.ok().entity(SmartCity.LastLocalsGlobals.newBuilder().setGlobals(globalStats).setLocals(localsStats).build().toByteArray()).build();
		} catch (Exception e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

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
			MeasurementsLock.getInstance().lock();
			CityMeasurements.getInstance().addGlobal(msg.getGlobal());
			for (CityNode node : CityMap.getInstance().getCityNodes()) {
				SmartCity.NodeLocalStatistics localStats = msg.getNodesLocalsList().stream().filter(nd -> nd.getNode().getId() == node.getNode().getId()).findFirst().orElse(null);
				if (localStats != null && localStats.getLocalsList() != null) {
					node.addAllStatistics(localStats.getLocalsList());
				}
			}
			System.out.println("Thread " + Thread.currentThread().getId() + " Refresh statistiche " + CityMeasurements.getInstance());
			MeasurementsLock.getInstance().unlock();
			return Response.ok().build();
		} catch (InvalidProtocolBufferException e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Errore nell'elaborazione del messaggio");
			return Response.status(Response.Status.BAD_REQUEST).build();
		} catch (Exception e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

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
		try {
			CityMap map = CityMap.getInstance();
			MeasurementsLock.getInstance().lock();
			List<CityNode> nodes = map.getCityNodes();
			CityNode myNode = nodes != null ? nodes.stream().filter(nd -> nd.getNode().getId() == nodeId).findFirst().orElseGet(null) : null;
			List<SmartCity.NodeMeasurement> nodeMeasurements = null;
			if (myNode == null || myNode.getNodeStatistics() == null) {
				MeasurementsLock.getInstance().unlock();
				//System.out.println("Non ci sono statistiche per quel nodo");
				return Response.status(404).build();
			}
			nodeMeasurements = myNode.getNodeStatistics().getStatisticsList().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
			List<SmartCity.NodeMeasurement> filtered = (nodeMeasurements == null || nodeMeasurements.isEmpty()) ? new Vector<>() : nodeMeasurements.size() >= n ? nodeMeasurements.subList(0, n) : nodeMeasurements;
			MeasurementsLock.getInstance().unlock();
			//System.out.println("Statistiche per quel nodo: " + filtered);
			byte[] toSend = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(filtered).build().toByteArray();
			return Response.ok().entity(toSend).build();
		} catch (Exception e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

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
		try {
			CityMap map = CityMap.getInstance();
			CityNode node = map.getCityNodes().stream().filter(nd -> nd.getNode().getId() == nodeId).findFirst().orElse(null);
			if (node == null) {
				//System.out.println("Nessun nodo trovato");
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			List<SmartCity.NodeMeasurement> stats = null;
			MeasurementsLock.getInstance().lock();
			stats = node.getNodeStatistics().getStatisticsList().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());
			MeasurementsLock.getInstance().unlock();

			stats = stats.size() > n ? stats.subList(0, n) : stats;
			Double mean = CloudServerUtility.getMean(stats);
			Double devstd = CloudServerUtility.getDevstd(stats, mean);
			//System.out.println("Media: " + mean + ", devstd: " + devstd);
			return Response.ok().entity(SmartCity.AggregatedStatistic.newBuilder().setDevstd(devstd).setMean(mean).build().toByteArray()).build();
		} catch (Exception e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

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
		try {
			CityMeasurements measurements = CityMeasurements.getInstance();
			double mean = 0;
			double devstd = 0;
			MeasurementsLock.getInstance().lock();
			List<SmartCity.NodeMeasurement> globals = measurements.getGlobals().stream().sorted(CloudServerUtility.getStatsComparator()).collect(Collectors.toList());

			globals = globals.size() > n ? globals.subList(0, n) : globals;
			if (globals.isEmpty()) {
				//System.out.println("Non ci sono globals");
				MeasurementsLock.getInstance().unlock();
				return Response.status(Response.Status.NOT_FOUND).build();
			}
			mean = CloudServerUtility.getMean(globals);
			devstd = CloudServerUtility.getDevstd(globals, mean);
			MeasurementsLock.getInstance().unlock();
			//System.out.println("Media: " + mean);
			//System.out.println("Devstd: " + devstd);
			return Response.ok().entity(SmartCity.AggregatedStatistic.newBuilder().setDevstd(devstd).setMean(mean).build().toByteArray()).build();
		} catch (Exception e) {
			MeasurementsLock.getInstance().unlock();
			System.out.println("Errore :- Si è verificato un errore generico nell'elaborazione dei dati");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

	}

}
