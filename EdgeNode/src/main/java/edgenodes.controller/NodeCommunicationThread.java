package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.Coordinator;
import edgenodes.model.MajorNodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class NodeCommunicationThread extends Thread {

    private Socket connection;
    private SmartCity.Node node;
    public NodeCommunicationThread(Socket connection, SmartCity.Node node){
        this.connection = connection;
        this.node = node;
    }

    @Override
    public void run(){
        try{
            DataInputStream inputStream = new DataInputStream(this.connection.getInputStream());
            byte[] message = new byte[inputStream.readInt()];
            inputStream.read(message,0,message.length);

            SmartCity.MessageRequest request = SmartCity.MessageRequest.parseFrom(message);
            SmartCity.MessageType type = request.getTypemessage();
            switch(type){
                case HELLO:
                    manageHelloRequest(request);
                    break;
                case LOCALSTATISTIC:
                    manageStatisticUpdate(request);
                    break;
                default:
                    System.out.println("Errore :- message type non riconosciuto");
                    break;
            }

            this.connection.close();
        } catch(Exception e){
            System.out.println("Errore nel parsing del messaggio");
        }

    }

    public void manageHelloRequest(SmartCity.MessageRequest request){
        try{
            DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
            if(request.getNode().getId()>this.node.getId()){
                MajorNodes.getInstance().addMajorThanMe(this.node);
            }
            SmartCity.HelloResponse response = SmartCity.HelloResponse.newBuilder().setTypemessage(SmartCity.MessageType.WELCOME).build();
            if(Coordinator.getInstance().getCoordinator().getId()==this.node.getId()){
                response = response.toBuilder().setIscoordinator(true).build();
            } else{
                response = response.toBuilder().setIscoordinator(false).build();
            }
            byte[] output = response.toByteArray();
            outputStream.writeInt(output.length);
            outputStream.write(output);
        } catch(Exception e){
            System.out.println("Errore durante la hello request");
        }

    }

    public void manageStatisticUpdate(SmartCity.MessageRequest request){
        SmartCity.NodeStatistic statistic = request.getStatistic();
        try{
            DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
            byte[] output = SmartCity.NodeStatistic.newBuilder().setMean(30.).setTimestamp(30).build().toByteArray();
            outputStream.writeInt(output.length);
            outputStream.write(output);
        } catch(Exception e){
            System.out.println("Non è stato possibile rispondere all'aggiornamento di statistiche");
        }
    }
}
