package edgenodes.model;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.utility.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class MeasurementsBuffer {

    private List<SmartCity.NodeMeasurement> measurementsBuffer;
    private SmartCity.NodeStatistic global;
    private static MeasurementsBuffer buffer;
    private MeasurementsBuffer(){
        this.measurementsBuffer = new Vector<>();
    }

    public synchronized static MeasurementsBuffer getInstance(){
        if(buffer==null){
            buffer = new MeasurementsBuffer();
        }
        return buffer;
    }

    public synchronized SmartCity.NodeStatistic getLastGlobal(){
        /*return this.globals.stream().max(new Comparator<SmartCity.NodeStatistic>() {
            @Override
            public int compare(SmartCity.NodeStatistic o1, SmartCity.NodeStatistic o2) {
                Long firstTimestamp =o1.getTimestamp();
                Long secondTimestamp = o2.getTimestamp();
                return firstTimestamp.compareTo(secondTimestamp);
            }
        }).orElseGet(null);*/
        return this.global;
    }

    /**
     * Una volta ricevuta la misurazione, se sono state raccolte più di 40 misurazionioni, devono essere spedite al
     * nodo coordinatore e si deve utilizzare lo sliding window overlappato al 50% perciò le ultime 20 di queste 40 devono essere
     * tenute per il giro successivo. Se durante la comunicazione con il nodo coordinatore si nota che non è più attivo
     * si deve indire l'elezione.
     * @param m
     */
    public synchronized void addMeasurement(SmartCity.Node node, SmartCity.NodeMeasurement m){
        this.measurementsBuffer.add(m);
        this.measurementsBuffer.sort(Utility.getComparator());

        if(this.measurementsBuffer.size()>=40) {
            calculateOverlappedStats(node);
        }
    }

    private void calculateOverlappedStats(SmartCity.Node node) {
/*
        System.out.println("Ho fatto più di 40 misurazioni");
*/
        double sumOfValues = this.measurementsBuffer.subList(0,40).stream().map(a->a.getValue()).reduce((a,b)->a+b).orElse(0.);
        Long timestamp = Utility.generateTimestamp();
        double mean = sumOfValues/40.;
        this.measurementsBuffer = this.measurementsBuffer.subList(20,40);
        if(Coordinator.getInstance().getCoordinator()!=null){
            SmartCity.Node coord = Coordinator.getInstance().getCoordinator();
            try{
                //Apro la socket con il coordinatore
                Socket coordSocket = new Socket(coord.getSelfIp(),coord.getOtherNodesPort());
                //Utilizzo l'output stream per metterci la statistica locale calcolata da questo nodo cioè la media con il suo timestamp
                DataOutputStream outputStream = new DataOutputStream(coordSocket.getOutputStream());
                SmartCity.NodeStatistic meanStatistic = SmartCity.NodeStatistic.newBuilder().setMean(mean).setTimestamp(timestamp).build();
                SmartCity.NodeStatisticMessage statisticMessage = SmartCity.NodeStatisticMessage.newBuilder().setStatistic(meanStatistic).setNode(node).build();
                SmartCity.MessageRequest messageToCoord = SmartCity.MessageRequest.newBuilder().setStatisticMsg(statisticMessage).setTypemessage(SmartCity.MessageType.LOCALSTATISTIC).build();
                byte[] statistic = messageToCoord.toByteArray();
                outputStream.writeInt(statistic.length);
                outputStream.write(statistic);
/*
                System.out.println("Statistica locale inviata al coordinatore: "+meanStatistic);
*/

                //Voglio ricevere invece la statistica globale misurata dal nodo coordinatore
                DataInputStream inputStream = new DataInputStream((coordSocket.getInputStream()));
                byte[] globalstatics = new byte[inputStream.readInt()];
                inputStream.read(globalstatics);
                SmartCity.NodeStatistic global = SmartCity.NodeStatistic.parseFrom(globalstatics);
/*
                System.out.println("Statistica globale ricevuta dal coordinatore: "+global);
*/
                this.global=global;
                coordSocket.close();
            } catch(Exception e){
                System.out.println(e);
                System.out.println("Thread sta per indire elezione: "+Thread.currentThread().getId());
                NodeMain.startElection(node);
            }
        } else{
            NodeMain.startElection(node);
        }
    }


}
