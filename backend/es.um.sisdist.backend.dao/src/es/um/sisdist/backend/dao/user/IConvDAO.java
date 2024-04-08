package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.Conversation;


public interface IConvDAO {
    public Optional<Conversation> getConvByID(String id);
    
    public Optional<Conversation> createConv(String id, String name, int status);

    public boolean deleteConv(String id);
    
    public Optional<Conversation> modifyConv(String id, int status);
    
    public Optional<Conversation> addDialogue(String convID, String dialogueID);
}
