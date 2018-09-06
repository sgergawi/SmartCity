package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.model.Coordinator;
import edgenodes.model.GlobalStatistic;
import edgenodes.model.MajorNodes;
import edgenodes.model.MeasurementsBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class NodeCommunicationThread extends Thread {

    private Socket connection;
    private SmartCity.Node node;
    public NodeCommunicationThread(Socket connection, SmartCity.Node node){
        this.connection = connection;
        this.node = node;
    }

    /**
     * Quando un nodo riceve un messaggio da altri nodi deve decodificare il messaggio contenuto in esso
     * in base al protocollo prefissato. Per ogni messaggio vi è un codice iniziale che identifica la tipologia
     * della request.
     */
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
                case ELECTIONTIME:
                    manageElectionTime(request);
                    break;
                case ELECTIONRECEIVED:
                    System.out.println("ELECTION RECEIVED");
                    break;
                case ELECTIONRESULT:
                    manageElectionResult(request);
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
                MajorNodes.getInstance().addMajorThanMe(request.getNode());
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
        SmartCity.NodeStatistic statistic = request.getStatisticMsg().getStatistic();
        SmartCity.Node node = request.getStatisticMsg().getNode();
/*
        System.out.println("Aggiungo statistica locale: "+statistic);
*/
        GlobalStatistic.getInstance().addLocalStatistics(node,statistic);
/*
        System.out.println("Statistiche locali raccolte fino ad ora: "+GlobalStatistic.getInstance().getNodesLocals());
*/
        try{
/*
            System.out.println("Invio statistica globale: "+GlobalStatistic.getInstance().getGlobal());
*/
            DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
            SmartCity.NodeStatistic global = GlobalStatistic.getInstance().getGlobal();
            byte[] output = global==null?SmartCity.NodeStatistic.newBuilder().build().toByteArray():global.toByteArray();
            outputStream.writeInt(output.length);
            outputStream.write(output);
        } catch(Exception e){
            System.out.println("Non è stato possibile rispondere all'aggiornamento di statistiche");
        }
    }

    public void manageElectionTime(SmartCity.MessageRequest request){
        try{
            System.out.println("Mi ha chiamato il nodo "+request.getNode().getId());
            /*Socket socket = new Socket(request.getNode().getSelfIp(), request.getNode().getOtherNodesPort());*/
            DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
            SmartCity.MessageRequest response = SmartCity.MessageRequest.newBuilder().setNode(this.node).setTypemessage(SmartCity.MessageType.ELECTIONRECEIVED).build();
            byte[] respBytes=response.toByteArray();
            outputStream.writeInt(respBytes.length);
            outputStream.write(respBytes);
        } catch(Exception e){
            e.printStackTrace();
        }
        NodeMain.startElection(this.node);
    }

    public void manageElectionResult(SmartCity.MessageRequest request){
        System.out.println(this.node.getId()+" ha aggiornato il coordinatore");
        try{
            Coordinator.getInstance().setCoordinator(request.getNode());
            System.out.println("Coordinatore: "+Coordinator.getInstance().getCoordinator());
            if(node.getId()==Coordinator.getInstance().getCoordinator().getId()){
                GlobalStatisticsThread thread = new GlobalStatisticsThread(node);
                thread.start();
                thread.join();
            }
        } catch(Exception e){
            System.out.println("Errore nell'aggiornamento del coordinatore: "+e);
            NodeMain.deleteNodeServerSide(this.node);
        }

    }
}
