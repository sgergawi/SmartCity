package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.model.Coordinator;
import edgenodes.model.GlobalStatistic;
import edgenodes.model.MajorNodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class NodeCommunicationThread extends Thread {

	private Socket connection;
	private SmartCity.Node node;

	public NodeCommunicationThread (Socket connection, SmartCity.Node node) {
		this.connection = connection;
		this.node = node;
	}

	/**
	 * Quando un nodo riceve un messaggio da altri nodi deve decodificare il messaggio contenuto in esso
	 * in base al protocollo prefissato. Per ogni messaggio vi è un codice iniziale che identifica la tipologia
	 * della request.
	 */
	@Override
	public void run () {
		try {
			DataInputStream inputStream = new DataInputStream(this.connection.getInputStream());
			byte[] message = new byte[inputStream.readInt()];
			inputStream.read(message, 0, message.length);

			SmartCity.MessageRequest request = SmartCity.MessageRequest.parseFrom(message);
			SmartCity.MessageType type = request.getTypemessage();
			switch (type) {
				case HELLO:
					manageHelloRequest(request);
					break;
				case LOCALSTATISTIC:
					manageStatisticUpdate(request);
					break;
				case LOCALSGLOBALS:
					manageLocalsGlobalsStatisticUpdate(request);
					break;
				case ELECTIONTIME:
					manageElectionTime(request);
					break;
				case ELECTIONRECEIVED:
					System.out.println("ELECTION RECEIVED");
					break;
				case ELECTIONRESULT:
					manageElectionResult(request);
					break;
				default:
					System.out.println("Errore :- message type non riconosciuto");
					break;
			}
			this.connection.close();
		} catch (Exception e) {
			System.out.println("Errore nel parsing del messaggio");
		}

	}

	public void manageHelloRequest (SmartCity.MessageRequest request) {
		try {
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			if (request.getNode().getId() > this.node.getId()) {
				MajorNodes.getInstance().addMajorThanMe(request.getNode());
			}
			SmartCity.HelloResponse response = SmartCity.HelloResponse.newBuilder().setTypemessage(SmartCity.MessageType.WELCOME).build();
			if (Coordinator.getInstance().getCoordinator().getId() == this.node.getId()) {
				response = response.toBuilder().setIscoordinator(true).build();
			} else {
				response = response.toBuilder().setIscoordinator(false).build();
			}
			byte[] output = response.toByteArray();
			outputStream.writeInt(output.length);
			outputStream.write(output);
		} catch (Exception e) {
			System.out.println("Errore durante la hello request");
		}

	}

	/**
	 * Questo metodo viene chiamato quando i nodi foglia chiamano il padre per inviargli le aggregazioni
	 * effettuate con sliding window e overlap del 50%. Questo metodo aggiorna le informazioni che il nodo padre
	 * ha su quel figlio (ovvero le misurazioni locali del figlio) e aggiunge quelle aggregazioni alla lista aggregatedGlobals
	 * che poi verrà coinvolta per l'aggiornamento delle statistiche locali calcolate da questo nodo
	 *
	 * @param request
	 */
	public void manageStatisticUpdate (SmartCity.MessageRequest request) {
		SmartCity.NodeMeasurement statistic = request.getStatisticMsg().getStatistic();
		SmartCity.Node node = request.getStatisticMsg().getNode();
		GlobalStatistic.getInstance().addLocalStatistics(node, statistic);
		GlobalStatistic.getInstance().addAggregatedGlobals(statistic);
		sendCurrentGlobalToChild();
	}

	private void sendCurrentGlobalToChild () {
		try {
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			SmartCity.NodeMeasurement global = GlobalStatistic.getInstance().getGlobal();
			byte[] output = global == null ? SmartCity.NodeMeasurement.newBuilder().build().toByteArray() : global.toByteArray();
			outputStream.writeInt(output.length);
			outputStream.write(output);
		} catch (Exception e) {
			System.out.println("Non è stato possibile rispondere all'aggiornamento di statistiche");
		}
	}

	/**
	 * Metodo che gestisce la ricezione delle aggregazioni parziali effettuate dai nodi intermedi. Questo metodo viene chiamato dal padre
	 * quando riceve le aggregazioni calcolate dai figli. Il padre aggiunge l'aggregazione come misurazione locale del nodo figlio e
	 * aggiunge la misurazione anche come dato aggregato che verrà successivamente utilizzato per applicare il livello successivo di aggregazione.
	 * Il padre salva anche tutte le misurazioni locali dei nodi figli del figlio.
	 *
	 * @param request
	 */
	public void manageLocalsGlobalsStatisticUpdate (SmartCity.MessageRequest request) {
		System.out.println("Ricevo un aggregato dal nodo figlio");
		SmartCity.LocalsGlobalsMessage msg = request.getLocalsglobalsUpdate();
		SmartCity.NodeMeasurement global = msg.getGlobal();
		List<SmartCity.NodeLocalStatistics> childsMeasurements = msg.getNodesLocalsList();
		GlobalStatistic globalSituation = GlobalStatistic.getInstance();
		globalSituation.addAggregatedGlobals(global);
		childsMeasurements.stream().forEach(measurement -> globalSituation.setAllLocalStatistics(measurement.getNode(), measurement.getLocalsList()));
		sendCurrentGlobalToChild();
	}

	public void manageElectionTime (SmartCity.MessageRequest request) {
		try {
			System.out.println("Mi ha chiamato il nodo " + request.getNode().getId());
			/*Socket socket = new Socket(request.getNode().getSelfIp(), request.getNode().getOtherNodesPort());*/
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			SmartCity.MessageRequest response = SmartCity.MessageRequest.newBuilder().setNode(this.node).setTypemessage(SmartCity.MessageType.ELECTIONRECEIVED).build();
			byte[] respBytes = response.toByteArray();
			outputStream.writeInt(respBytes.length);
			outputStream.write(respBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NodeMain.startElection(this.node);
	}

	public void manageElectionResult (SmartCity.MessageRequest request) {
		System.out.println(this.node.getId() + " ha aggiornato il coordinatore");
		try {
			Coordinator.getInstance().setCoordinator(request.getNode());
			System.out.println("Coordinatore: " + Coordinator.getInstance().getCoordinator());
			if (node.getId() == Coordinator.getInstance().getCoordinator().getId()) {
				GlobalStatisticsThread thread = new GlobalStatisticsThread(node);
				thread.start();
				thread.join();
			}
		} catch (Exception e) {
			System.out.println("Errore nell'aggiornamento del coordinatore: " + e);
			NodeMain.deleteNodeServerSide(this.node);
		}

	}
}
