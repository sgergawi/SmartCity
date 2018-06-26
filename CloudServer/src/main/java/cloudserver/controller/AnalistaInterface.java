package cloudserver.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/cloud-server")
public class AnalistaInterface {

    @GET
    @Path("/city-states")
    public Response getCityStates(){
        return Response.ok().entity("Ciao").build();
    }
}
