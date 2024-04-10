package es.um.sisdist.models;

import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AllConvsDTO {
    private List<ConversationSummaryDTO> allConvs;

    public AllConvsDTO() {
    }

    public AllConvsDTO(List<ConversationSummaryDTO> convs) {
        this.allConvs = convs;
    }

    public List<ConversationSummaryDTO> getAllConvs() {
        return allConvs;
    }

    public void setAllConvs(List<ConversationSummaryDTO> allConvs) {
        this.allConvs = allConvs;
    }

}
