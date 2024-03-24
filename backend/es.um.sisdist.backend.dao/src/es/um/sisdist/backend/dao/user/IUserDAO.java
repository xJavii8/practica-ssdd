package es.um.sisdist.backend.dao.user;

import java.util.Optional;
import java.util.Properties;

import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    public Optional<User> crearUser(String email, String password, String name);

    public boolean deleteUser(String id);
    
    public Optional<User> modifyUser(String emailActual, String emailNuevo, String name, String passNueva);
}
