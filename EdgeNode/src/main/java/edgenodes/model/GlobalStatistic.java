package edgenodes.model;

import cloudserver.model.SmartCity;
import edgenodes.utility.Utility;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GlobalStatistic {
    private HashMap<SmartCity.Node, List<SmartCity.NodeStatistic>> nodesLocals;
    private SmartCity.NodeStatistic global;
    private static GlobalStatistic instance;

    public static synchronized GlobalStatistic getInstance(){
        if(instance==null){
            instance= new GlobalStatistic();
        }
        return instance;
    }
    private GlobalStatistic(){
        this.nodesLocals = new HashMap<>();
    }

    public synchronized void addLocalStatistics(SmartCity.Node node, SmartCity.NodeStatistic statistic){
        if(nodesLocals.containsKey(node)){
            nodesLocals.get(node).add(statistic);
        } else{
            Vector<SmartCity.NodeStatistic> nodeStat = new Vector<>();
            nodeStat.add(statistic);
            nodesLocals.put(node,nodeStat);
        }
    }

    public synchronized void updateGlobal(){
        List<SmartCity.NodeStatistic> allLocals = new Vector<>();
        nodesLocals.values().forEach(allLocals::addAll);
        System.out.println("Tutte le medie raccolte dai vari nodi: "+nodesLocals);
        if(!nodesLocals.isEmpty()){
            Double sum = allLocals.stream().map(stat->stat.getMean()).reduce((a,b)->a+b).orElse(0.);
            global=SmartCity.NodeStatistic.newBuilder().setMean(sum/allLocals.size()).setTimestamp(Utility.generateTimestamp()).build();
        }
        System.out.println("Media globale aggiornata: "+global);
    }

    public SmartCity.NodeStatistic getGlobal(){
        return this.global;
    }

    public HashMap<SmartCity.Node, List<SmartCity.NodeStatistic>> getNodesLocals(){
        return this.nodesLocals;
    }

    public List<SmartCity.NodeLocalStatistics> getNodeslocalsMsg(){
        List<SmartCity.NodeLocalStatistics> msgs = new Vector<>();
        for (SmartCity.Node node: this.nodesLocals.keySet()){
            msgs.add(SmartCity.NodeLocalStatistics.newBuilder().setNode(node).addAllLocals(this.nodesLocals.get(node)).build());
        }
        return msgs;
    }
    public boolean isThereAnyLocal(){
        List<SmartCity.NodeStatistic> locals = new Vector<>();
        this.getNodesLocals().values().forEach(locals::addAll);
        return !locals.isEmpty();
    }
    public synchronized void clearLocals(){
        //TODO il syncronized andrebbe messo sulla nodesLocals e non sul metodo
        this.nodesLocals.clear();
    }
}
