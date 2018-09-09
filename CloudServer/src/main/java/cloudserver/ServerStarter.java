import cloudserver.model.CityMap;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class ServerStarter {
	final static String HOST = "localhost";
	final static int PORT = 8480;

	public static void main (String[] args) {
		Server server = new Server(PORT);
		try {
			/*HttpServer server = HttpServerFactory.create("http://" + HOST + ":" + PORT + "/");
			 *//**
			 * Per ora non ce n'è bisogno ma se volessi customizzarlo devo modificare questo pezzo
			 *//*
			CityMap.setMaxChildsNum(3);
			server.start();*/
			ServletContextHandler ctx =
					new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
			ctx.setContextPath("/");
			server.setHandler(ctx);

			ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/cloud-server/*");
			serHol.setInitOrder(1);
			serHol.setInitParameter("jersey.config.server.provider.packages",
					"cloudserver.controller");
			System.out.println("Server running!");
			System.out.println("Hit return to stop...");
			System.in.read();
			System.out.println("Stopping server");
			CityMap.setMaxChildsNum(3);
			server.start();
			server.join();
			System.out.println("Server stopped");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Errore nella connessione");
		} finally {
			server.destroy();
		}
	}
}
