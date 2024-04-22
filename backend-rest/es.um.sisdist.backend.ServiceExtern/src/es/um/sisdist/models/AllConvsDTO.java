package es.um.sisdist.models;

import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AllConvsDTO {
    private List<ConvSummaryDTO> allConvs;
    public AllConvsDTO() {
    }

    public AllConvsDTO(List<ConvSummaryDTO> convs) {
        this.allConvs = convs;
    }

    public List<ConvSummaryDTO> getAllConvs() {
        return allConvs;
    }

    public void setAllConvs(List<ConvSummaryDTO> allConvs) {
        this.allConvs = allConvs;
    }

}
