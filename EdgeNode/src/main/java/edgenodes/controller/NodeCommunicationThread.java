package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.model.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
			System.out.println("Errore :- si è verificato un errore generico nel parsing del messaggio.");
		}

	}

	/**
	 * Metodo che gestisce la ricezione della presentazione di un nuovo nodo in rete.
	 *
	 * @param request
	 */
	public void manageHelloRequest (SmartCity.MessageRequest request) {
		try {
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			CityNodes.getInstance().addCityNode(request.getNode());

			SmartCity.HelloResponse response = SmartCity.HelloResponse.newBuilder().setTypemessage(SmartCity.MessageType.WELCOME).build();
			if (Coordinator.getInstance().getCoordinator().getId() == this.node.getId()) {
				response = response.toBuilder().setIscoordinator(true).build();
			} else {
				response = response.toBuilder().setIscoordinator(false).build();
			}
			byte[] output = response.toByteArray();
			outputStream.writeInt(output.length);
			outputStream.write(output);
		} catch (IOException e) {
			System.out.println("Errore :- si è verificato un problema di comunicazione durante la ricezione di un HELLO.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un problema generico durante la hello request.");
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
		System.out.println("Ricevo statistica dal figlio " + node.getId());
		GlobalStatistic.getInstance().addLocalStatistics(node, statistic);
		GlobalStatistic.getInstance().addAggregatedGlobals(statistic);
		sendCurrentGlobalToChild(node);
	}

	private void sendCurrentGlobalToChild (SmartCity.Node child) {
		try {
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			SmartCity.NodeMeasurement global = null;
			if (this.node.getId() == Coordinator.getInstance().getCoordinator().getId()) {
				global = GlobalStatistic.getInstance().getLastGlobalCalculated();
			} else {
				global = GlobalStatistic.getInstance().getLastGlobalReceived();
			}
			byte[] output = global == null ? SmartCity.NodeMeasurement.newBuilder().build().toByteArray() : global.toByteArray();
			outputStream.writeInt(output.length);
			outputStream.write(output);
		} catch (IOException e) {
			CityNodes.getInstance().removeCityNode(child);
			System.out.println("Errore :- Non è stato possibile comunicare con il figlio");
		} catch (Exception e) {
			System.out.println("Errore :- Non è stato possibile condividere la statistica globale con il figlio");
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
		SmartCity.LocalsGlobalsMessage msg = request.getLocalsglobalsUpdate();
		SmartCity.Node sender = msg.getSender();
		System.out.println("Thread " + Thread.currentThread().getId() + " Ricevo un aggregato dal nodo figlio: " + sender.getId());
		SmartCity.NodeMeasurement global = msg.getGlobal();
		List<SmartCity.NodeLocalStatistics> childsMeasurements = msg.getNodesLocalsList();
		GlobalStatistic globalSituation = GlobalStatistic.getInstance();
		globalSituation.addAggregatedGlobals(global);
		childsMeasurements.stream().forEach(measurement -> globalSituation.setAllLocalStatistics(measurement.getNode(), measurement.getLocalsList()));
		sendCurrentGlobalToChild(sender);
	}

	/**
	 * Questo metodo gestisce gli inviti a iniziare l'elezione ricevuti dai nodi che si sono accorti della mancanza del coordinatore.
	 *
	 * @param request
	 */
	public synchronized void manageElectionTime (SmartCity.MessageRequest request) {
		try {
			System.out.println("Thread " + Thread.currentThread().getId() + " Mi ha chiamato il nodo " + request.getNode().getId() + " per inizio elezione");
			DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
			SmartCity.MessageRequest response = SmartCity.MessageRequest.newBuilder().setNode(this.node).setTypemessage(SmartCity.MessageType.ELECTIONRECEIVED).build();
			byte[] respBytes = response.toByteArray();
			outputStream.writeInt(respBytes.length);
			outputStream.write(respBytes);
			System.out.println("Thread " + Thread.currentThread().getId() + " inizio elezione");
			NodeMain.startElection(this.node);
			System.out.println("Thread " + Thread.currentThread().getId() + "Termine elezione");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Errore :- si è verificato un errore nell'invio dell'ACK di elezione");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Errore :- si è verificato un errore generico nell'invio dell'ACK di elezione");
		}
	}

	/**
	 * Il metodo gestisce la ricezione dei risultati dell'elezione e la chiamata successiva al server
	 * in modo tale da conoscere il nuovo padre.
	 *
	 * @param request
	 */
	public synchronized void manageElectionResult (SmartCity.MessageRequest request) {
		System.out.println("Thread " + Thread.currentThread().getId() + " " + this.node.getId() + " è chiamato per la result di elezione");
		ElectionMutex.getInstance().enter();
		List<SmartCity.Node> allNodes = CityNodes.getInstance().getAllNodes();
		if (allNodes.stream().allMatch(n -> n.getId() < this.node.getId())) {
			NodeMain.broadcastElectionResult(this.node);
		} else {
			System.out.println("Thread " + Thread.currentThread().getId() + " " + this.node.getId() + " ha aggiornato il coordinatore in: " + request.getNode().getId());
			try {
				Coordinator.getInstance().setCoordinator(request.getNode());
				Coordinator.getInstance().setFatherNode(NodeMain.getNewFather(node));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Errore :- si è verificato un errore nell'aggiornamento del coordinatore e del nodo padre");
			}
		}
		System.out.println("Thread " + Thread.currentThread().getId() + " fine aggiornamento coordinatore");
		ElectionMutex.getInstance().exit();
		ElectionInProgressSemaphore.getInstance().exit();
	}
}
