package es.um.sisdist.backend.dao.user;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    public Optional<User> crearUser(String email, String password, String name);

    public boolean deleteUser(String id);
    
    public Optional<User> modifyUser(String id, String actualEmail, String newEmail, String name, String newPass);
    
    public Optional<Conversation> createConversation(String userID, String convName);

    public boolean checkIfConvExists(String userID, String convName);

    public boolean endConversation(String userID, String convName);

    public Optional<List<Dialogue>> getAllDialoguesFromUser(String userID);

    public Optional<Dialogue> getDialogueFromUser(String userID, String dialogueID);
}
