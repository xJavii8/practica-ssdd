
package es.um.sisdist.backend.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.AllConvsDTO;
import es.um.sisdist.models.AllConvsDTOUtils;
import es.um.sisdist.models.ChangeUserInfoDTO;
import es.um.sisdist.models.ConvDTO;
import es.um.sisdist.models.ConvDTOUtils;
import es.um.sisdist.models.ConversationSummary;
import es.um.sisdist.models.PromptDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username) {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserDTO uo) {
        Optional<User> u = impl.createUser(uo.getEmail(), uo.getName(), uo.getPassword());
        if (u.isPresent()) {
            // Mejor retonar objeto completo que solo el id para no tener llamadas de más a
            // la API.
            return Response.ok(UserDTOUtils.toDTO(u.get())).build();
        } else {
            // Error 409 si email ya existe.
            return Response.status(Status.CONFLICT).build();
        }
    }

    @POST
    @Path("/changeInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeInfoUser(ChangeUserInfoDTO cuio) {
        Optional<User> u = impl.modifyUser(cuio.getActualEmail(), cuio.getNewMail(), cuio.getName(),
                cuio.getPassword());
        if (u.isPresent()) {
            return Response.ok(UserDTOUtils.toDTO(u.get())).build();
        } else {
            return Response.status(Status.NO_CONTENT).build();
        }
    }

    @DELETE
    @Path("/deleteUser/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") String id) {
        boolean deleted = impl.deleteUser(id);
        if (deleted) {
            return Response.status(Status.OK).build();
        } else {
            return Response.status(Status.NO_CONTENT).build();
        }
    }

    @POST
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createConv(@PathParam("id") String id, ConvDTO cDTO) {
        
        Optional<Conversation> conv = impl.createConversation(id, cDTO.getConvName());
        if (!conv.isPresent()) {
            return Response.status(Status.NO_CONTENT).build();
        }

        UriBuilder builder = UriBuilder.fromResource(UsersEndpoint.class).path("{id}/dialogue/{name}");
        String convID = conv.get().getID();
        Map<String, String> convJSON = new HashMap<>();
        convJSON.put("convID", convID);
        return Response.created(builder.build(id, convID)).entity(convJSON).status(Status.CREATED).build();
    }

    @GET
    @Path("{id}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AllConvsDTO getConvs(@PathParam("id") String id) {
        
        Optional<List<ConversationSummary>> conv = impl.getConversations(id);
        if (!conv.isPresent()) {
            return new AllConvsDTO();
        }

        return AllConvsDTOUtils.toDTO(conv.get());
    }

    @GET
    @Path("{id}/dialogue/{convID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConvDTO getConvData(@PathParam("id") String id, @PathParam("convID") String convID) {
        
        Optional<Conversation> c = impl.getConversationData(id, convID);
        if (!c.isPresent()) {
            return new ConvDTO();
        }

        return ConvDTOUtils.toDTO(c.get());
    }

    @POST
    @Path("{id}/dialogue/{convID}/end")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public boolean endConversation(@PathParam("id") String id, @PathParam("convID") String convID) {     
        return impl.endConversation(id, convID);
    }

    /*@POST
    @Path("/sendPrompt")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendPrompt(PromptDTO pDTO) {
        Optional<Conversation> conv = impl.createConversation(pDTO.getQuestion());
        if (!conv.isPresent()) {
            return Response.status(Status.NO_CONTENT).build();
        }

        return Response.status(Status.OK).build();
    }*/

    

}
