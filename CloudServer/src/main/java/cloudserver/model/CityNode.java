package cloudserver.model;

import java.util.List;

public class CityNode {
    private Node node;
    private List<Measurement> measurements;

    public Node getNode(){
        return this.node;
    }

    public List<Measurement> getMeasurements(){
        return this.measurements;
    }

    public CityNode(Node node, List<Measurement> measurements){
        this.node = node;
        this.measurements = measurements;
    }
    public CityNode(){}

    @Override
    public String toString(){
        return this.node + ", Measurements: "+measurements;
    }
}
