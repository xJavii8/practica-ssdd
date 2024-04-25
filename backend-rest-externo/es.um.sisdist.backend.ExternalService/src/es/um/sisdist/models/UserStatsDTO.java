package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserStatsDTO {
    private int createdConvs;
    private int promptCalls;

    public UserStatsDTO() {
    }

    public UserStatsDTO(int createdConvs, int promptCalls) {
        this.createdConvs = createdConvs;
        this.promptCalls = promptCalls;
    }

    public int getCreatedConvs() {
        return createdConvs;
    }

    public void setCreatedConvs(int createdConvs) {
        this.createdConvs = createdConvs;
    }

    public int getPromptCalls() {
        return promptCalls;
    }

    public void setPromptCalls(int promptCalls) {
        this.promptCalls = promptCalls;
    }

}
