package cloudserver.controller;


import cloudserver.model.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;
import java.math.*;
import java.util.stream.Collectors;

@Path("/cloud-server/nodes")
public class CloudServerInterfaces {
    @GET
    @Produces({"application/json","application/xml"})
    public Response getNodesState(@QueryParam("xcoord")Integer xPos, @QueryParam("ycoord")Integer yPos){
        CityMap map = CityMap.getInstance();
        if(xPos != null && yPos!=null){
            //se vengono specificati vuol dire che mi sta chiamando un sensore quindi
            //vuole conoscere quali sono i nodi più vicini a lui

            List<Node> nodes = map.getNodes().getNodes().stream().filter(node -> Math.abs(node.getxPos()-xPos)+Math.abs(node.getyPos()-yPos)<20).sorted(new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return (Math.abs(o1.getxPos()-xPos)+Math.abs(o1.getyPos()-yPos)) - (Math.abs(o2.getxPos()-xPos)+Math.abs(o2.getyPos()-yPos));
                }
            }).collect(Collectors.toList());

            if(nodes!=null && !nodes.isEmpty()){
                return Response.ok().entity(nodes!=null && !nodes.isEmpty()?nodes.get(0):null).build();
            } else{
                return Response.status(404).build();
            }
        }
        return Response.ok().entity(CityMap.getInstance().getNodes()).build();
    }

    @POST
    @Consumes({"application/xml","application/json"})
    public Response addNode(Node node){

        return Response.ok().build();
    }

    @DELETE
    @Path("/{nodeid}")
    public Response deleteNode(@PathParam("nodeid")int nodeId){
        System.out.println(nodeId);
        return Response.ok().build();
    }

    @GET
    @Produces({"application/xml","application/json"})
    @Path("/measurements")
    public Response getMeasurements(@DefaultValue("10")@QueryParam("n")int n){
        //ultime N statistiche GLOBALI E LOCALI DELLA CITTà
        return Response.ok().entity(new GroupMeasurements()).build();
    }

    @Path("/measurements")
    @POST
    @Consumes({"application/xml","application/json"})
    public Response refreshMeasurements(GroupMeasurements measurements){
        return Response.ok().build();
    }

    @GET
    @Produces({"application/xml","application/json"})
    @Path("/{nodeid}/measurements")
    public Response getNodeMeasurements(@DefaultValue("10")@QueryParam("n")int n, @PathParam("nodeid")int nodeId){
        //ultime N statistiche con timestamp prodotte da uno specifico nodo
        return Response.ok().entity(new Vector<Measurement>()).build();
    }

    @GET
    @Produces({"application/xml","application/json"})
    @Path("/{nodeid}/statistics")
    public Response getNodeStats(@DefaultValue("10")@QueryParam("n")int n, @PathParam("nodeid")int nodeId){
        //deviazione std e media delle ultime N statistiche di UN NODO EDGE
        return Response.ok().entity(new cloudserver.model.Statistic()).build();
    }

    @GET
    @Produces({"application/xml","application/json"})
    @Path("/statistics")
    public Response getNodeStats(@DefaultValue("10")@QueryParam("n")int n){
        //deviazione std e media delle ultime n statistiche GLOBALI della città
        return Response.ok().entity(new Statistic()).build();
    }

}
