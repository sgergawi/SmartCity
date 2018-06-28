package cloudserver.controller;


import cloudserver.model.*;
import cloudserver.utility.CloudServerUtility;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/cloud-server/nodes")
public class CloudServerInterfaces {
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getNodesState(@QueryParam("xcoord")Integer xPos, @QueryParam("ycoord")Integer yPos){
        CityMap map = CityMap.getInstance();
        SmartCity.Node nd = SmartCity.Node.newBuilder().setId(1234).setOtherNodesPort(234).setSensorsPort(234).setXPos(50).setXPos(70).setSelfIp("ciao").build();
        map.addNode(nd,null);
        if(xPos != null && yPos!=null) {
            //se vengono specificati vuol dire che mi sta chiamando un sensore quindi
            //vuole conoscere quali sono i nodi più vicini a lui
            List<SmartCity.Node> nodes = map.getNodes().getNodesList().stream().filter(node -> Math.abs(node.getXPos() - xPos) + Math.abs(node.getYPos() - yPos) < 20).sorted(new Comparator<SmartCity.Node>() {
                @Override
                public int compare(SmartCity.Node o1, SmartCity.Node o2) {
                    return (Math.abs(o1.getXPos() - xPos) + Math.abs(o1.getYPos() - yPos)) - (Math.abs(o2.getXPos() - xPos) + Math.abs(o2.getYPos() - yPos));
                }
            }).collect(Collectors.toList());
            if (nodes != null && !nodes.isEmpty()) {
                byte[] toSend = nodes.get(0).toByteArray();
                return Response.ok().entity(toSend).header(HttpHeaders.CONTENT_LENGTH, toSend.length).build();
            } else {
                return Response.status(404).build();
            }
        }
        List<SmartCity.Node> nodes = map.getNodes()!=null?map.getNodes().getNodesList():new Vector<>();
        if(nodes.isEmpty()){
            return Response.status(404).build();
        } else{
            return Response.ok().entity(map.getNodes().toByteArray()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response addNode(SmartCity.Node node){

        return Response.ok().build();
    }

    @DELETE
    @Path("/{nodeid}")
    public Response deleteNode(@PathParam("nodeid")int nodeId){
        System.out.println(nodeId);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/measurements")
    public Response getMeasurements(@DefaultValue("10")@QueryParam("n")int n){
        //ultime N statistiche GLOBALI E LOCALI DELLA CITTà
        return Response.ok().entity(null).build();
    }

    @Path("/measurements")
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response refreshMeasurements(){
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{nodeid}/measurements")
    public Response getNodeMeasurements(@DefaultValue("10")@QueryParam("n")int n, @PathParam("nodeid")int nodeId){
        //ultime N statistiche con timestamp prodotte da uno specifico nodo
        CityMap map = CityMap.getInstance();
        List<CityNode> nodes = map.getCityNodes();
        CityNode myNode = nodes!=null?nodes.stream().filter(nd -> nd.getNode().getId()==nodeId).findFirst().orElseGet(null):null;
        if(myNode==null || myNode.getNodeMeasurements()==null || myNode.getNodeMeasurements().getStatisticsList()==null){
            return Response.status(404).build();
        }
        List<SmartCity.NodeMeasurement> nodeMeasurements=myNode.getNodeMeasurements().getStatisticsList();
        List<SmartCity.NodeMeasurement> filtered = nodeMeasurements.size()>=n?nodeMeasurements.subList(0,n):nodeMeasurements;
        byte[] toSend = SmartCity.NodeMeasurements.newBuilder().addAllStatistics(filtered).build().toByteArray();
        return Response.ok().entity(toSend).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{nodeid}/statistics")
    public Response getNodeStats(@DefaultValue("10")@QueryParam("n")int n, @PathParam("nodeid")int nodeId){
        //deviazione std e media delle ultime N statistiche di UN NODO EDGE
        CityMap map = CityMap.getInstance();
        CityNode node = map.getCityNodes().stream().filter(citynode -> citynode.getNode().getId()==nodeId).findFirst().orElseGet(null);
        if(node.getNodeMeasurements()==null || node.getNodeMeasurements().getStatisticsList()==null || node.getNodeMeasurements().getStatisticsList().isEmpty()){
            return Response.status(404).build();
        }
        List<SmartCity.NodeMeasurement> ms = node.getNodeMeasurements().getStatisticsList().size()>=n?node.getNodeMeasurements().getStatisticsList().subList(0,n):node.getNodeMeasurements().getStatisticsList();
        List<Float> values = ms.stream().map(measure -> measure.getValue()).collect(Collectors.toList());
        float mean = values.stream().reduce((a,b)->a+b).get()/ms.size();
        double devstd = CloudServerUtility.getDevStd(values, mean);
        SmartCity.AggregatedStatistic stats = SmartCity.AggregatedStatistic.newBuilder().setDevstd(devstd).setMean(mean).build();
        return Response.ok().entity(stats).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/statistics")
    public Response getCityStats(@DefaultValue("10")@QueryParam("n")int n){
        //deviazione std e media delle ultime n statistiche GLOBALI della città
        CityMap map = CityMap.getInstance();

        return Response.ok().entity(null).build();
    }

}
