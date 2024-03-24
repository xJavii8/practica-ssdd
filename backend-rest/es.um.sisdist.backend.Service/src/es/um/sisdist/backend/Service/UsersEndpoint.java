
package es.um.sisdist.backend.Service;

import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.ChangeUserInfoDTO;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(UserDTO uo){
        Optional<User> u = impl.createUser(uo.getEmail(), uo.getName(), uo.getPassword());
        if (u.isPresent()){
            //Mejor retonar objeto completo que solo el id para no tener llamadas de más a la API.
            return Response.ok(UserDTOUtils.toDTO(u.get())).build();
        }else{
            //Error 409 si email ya existe.
            return Response.status(Status.CONFLICT).build();
        }
    }

    @POST
    @Path("/changeInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeInfoUser(ChangeUserInfoDTO cuio){
        Optional<User> u = impl.modifyUser(cuio.getActualEmail(), cuio.getNewMail(), cuio.getName(), cuio.getPassword());
        if (u.isPresent()) {
            return Response.ok(UserDTOUtils.toDTO(u.get())).build();
        } else {
            return Response.status(Status.NO_CONTENT).build();
        }
    }
}
