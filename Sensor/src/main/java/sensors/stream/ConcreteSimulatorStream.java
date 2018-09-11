package sensors.stream;

import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lib.Measurement;
import lib.SensorStream;
import sensors.assembler.MeasurementAssembler;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

public class ConcreteSimulatorStream implements SensorStream {
	private SmartCity.Node node = null;
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
        /*int xPos = rand.nextInt(100);
        int yPos = rand.nextInt(100);*/
		//TODO questi dovrebbero essere generati casualmente
		this.xPos = 31;
		this.yPos = 44;
		this.serverHost = host;
		this.serverPort = port;
		updateCloserMethod();
		//TODO sistemare l'aggiornamento del nodo più vicino quando l'albero della città cambia
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
		Client client = Client.create();
		WebResource resource = client.resource("http://" + this.serverHost + ":" + this.serverPort + "/cloud-server/nodes").queryParams(params);
		ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
		if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
			byte[] nodeResp = response.getEntity(byte[].class);
			try {
				SmartCity.Node node = SmartCity.Node.parseFrom(nodeResp);
				this.node = node;
			} catch (Exception e) {
				this.node = null;
				System.out.println("Errore ricezione nodo vicino");
			}

		} else {
			this.node = null;
		}
		response.close();
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
		if (node != null) {
			while (retry > 0) {
				try {
					Socket connectionSocket = new Socket(node.getSelfIp(), node.getSensorsPort());
					DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());
					SmartCity.NodeMeasurement mToSend = MeasurementAssembler.assembleFrom(m);
					outStream.writeInt(mToSend.toByteArray().length);
					outStream.write(mToSend.toByteArray());
					System.out.println("Ho inviato: " + mToSend);
					connectionSocket.close();
					retry = 0;
				} catch (Exception e) {
					System.out.println("Si è verificato un errore nella comunicazione della misurazione");
					retry--;
				}
			}
		}
		updateCloserMethod();
	}
}
