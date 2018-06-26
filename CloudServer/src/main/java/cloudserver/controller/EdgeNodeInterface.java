package cloudserver.controller;


import cloudserver.model.GroupMeasurements;
import cloudserver.model.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/nodes")
public class EdgeNodeInterface {

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

}
