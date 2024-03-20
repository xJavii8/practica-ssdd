package es.um.sisdist.backend;
import java.net.URI;
import java.util.Optional;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

//Pojo no interface no extend
@Path("/checkLogin")
public class RegisterUserEndpoint {
    private AppLogicImpl impl = AppLogicImpl.getInstance();
    @POST
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
}
