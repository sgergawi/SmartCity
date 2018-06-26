package cloudserver.controller;


import cloudserver.model.GroupMeasurements;
import cloudserver.model.Measurement;
import cloudserver.model.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Vector;

@Path("/nodes")
public class CloudServerInterfaces {
    @GET
    @Produces("application/xml")
    public Response getCityStates(){
        return Response.ok().entity(new Vector<Node>()).build();
    }

    @POST
    @Consumes("application/xml")
    public Response addNode(Node node){
        System.out.println(node);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{nodeid}")
    public Response deleteNode(@PathParam("nodeid")int nodeId){
        System.out.println(nodeId);
        return Response.ok().build();
    }

    @GET
    @Produces("application/xml")
    @Path("/measurements")
    public Response getMeasurements(){
        return Response.ok().entity(new GroupMeasurements()).build();
    }

    @Path("/measurements")
    @POST
    @Consumes("application/xml")
    public Response refreshMeasurements(GroupMeasurements measurements){
    return Response.ok().build();
    }

    @GET
    @Produces("application/xml")
    public Response getClosestNode(@QueryParam("xcoord")int xPos, @QueryParam("ycoord")int yPos){
        return Response.ok(new Node()).build();
    }

    @GET
    @Produces("application/xml")
    @Path("/{nodeid}/measurements")
    public Response getNodeMeasurements(@PathParam("nodeid")int nodeId){
        return Response.ok().entity(new Vector<Measurement>()).build();
    }

}
