package es.um.sisdist.backend.dao.user;

import java.util.Date;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.Dialogue;

public interface IDialogueDAO {

    public Optional<Dialogue> getDialogueByID(String id);

    public Optional<Dialogue> createDialogue(String id, String response, String prompt, Date timestamp);

    public boolean deleteDialogue(String id);
    
    public Optional<Dialogue> modifyDialogue(String id, String prompt, Optional<Date> timestamp, String response);
}
