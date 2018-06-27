package cloudserver.model;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {
    private List<CityNode> cityNodes;
    private float globalStats;
    private static CityMap cityMap;

    public synchronized Nodes getNodes(){
        return cityNodes==null?new Nodes():new Nodes(this.cityNodes.stream().map(CityNode::getNode).collect(Collectors.toList()));
    }

    public synchronized void addNode(Node node, List<Measurement> measurements){
        if(!this.cityNodes.isEmpty() && this.cityNodes.stream().allMatch(nd -> nd.getNode().getId()!=node.getId())){
            this.cityNodes.add(new CityNode(node, measurements));
        }
    }


    public synchronized static CityMap getInstance(){
        if(cityMap==null){
            cityMap = new CityMap();
        }
        return cityMap;
    }
    public CityMap(){ this.cityNodes = new Vector<>();}
    @Override
    public String toString(){
        return this.cityNodes.toString() + ", global statistics: "+globalStats;
    }
}
