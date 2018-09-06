package sensors.stream;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import lib.PM10Simulator;

import javax.imageio.spi.ServiceRegistry;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;

public class ServerCommunication extends Thread {
    private ConcreteSimulatorStream concreteStream;
    public ServerCommunication(ConcreteSimulatorStream stream){
        this.concreteStream = stream;
    }

    @Override
    public void run(){
        while (true){
            try{
                Thread.sleep(10000);
                MultivaluedMap<String,String> params = new MultivaluedMapImpl();
                params.put("xcoord", Arrays.asList(this.concreteStream.getXPos()+""));
                params.put("ycoord",Arrays.asList(this.concreteStream.getYPos()+""));
                this.concreteStream.updateCloserNode(params);
            } catch(InterruptedException e){
                System.out.println("Si è verificato un errore durante l'aggiornamento del nodo più vicino");
            }

        }
    }
}
