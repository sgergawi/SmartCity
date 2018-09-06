package edgenodes.controller;

import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import edgenodes.NodeMain;
import edgenodes.model.GlobalStatistic;
import edgenodes.model.MeasurementsBuffer;

public class GlobalStatisticsThread extends Thread {
    private SmartCity.Node node;
    public GlobalStatisticsThread(SmartCity.Node node){
        this.node = node;
    }

    @Override
    public void run(){
        while(true){
            try{
                Thread.sleep(5000);
                GlobalStatistic.getInstance().updateGlobal();
                NodeMain.sendGlobalStatisticsToServer();
                GlobalStatistic.getInstance().clearLocals();
            } catch(InterruptedException e){
                System.out.println("Il calcolo della statistica globale è stato interrotto");
            }

        }
    }
}
