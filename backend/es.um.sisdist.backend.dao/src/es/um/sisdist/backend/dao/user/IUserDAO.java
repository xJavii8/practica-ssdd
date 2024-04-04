package es.um.sisdist.backend.dao.user;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import es.um.sisdist.backend.dao.models.Dialogo;
import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    public Optional<User> crearUser(String email, String password, String name);

    public boolean deleteUser(String id);
    
    public Optional<User> modifyUser(String id, String emailActual, String emailNuevo, String name, String passNueva);
    
    public Optional<User> addConversation(String idUser, String idConversation);

    public Optional<List<Dialogo>> getAllDialogosOfUser(String idUser);

    public Optional<Dialogo> getDialogoOfUser(String idUser, String idDialogo);
}
