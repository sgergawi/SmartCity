package cloudserver.model;

import java.util.List;

public class CityNode {
    private SmartCity.Node node;
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
    }
    public CityNode(){}

    public void addAllStatistics(List<SmartCity.NodeStatistic> stats){
        List<SmartCity.NodeStatistic> currentStats = this.nodeStatistics.getStatisticsList();
        this.nodeStatistics = SmartCity.NodeStatistics.newBuilder().addAllStatistics(currentStats).addAllStatistics(stats).build();
    }
    @Override
    public String toString(){
        return this.node + ", Statistics: "+ nodeStatistics;
    }
}
