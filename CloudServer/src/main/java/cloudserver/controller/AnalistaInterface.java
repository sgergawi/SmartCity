package cloudserver.controller;

import cloudserver.model.GroupMeasurements;
import cloudserver.model.Measurement;
import cloudserver.model.Node;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Vector;

@Path("/nodes")
public class AnalistaInterface {

    @GET
    @Produces("application/xml")
    public Response getCityStates(){
        return Response.ok().entity(new Vector<Node>()).build();
    }

    @GET
    @Produces("application/xml")
    @Path("/{nodeid}/measurements")
    public Response getNodeMeasurements(@PathParam("nodeid")int nodeId){
        return Response.ok().entity(new Vector<Measurement>()).build();
    }

    @GET
    @Produces("application/xml")
    @Path("/measurements")
    public Response getMeasurements(){
        return Response.ok().entity(new GroupMeasurements()).build();
    }
}
