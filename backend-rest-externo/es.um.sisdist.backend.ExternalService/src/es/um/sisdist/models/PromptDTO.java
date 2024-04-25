package es.um.sisdist.models;

import es.um.sisdist.backend.dao.models.Conversation;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PromptDTO {
    private String userID;
    private String convID;
    private String prompt;
    private long timestamp;
    private ConvDTO conversation;

    public PromptDTO() {
    }

    public PromptDTO(String userID, String convID, String prompt, long timestamp) {
        this.userID = userID;
        this.convID = convID;
        this.prompt = prompt;
    }

    public PromptDTO(Conversation conversation) {
        this.conversation = ConvDTOUtils.toDTO(conversation);
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getConvID() {
        return convID;
    }

    public void setConvID(String convID) {
        this.convID = convID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ConvDTO getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = ConvDTOUtils.toDTO(conversation);
    }

}

