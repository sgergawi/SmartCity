package analista.controller;

import analista.utility.AnalistaUtility;
import cloudserver.model.SmartCity;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

public class AnalystController {
	private final String CLOUDHOST = "http://localhost";
	private final int CLOUDPORT = 8480;
	private final String ROOT = "/cloud-server/nodes";

	public void getCityState () {
		try {
			Client client = Client.create();
			WebResource webResource = client.resource(CLOUDHOST + ":" + CLOUDPORT + ROOT);
			ClientResponse response = webResource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if (response.getStatus() == 200) {
				byte[] output = response.getEntity(byte[].class);
				SmartCity.Nodes nodes = SmartCity.Nodes.parseFrom(output);
				System.out.println("Stato attuale della città: \n" + nodes);
			} else if (response.getStatus() == 404) {
				System.out.println("Dati non trovati.");
			} else {
				System.out.println("Errore :- Si è verificato un errore nell'elaborazione della richiesta.");
			}
			response.close();
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore :- si è verificato un errore nel parsing del messaggio ricevuto.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione con il server.");
		}

	}

	public void getEdgeNodeStats () {

		try {
			Scanner scanner = new Scanner(System.in);
			int statsNumber = AnalistaUtility.getStatsNumber(scanner);
			System.out.print("Inserire l'id del nodo edge: ");
			int edgeId = scanner.nextInt();

			Client client = Client.create();
			WebResource resource = client.resource(CLOUDHOST + ":" + CLOUDPORT + ROOT + "/" + edgeId + "/measurements").queryParam("n", "" + statsNumber);
			ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if (AnalistaUtility.ifOKResponse(response)) {
				byte[] output = response.getEntity(byte[].class);
				SmartCity.NodeMeasurements ms = SmartCity.NodeMeasurements.parseFrom(output);
				System.out.println("Statistiche del nodo " + edgeId + ": " + ms);
			} else if (response.getStatus() == 404) {
				System.out.println("Dati non trovati");
			} else {
				System.out.println("Errore :- Si è verificato un errore nell'elaborazione della richiesta.");
			}
			response.close();
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore :- si è verificato un errore nel parsing del messaggio ricevuto.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione con il server.");
		}

	}

	public void getGlobalAndLocalStats () {
		try {
			Scanner scanner = new Scanner(System.in);
			int statsNumber = AnalistaUtility.getStatsNumber(scanner);
			Client client = Client.create();
			WebResource resource = client.resource(CLOUDHOST + ":" + CLOUDPORT + ROOT + "/measurements").queryParam("n", "" + statsNumber);
			ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if (AnalistaUtility.ifOKResponse(response)) {
				byte[] output = response.getEntity(byte[].class);
				SmartCity.LastLocalsGlobals localsglobals = SmartCity.LastLocalsGlobals.parseFrom(output);
				System.out.println("Statistiche globali e locali: \n" + localsglobals);
			} else if (response.getStatus() == 404) {
				System.out.println("Dati non trovati");
			} else {
				System.out.println("Errore :- Si è verificato un errore nell'elaborazione della richiesta.");
			}
			response.close();
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore :- si è verificato un errore nel parsing del messaggio ricevuto.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione con il server.");
		}
	}

	public void getStdDevMeanSingleNode () {
		try {
			Scanner scanner = new Scanner(System.in);
			int statsNumber = AnalistaUtility.getStatsNumber(scanner);
			System.out.print("Inserire l'ID del nodo edge interessato: ");
			int nodeId = scanner.nextInt();
			Client client = Client.create();
			WebResource resource = client.resource(CLOUDHOST + ":" + CLOUDPORT + ROOT + "/" + nodeId + "/statistics").queryParam("n", "" + statsNumber);
			ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if (AnalistaUtility.ifOKResponse(response)) {
				byte[] output = response.getEntity(byte[].class);
				SmartCity.AggregatedStatistic aggregate = SmartCity.AggregatedStatistic.parseFrom(output);
				System.out.println("Deviazione std e media delle N statistiche del nodo " + nodeId + ": " + aggregate);
			} else if (response.getStatus() == 404) {
				System.out.println("Dati non trovati");
			} else {
				System.out.println("Errore :- Si è verificato un errore nell'elaborazione della richiesta.");
			}
			response.close();
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore :- si è verificato un errore nel parsing del messaggio ricevuto.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione con il server.");
		}
	}

	public void getStdDevMeanNodes () {
		try {
			Scanner scanner = new Scanner(System.in);
			int statsNumber = AnalistaUtility.getStatsNumber(scanner);
			Client client = Client.create();
			WebResource resource = client.resource(CLOUDHOST + ":" + CLOUDPORT + ROOT + "/statistics").queryParam("n", "" + statsNumber);
			ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
			if (AnalistaUtility.ifOKResponse(response)) {
				byte[] output = response.getEntity(byte[].class);
				SmartCity.AggregatedStatistic aggregate = SmartCity.AggregatedStatistic.parseFrom(output);
				System.out.println("Deviazione std e media delle N statistiche: " + aggregate);
			} else if (response.getStatus() == 404) {
				System.out.println("Dati non trovati");
			} else {
				System.out.println("Errore :- Si è verificato un errore nell'elaborazione della richiesta.");
			}
			response.close();
		} catch (InvalidProtocolBufferException e) {
			System.out.println("Errore :- si è verificato un errore nel parsing del messaggio ricevuto.");
		} catch (Exception e) {
			System.out.println("Errore :- si è verificato un errore generico nella comunicazione con il server.");
		}
	}
}
