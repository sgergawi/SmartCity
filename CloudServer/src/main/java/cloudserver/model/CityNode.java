package cloudserver.model;

public class CityNode {
    private SmartCity.Node node;
    private SmartCity.NodeMeasurements nodeMeasurements;

    public SmartCity.Node getNode(){
        return this.node;
    }
    public SmartCity.NodeMeasurements getNodeMeasurements(){
        return this.nodeMeasurements;
    }

    public CityNode(SmartCity.Node node, SmartCity.NodeMeasurements measurements){
        this.node = node;
        this.nodeMeasurements = measurements;
    }
    public CityNode(){}

    @Override
    public String toString(){
        return this.node + ", Measurements: "+nodeMeasurements;
    }
}
