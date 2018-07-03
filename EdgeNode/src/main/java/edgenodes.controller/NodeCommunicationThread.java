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
            DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
            SmartCity.HelloRequest request = SmartCity.HelloRequest.parseFrom(message);
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
            this.connection.close();
        } catch(Exception e){
            System.out.println("Errore nel parsing del messaggio");
        }

    }
}
