import cloudserver.model.CityMap;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

public class ServerStarter {
    final static String HOST = "localhost";
    final static String PORT = "8480";
    public static void main(String[] args){
        try{
            HttpServer server = HttpServerFactory.create("http://"+HOST+":"+PORT+"/");
            /**
             * Per ora non ce n'è bisogno ma se volessi customizzarlo devo modificare questo pezzo
             */
            CityMap.setMaxChildsNum(3);
            server.start();
            System.out.println("Server running!");
            System.out.println("Hit return to stop...");
            System.in.read();
            System.out.println("Stopping server");
            server.stop(0);
            System.out.println("Server stopped");
        } catch(Exception e){
            System.out.println("Errore nella connessione");
        }
    }
}
