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
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class ConcreteSimulatorStream implements SensorStream {
    private SmartCity.Node node = null;

    public ConcreteSimulatorStream(String host, int port){
        Random rand = new Random();
        /*int xPos = rand.nextInt(100);
        int yPos = rand.nextInt(100);*/
        int xPos = 57;
        int yPos = 68;
        Client client = Client.create();
        MultivaluedMap<String,String> params = new MultivaluedMapImpl();
        params.put("xcoord", Arrays.asList(xPos+""));
        params.put("ycoord",Arrays.asList(yPos+""));
        WebResource resource = client.resource("http://"+host+":"+port+"/cloud-server/nodes").queryParams(params);
        ClientResponse response = resource.accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
        if(response.getStatus() == ClientResponse.Status.OK.getStatusCode()){
            byte[] nodeResp = response.getEntity(byte[].class);
            try{
                SmartCity.Node node = SmartCity.Node.parseFrom(nodeResp);
                this.node = node;
                System.out.println("Nodo registrato: "+node);
            } catch(Exception e){
                System.out.println("Errore ricezione nodo vicino");
            }

        }
        response.close();
    }

    @Override
    public void sendMeasurement(Measurement m){
        //TODO inviare la misurazione al nodo edge più vicino
        if(node!=null){
            try{
                //TODO dovrei riprovare per un massimo di 10 volte dopo di che devo reInterrogare
                // il cloud server per conoscere un altro nodo più vicino
                System.out.println("Sto per inviare una misurazione");
                Socket connectionSocket = new Socket(node.getSelfIp(), node.getSensorsPort());
                DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());
                SmartCity.NodeMeasurement mToSend = MeasurementAssembler.assembleFrom(m);
                outStream.writeInt(mToSend.toByteArray().length);
                outStream.write(mToSend.toByteArray());
                System.out.println("Ho inviato: "+mToSend);
                connectionSocket.close();
            } catch(Exception e){
                System.out.println("Non è stato possibile trasmettere le misurazioni");
            }
        }
    }
}
