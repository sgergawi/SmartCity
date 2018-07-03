package edgenodes.model;

import cloudserver.model.SmartCity;

import java.util.List;
import java.util.Vector;

public class MeasurementsBuffer {

    private int nMeasurements = 0;
    private List<SmartCity.NodeMeasurement> measurementsBuffer;
    private List<SmartCity.AggregatedStatistic> statistics;
    private static MeasurementsBuffer buffer;
    private MeasurementsBuffer(){
        this.measurementsBuffer = new Vector<>();
        this.statistics = new Vector<>();
    }

    public synchronized static MeasurementsBuffer getInstance(){
        if(buffer==null){
            buffer = new MeasurementsBuffer();
        }
        return buffer;
    }

    public synchronized void addMeasurement(SmartCity.NodeMeasurement m){
        this.measurementsBuffer.add(m);
        nMeasurements++;
        if(nMeasurements>=40){
            //TODO devo fare le statistica dei 40 e cancellare i 20 più vecchi
            //TODO dopo aver calcolato la statistica la devo trasmettere al nodo edge coordinatore
            System.out.println("Ho fatto più di 40 misurazioni");
        }
    }



}
