package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO {
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    public Optional<User> crearUser(String email, String password, String name);

    public boolean deleteUser(String id);

    public Optional<User> modifyUser(String id, String actualEmail, String newEmail, String name, String newPass);

    public Optional<Conversation> createConversation(String userID, String convName);

    public Optional<Conversation> getConvByID(String userID, String convID);

    public boolean endConversation(String userID, String convID);

    public boolean delConversation(String userID, String convID);

    public boolean delAllConvs(String userID);

    public boolean createDialogue(String userID, String convID, String dialogueID, String prompt, long timestamp);

    public boolean addResponse(String userID, String convID, String dialogueID, String response);

    public boolean updatePromptCalls(String userID);

}
