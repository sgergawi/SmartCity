package cloudserver.model;

import cloudserver.utility.CloudServerUtility;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {

    private List<CityNode> cityNodes;
    private float globalStats;
    private static CityMap map;

    public synchronized static CityMap getInstance(){
        if(map==null){
            map = new CityMap();
        }
        return map;
    }

    private CityMap(){
        this.cityNodes = new Vector<CityNode>();
    }

    public SmartCity.Nodes getNodes(){
        return cityNodes == null ? null : SmartCity.Nodes.newBuilder().addAllNodes(cityNodes.stream().map(nd -> nd.getNode()).collect(Collectors.toList())).build();
    }

    public synchronized void addNode(SmartCity.Node node, SmartCity.NodeMeasurements measurements){
        this.cityNodes.add(new CityNode(node, measurements));
    }
    public List<CityNode> getCityNodes(){
        return this.cityNodes;
    }
    @Override
    public String toString(){
        return this.cityNodes.toString() + ", global statistics: "+globalStats;
    }
}
