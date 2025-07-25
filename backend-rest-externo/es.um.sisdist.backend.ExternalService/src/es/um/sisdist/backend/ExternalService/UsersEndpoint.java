
package es.um.sisdist.backend.ExternalService;

import java.util.List;
import java.util.Optional;

import es.um.sisdist.backend.ExternalService.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.AllConvsDTO;
import es.um.sisdist.models.AllConvsDTOUtils;
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
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
        } else {
            return new UserDTO();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserDTO uo) {
        Optional<User> u = impl.createUser(uo.getEmail(), uo.getName(), uo.getPassword());

        if (u.isPresent()) {
            return Response.ok(UserDTOUtils.toDTO(u.get())).build();
        } else {
            return Response.status(Status.CONFLICT).build();
        }
    }

    @DELETE
    @Path("/deleteUser/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") String id, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            boolean deleted = impl.deleteUser(id);

            if (deleted) {
                return Response.status(Status.OK).build();
            } else {
                return Response.status(Status.NO_CONTENT).build();
            }
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConv(@PathParam("id") String id, ConvDTO cDTO, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            Optional<Conversation> conv = impl.createConversation(id, cDTO.getConvName());

            if (!conv.isPresent()) {
                return Response.status(Status.NO_CONTENT).build();
            }

            UriBuilder builder = UriBuilder.fromResource(UsersEndpoint.class).path("{id}/dialogue/{name}");
            Conversation c = conv.get();
            return Response.created(builder.build(id, c.getID())).entity(c).status(Status.CREATED).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AllConvsDTO getConvs(@PathParam("id") String id, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {

            Optional<List<ConvSummaryDTO>> conv = impl.getConversations(id);
            if (!conv.isPresent()) {
                return new AllConvsDTO();
            }

            return AllConvsDTOUtils.toDTO(conv.get());
        } else {
            return new AllConvsDTO();
        }
    }

    @GET
    @Path("{id}/stats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UserStatsDTO getUserStats(@PathParam("id") String id, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            Optional<UserStatsDTO> stats = impl.getUserStats(id);

            if (stats.isPresent()) {
                return stats.get();
            }

        }
        return new UserStatsDTO();
    }

    @GET
    @Path("{id}/dialogue/{convID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConvDTO getConvData(@PathParam("id") String id, @PathParam("convID") String convID, @Context UriInfo ui,
            @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            Optional<Conversation> c = impl.getConversationData(id, convID);

            if (!c.isPresent()) {
                return new ConvDTO();
            }

            return ConvDTOUtils.toDTO(c.get());
        } else {
            return new ConvDTO();
        }
    }

    @POST
    @Path("{id}/dialogue/{convID}/end")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response endConversation(@PathParam("id") String id, @PathParam("convID") String convID, @Context UriInfo ui,
            @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            boolean ended = impl.endConversation(id, convID);

            if (ended == true) {
                return Response.status(Status.OK).build();
            }

            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @DELETE
    @Path("{id}/dialogue/{convID}/del")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delConversation(@PathParam("id") String id, @PathParam("convID") String convID, @Context UriInfo ui,
            @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            boolean deleted = impl.delConversation(id, convID);

            if (deleted == true) {
                return Response.status(Status.OK).build();
            }

            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @DELETE
    @Path("{id}/delAllConvs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delAllConvs(@PathParam("id") String id, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            boolean deleted = impl.delAllConvs(id);

            if (deleted == true) {
                return Response.status(Status.OK).build();
            }

            return Response.status(Status.NO_CONTENT).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("{id}/dialogue/{convID}/next/{timestamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPrompt(PromptDTO pDTO, @Context UriInfo ui, @Context HttpHeaders hh) {
        MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
        String URI = ui.getRequestUri().toString();
        String userHeader = headerParams.getFirst("user");
        String dateHeader = headerParams.getFirst("date");
        String authTokenHeader = headerParams.getFirst("auth-token");

        boolean auth = impl.checkUserAuth(userHeader, dateHeader, authTokenHeader, URI);

        if (auth) {
            if (impl.isConvReady(pDTO.getUserID(), pDTO.getConvID())) {
                Optional<Conversation> c = impl.sendPrompt(pDTO.getUserID(), pDTO.getConvID(), pDTO.getPrompt(),
                        pDTO.getTimestamp());

                if (c.isPresent()) {
                    return Response.status(Status.OK).entity(c.get()).build();
                }
                return Response.status(Status.NOT_FOUND).build();
            }

            return Response.status(Status.NO_CONTENT).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

}
