package cloudserver.model;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {

    private List<CityNode> cityNodes;
    private List<SmartCity.NodeStatistic> globalStats;
    private static CityMap map;

    public synchronized static CityMap getInstance(){
        if(map==null){
            map = new CityMap();
        }
        return map;
    }

    private CityMap(){
        this.cityNodes = new Vector<CityNode>();
        this.globalStats = new Vector<SmartCity.NodeStatistic>();
    }

    public SmartCity.Nodes getNodes(){
        return cityNodes == null ? null : SmartCity.Nodes.newBuilder().addAllNodes(cityNodes.stream().map(nd -> nd.getNode()).collect(Collectors.toList())).build();
    }
    public synchronized void addNode(SmartCity.Node node, SmartCity.NodeStatistics measurements){
        this.cityNodes.add(new CityNode(node, measurements));
    }

    public synchronized void removeNode(int nodeId){
        this.cityNodes.removeIf(cn -> cn.getNode().getId()==nodeId);
    }
    public List<CityNode> getCityNodes(){
        return this.cityNodes;
    }

    public synchronized void addGlobal(SmartCity.NodeStatistic global){
        //TODO qua il synchronized va messo sulle globals
        if(global!=null){
            this.globalStats.add(global);
        }
    }
    public List<SmartCity.NodeStatistic> getGlobals(){
        return this.globalStats;
    }
    @Override
    public String toString(){
        return this.cityNodes.toString() + ", global statistics: "+globalStats;
    }
}
