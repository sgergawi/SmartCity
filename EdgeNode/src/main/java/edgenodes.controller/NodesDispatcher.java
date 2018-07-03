package edgenodes.controller;

import cloudserver.model.SmartCity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodesDispatcher extends Thread{
    private ServerSocket nodesServerSocket;
    private SmartCity.Node node;

    public NodesDispatcher(SmartCity.Node node, ServerSocket connection){
        this.node = node;
        this.nodesServerSocket = connection;
    }

    @Override
    public void run(){
        while(true){
            try{
                System.out.println("Connessione con i nodi in accensione");
                Socket connectionSocket = nodesServerSocket.accept();
                NodeCommunicationThread thread = new NodeCommunicationThread(connectionSocket, node);
                thread.start();
            } catch(IOException e){
                System.out.println("Errore nella comunicazione con un nodo");
            }

        }
    }
}
