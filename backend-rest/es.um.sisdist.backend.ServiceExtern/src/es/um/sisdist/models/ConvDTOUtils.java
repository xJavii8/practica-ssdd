package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Conversation;

public class ConvDTOUtils {
    
    public static Conversation fromDTO(String userID, ConvDTO cdto) {
        return new Conversation(userID, cdto.getConvID(), cdto.getConvName(), cdto.getStatus(), cdto.getDialogues());
    }

    public static ConvDTO toDTO(Conversation c) {
        return new ConvDTO(c.getID(), c.getName(), c.getStatus(), c.getDialogues(), c.getNextURL(), c.getEndURL());
    }
}