package es.um.sisdist.models;

import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AllConvsDTO {
    private List<ConversationSummary> allConvs;

    public AllConvsDTO() {
    }

    public AllConvsDTO(List<ConversationSummary> convs) {
        this.allConvs = convs;
    }

    public List<ConversationSummary> getAllConvs() {
        return allConvs;
    }

    public void setAllConvs(List<ConversationSummary> allConvs) {
        this.allConvs = allConvs;
    }

}
