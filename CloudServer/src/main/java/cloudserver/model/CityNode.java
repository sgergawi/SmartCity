package cloudserver.model;

import java.util.List;
import java.util.Vector;

public class CityNode {
    private SmartCity.Node node;
    private List<CityNode> childNodes;
    private SmartCity.NodeStatistics nodeStatistics;

    public SmartCity.Node getNode(){
        return this.node;
    }
    public SmartCity.NodeStatistics getNodeStatistics(){
        return this.nodeStatistics;
    }

    public CityNode(SmartCity.Node node, SmartCity.NodeStatistics statistics){
        this.node = node;
        this.nodeStatistics = statistics;
        this.childNodes = new Vector<>();
    }
    public CityNode(){}

    public void addAllStatistics(List<SmartCity.NodeStatistic> stats){
        List<SmartCity.NodeStatistic> currentStats = this.nodeStatistics.getStatisticsList();
        this.nodeStatistics = SmartCity.NodeStatistics.newBuilder().addAllStatistics(currentStats).addAllStatistics(stats).build();
    }

    public List<CityNode> getChildNodes(){
        return this.childNodes;
    }
    @Override
    public String toString(){
        return this.node + ", Statistics: "+ nodeStatistics;
    }
}
