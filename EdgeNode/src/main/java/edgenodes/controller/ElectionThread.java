package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.model.Semaphore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.security.spec.EllipticCurve;

public class ElectionThread extends Thread {
    private SmartCity.Node majorNode;
    private SmartCity.MessageRequest msg;
    private Semaphore semaphore;
    public ElectionThread(SmartCity.MessageRequest msg, SmartCity.Node major, Semaphore semaphore){
        this.msg=msg;
        this.majorNode=major;
        this.semaphore = semaphore;
    }

    @Override
    public void run(){
        try{
            Socket nodeSocket = new Socket(this.majorNode.getSelfIp(),this.majorNode.getOtherNodesPort());
            DataOutputStream outputStream = new DataOutputStream(nodeSocket.getOutputStream());
            outputStream.writeInt(msg.toByteArray().length);
            outputStream.write(msg.toByteArray());
            System.out.println("Aspetto l'ok dal nodo "+this.majorNode.getId());

            DataInputStream inputStream = new DataInputStream(nodeSocket.getInputStream());
            byte[] response = new byte[inputStream.readInt()];
            inputStream.read(response);
            System.out.println(response);
            System.out.println("OK ricevuto: "+SmartCity.MessageRequest.parseFrom(response));
            this.semaphore.exit();
            nodeSocket.close();
        } catch(Exception e){
            System.out.println(e);
            System.out.println("Si è verificato un errore durante la comunicazione con il nodo "+this.majorNode.getId());
        }

    }
}
