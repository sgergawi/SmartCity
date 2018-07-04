package edgenodes.model;

import cloudserver.model.SmartCity;
import edgenodes.utility.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class MeasurementsBuffer {

    private List<SmartCity.NodeMeasurement> measurementsBuffer;
    private List<SmartCity.NodeStatistic> globals;
    private static MeasurementsBuffer buffer;
    private MeasurementsBuffer(){
        this.measurementsBuffer = new Vector<>();
        this.globals = new Vector<>();
    }

    public synchronized static MeasurementsBuffer getInstance(){
        if(buffer==null){
            buffer = new MeasurementsBuffer();
        }
        return buffer;
    }

    public synchronized void addMeasurement(SmartCity.NodeMeasurement m){
        this.measurementsBuffer.add(m);
        this.measurementsBuffer.sort(Utility.getComparator());

        if(this.measurementsBuffer.size()>=40){
            System.out.println("Ho fatto più di 40 misurazioni");

            double sumOfValues = this.measurementsBuffer.subList(0,40).stream().map(a->a.getValue()).reduce((a,b)->a+b).orElse(0.);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            Long timestamp = c.getTimeInMillis();
            double mean = sumOfValues/40.;
            this.measurementsBuffer = this.measurementsBuffer.subList(20,40);
            if(Coordinator.getInstance().getCoordinator()!=null){
                SmartCity.Node coord = Coordinator.getInstance().getCoordinator();
                try{
                    Socket coordSocket = new Socket(coord.getSelfIp(),coord.getOtherNodesPort());
                    DataOutputStream outputStream = new DataOutputStream(coordSocket.getOutputStream());
                    SmartCity.NodeStatistic meanStatistic = SmartCity.NodeStatistic.newBuilder().setMean(mean).setTimestamp(timestamp).build();
                    SmartCity.MessageRequest messageToCoord = SmartCity.MessageRequest.newBuilder().setStatistic(meanStatistic).setTypemessage(SmartCity.MessageType.LOCALSTATISTIC).build();
                    byte[] statistic = messageToCoord.toByteArray();
                    outputStream.writeInt(statistic.length);
                    outputStream.write(statistic);

                    DataInputStream inputStream = new DataInputStream((coordSocket.getInputStream()));

                    byte[] globalstatics = new byte[inputStream.readInt()];
                    inputStream.read(globalstatics);
                    SmartCity.NodeStatistic global = SmartCity.NodeStatistic.parseFrom(globalstatics);
                    System.out.println("dal coord Statistica globale ricevuta: "+global);
                    globals.add(global);
                    coordSocket.close();
                } catch(Exception e){
                    System.out.println(e);
                    //TODO se mi accorgo che il nodo coordinatore non risponde allora indico l'elezione!!!
                }


            }
        }
    }



}
