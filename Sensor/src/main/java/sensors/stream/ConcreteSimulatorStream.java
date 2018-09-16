package sensors.stream;

import cloudserver.model.SmartCity;
import com.google.common.io.Closer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lib.Measurement;
import lib.SensorStream;
import sensors.assembler.MeasurementAssembler;
import sensors.model.CloserNode;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

public class ConcreteSimulatorStream implements SensorStream {
	//private SmartCity.Node node = null;
	private int xPos, yPos;
	private String serverHost;
	private int serverPort;

	/**
	 * Configura il sensore estraendo le due coordinate in modo casuale e successivamente tenta di collegarsi al
	 * cloudserver per richiedere il nodo più vicino a lui nella mappa.
	 *
	 * @param host
	 * @param port
	 */
	public ConcreteSimulatorStream (String host, int port) {
		Random rand = new Random();
		this.xPos = rand.nextInt(100);
		this.yPos = rand.nextInt(100);
		/*this.xPos = 3;
		this.yPos = 93;*/
		this.serverHost = host;
		this.serverPort = port;
		updateCloserMethod();
	}

	private void updateCloserMethod () {
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.put("xcoord", Arrays.asList(xPos + ""));
		params.put("ycoord", Arrays.asList(yPos + ""));
		updateCloserNode(params);
	}

	public int getXPos () {
		return this.xPos;
	}

	public int getYPos () {
		return this.yPos;
	}

	public synchronized void updateCloserNode (MultivaluedMap<String, String> params) {
		CloserNode.getInstance().lock();
		try {
			Client client = Client.create();
			WebResource resource = client.resource("http://" + this.serverHost + ":" + this.serverPort + "/cloud-server/nodes").queryParams(params);
			ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);

			if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
				byte[] nodeResp = response.getEntity(byte[].class);

				try {
					SmartCity.Node node = SmartCity.Node.parseFrom(nodeResp);
					CloserNode.getInstance().setNode(node);
				} catch (Exception e) {
					CloserNode.getInstance().setNode(null);
					System.out.println("Errore ricezione nodo vicino");
				}

			} else {
				CloserNode.getInstance().setNode(null);
			}

			response.close();
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore di connessione con il server");
		}
		CloserNode.getInstance().unlock();
	}

	/**
	 * Invia le misurazioni effettuate al nodo più vicino. Dovrebbe tentare per un massimo di 10 volte al termine delle
	 * quali, se sono stati riscontrati errori, dovrebbe richiedere al cloud server un nuovo nodo vicino con cui
	 * dialogare.
	 *
	 * @param m
	 */
	@Override
	public void sendMeasurement (Measurement m) {
		int retry = 10;
		CloserNode.getInstance().lock();
		if (CloserNode.getInstance().getNode() != null) {
			while (retry > 0) {
				try {
					Socket connectionSocket = new Socket(CloserNode.getInstance().getNode().getSelfIp(), CloserNode.getInstance().getNode().getSensorsPort());
					DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());
					SmartCity.NodeMeasurement mToSend = MeasurementAssembler.assembleFrom(m);
					outStream.writeInt(mToSend.toByteArray().length);
					outStream.write(mToSend.toByteArray());
					System.out.println("Ho inviato: " + mToSend);
					connectionSocket.close();
					retry = 0;
				} catch (Exception e) {
					System.out.println("Errore :- si è verificato un errore nella comunicazione della misurazione.");
					retry--;
				}
			}
		}
		CloserNode.getInstance().unlock();
		updateCloserMethod();
	}
}
