package edgenodes.model;

import cloudserver.model.SmartCity;

public class Coordinator {

    private SmartCity.Node coordinatorNode = null;
    private static Coordinator coord;

    public static Coordinator getInstance(){
        if(coord==null){
            coord = new Coordinator();
        }
        return coord;
    }
    private Coordinator(){}

    public synchronized SmartCity.Node getCoordinator(){
        return coordinatorNode;
    }
    public synchronized void setCoordinator(SmartCity.Node node){
        this.coordinatorNode = node;
    }

    @Override
    public String toString(){
        return this.coordinatorNode!=null?this.coordinatorNode.toString():null;
    }

}
