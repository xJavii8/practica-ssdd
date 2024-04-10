package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserStatsDTO {
    private int numConvs;
    private int promptCalls;

    public UserStatsDTO() {
    }

    public UserStatsDTO(int numConvs, int promptCalls) {
        this.numConvs = numConvs;
        this.promptCalls = promptCalls;
    }

    public int getNumConvs() {
        return numConvs;
    }

    public void setNumConvs(int numConvs) {
        this.numConvs = numConvs;
    }

    public int getPromptCalls() {
        return promptCalls;
    }

    public void setPromptCalls(int promptCalls) {
        this.promptCalls = promptCalls;
    }

}
