package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Conversation;

public class ConvDTOUtils {
    // TODO: Todavía tengo que pensar cómo implementar el nextURL y endURL en la conversación
    public static Conversation fromDTO(ConvDTO cdto) {
        return new Conversation(cdto.getConvID(), cdto.getConvName(), cdto.getStatus(), cdto.getDialogues());
    }

    public static ConvDTO toDTO(Conversation c) {
        return new ConvDTO(c.getID(), c.getName(), c.getStatus(), c.getDialogues(), c.getNextURL(), c.getEndURL());
    }
}