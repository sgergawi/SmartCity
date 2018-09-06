package edgenodes.controller;

import cloudserver.model.SmartCity;

import java.io.DataOutputStream;
import java.net.Socket;

public class ElectionBroadcastResultThread extends Thread {
    private SmartCity.Node newCoordinator, nodeToBeCalled;
    private SmartCity.MessageRequest msg;
    public ElectionBroadcastResultThread(SmartCity.MessageRequest msg, SmartCity.Node newCoordinator, SmartCity.Node nodeToBeCalled){
        this.msg = msg;
        this.newCoordinator=newCoordinator;
        this.nodeToBeCalled=nodeToBeCalled;
    }

    @Override
    public void run(){
        try{
            Socket socket = new Socket(nodeToBeCalled.getSelfIp(), nodeToBeCalled.getOtherNodesPort());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(msg.toByteArray().length);
            outputStream.write(msg.toByteArray());
            socket.close();
        } catch(Exception e){
            System.out.println("Errore nella comunicazione del coordinatore al nodo "+this.nodeToBeCalled.getId());
        }
    }
}
