package edgenodes.controller;

import cloudserver.model.SmartCity;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SensorsDispatcher extends Thread {

    private SmartCity.Node node;
    private ServerSocket sensorsServerConnection;
    public SensorsDispatcher(SmartCity.Node node, ServerSocket connection){
        this.node = node;
        this.sensorsServerConnection = connection;
    }

    @Override
    public void run(){
        while(true){
            try{
                System.out.println("Connessione sensori in accensione...");
                Socket connectionSocket = sensorsServerConnection.accept();
                SensorCommunicationThread thread = new SensorCommunicationThread(node, connectionSocket);
                thread.start();
            } catch(IOException e){
                System.out.println("Errore nella comunicazione con un sensore");
            }

        }
    }
}
