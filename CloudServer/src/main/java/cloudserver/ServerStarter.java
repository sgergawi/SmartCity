package cloudserver;

import cloudserver.controller.CloudServerInterfaces;
import cloudserver.model.CityMap;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ext.RuntimeDelegate;
import java.net.URI;

public class ServerStarter {
	final static String HOST = "localhost";
	final static int PORT = 8480;

	public static void main (String[] args) {
		try {
			URI baseUri = new URI("http://localhost:" + PORT + "/");
			ResourceConfig rc = new ResourceConfig();
			rc.registerClasses(CloudServerInterfaces.class);

			final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
					baseUri,
					rc,
					false);
			CityMap.setMaxChildsNum(3);
			System.out.println("Starting server...");
			server.start();
			System.out.println("Server running!");
			System.out.println("Press any button to stop server...");
			System.in.read();
			server.shutdownNow();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Errore nella connessione");
			System.exit(1);
		}
	}

}

