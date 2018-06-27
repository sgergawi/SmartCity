package cloudserver.model;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CityMap {
    private List<CityNode> cityNodes;
    private float globalStats;
    private static CityMap cityMap;

    public synchronized List<Node> getNodes(){
        return cityNodes==null?new Vector<Node>():this.cityNodes.stream().map(CityNode::getNode).collect(Collectors.toList());
    }

    public synchronized static CityMap getInstance(){
        if(cityMap==null){
            cityMap = new CityMap();
        }
        return cityMap;
    }
}
