
package es.um.sisdist.backend.Service;

import java.util.List;
import java.util.Optional;

//import org.glassfish.jersey.server.Uri;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.AllConvsDTO;
import es.um.sisdist.models.AllConvsDTOUtils;
import es.um.sisdist.models.AutenticationDTO;
import es.um.sisdist.models.ConvDTO;
import es.um.sisdist.models.ConvDTOUtils;
import es.um.sisdist.models.ConvSummaryDTO;
import es.um.sisdist.models.PromptDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.models.UserStatsDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    @POST
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConv(@Context UriInfo uri, ConvDTO cDTO) {
        if(!impl.authenticateUser(cDTO.getUser(), cDTO.getDate(), uri.getPath(), cDTO.getAuthToken()))
            return Response.status(401).build();

        Optional<Conversation> conv = impl.createConversation(cDTO.getUser(), cDTO.getConvName());
        if (!conv.isPresent()) {
            return Response.status(Status.NO_CONTENT).build();
        }

        UriBuilder builder = UriBuilder.fromResource(UsersEndpoint.class).path("{id}/dialogue/{name}");
        Conversation c = conv.get();
        return Response.created(builder.build(cDTO.getUser(), c.getID())).entity(c).status(Status.CREATED).build();
    }

    @GET
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConvs(@Context UriInfo uri, AutenticationDTO aDTO) {
        //AllConvsDTO
        if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(), uri.getPath(), aDTO.getAuthToken())){
            return Response.status(401).build();
        }
        Optional<List<ConvSummaryDTO>> conv = impl.getConversations(aDTO.getUser());
        if (!conv.isPresent()) {
            return Response.ok(new AllConvsDTO()).build();
        }

        return Response.accepted(AllConvsDTOUtils.toDTO(conv.get())).build();
    }

    @GET
    @Path("{id}/stats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserStats(@Context UriInfo uri, AutenticationDTO aDTO) {
        if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(),uri.getPath(), aDTO.getAuthToken())){
            return Response.status(401).build();
        }
        Optional<UserStatsDTO> stats = impl.getUserStats(aDTO.getUser());
        if (stats.isPresent()) {
            return Response.ok().entity(stats.get()).build();
        }
        return Response.ok().entity(new UserStatsDTO()).build();
    }

    @GET
    @Path("{id}/dialogue/{convID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConvData(@PathParam("convID") String convID, @Context UriInfo uri, AutenticationDTO aDTO) {
        if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(), uri.getPath(), aDTO.getAuthToken()))
            return Response.status(401).build();
        Optional<Conversation> c = impl.getConversationData(aDTO.getUser(), convID);

        if (!c.isPresent()) {
            return Response.ok().entity(new ConvDTO()).build();
        }

        return Response.ok(ConvDTOUtils.toDTO(c.get())).build();
    }

    @POST
    @Path("{id}/dialogue/{convID}/end")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response endConversation(@PathParam("convID") String convID, @Context UriInfo uri, AutenticationDTO aDTO) {
        if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(), uri.getPath(), aDTO.getAuthToken()))
            return Response.status(401).build();

        boolean ended = impl.endConversation(aDTO.getUser(), convID);
        if (ended == true) {
            return Response.status(200).build();
        }
        return Response.status(500).build();
    }

    @DELETE
    @Path("{id}/dialogue/{convID}/del")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delConversation(@PathParam("convID") String convID, @Context UriInfo uri, AutenticationDTO aDTO) {
       if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(), uri.getPath(), aDTO.getAuthToken()))
            return Response.status(401).build();
        
        boolean deleted = impl.delConversation(aDTO.getUser(), convID);
        if (deleted == true) {
            return Response.status(200).build();
        }
        return Response.status(500).build();
    }

    @DELETE
    @Path("{id}/delAllConvs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dellAllConvs(@Context UriInfo uri, AutenticationDTO aDTO) {
        if(!impl.authenticateUser(aDTO.getUser(), aDTO.getDate(), uri.getPath(), aDTO.getAuthToken()))
            return Response.status(401).build();

        boolean deleted = impl.delAllConvs(aDTO.getUser());
        if (deleted == true) {
            return Response.status(200).build();
        }

        return Response.status(500).build();
    }

    @POST
    @Path("{id}/dialogue/{convID}/next/{timestamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPrompt(@Context UriInfo uri, PromptDTO pDTO) {
        if(!impl.authenticateUser(pDTO.getUser(), pDTO.getDate(), uri.getPath(), pDTO.getAuthToken())){
            return Response.status(401).build();
        }
        if (impl.isConvReady(pDTO.getUser(), pDTO.getConvID())) {
            Optional<Conversation> c = impl.sendPrompt(pDTO.getUser(), pDTO.getConvID(), pDTO.getPrompt(),
                    pDTO.getTimestamp());

            if (c.isPresent()) {
                return Response.status(200).entity(c.get()).build();
            }
            return Response.status(404).build();
        }

        return Response.status(Status.NO_CONTENT).build();
    }

}
