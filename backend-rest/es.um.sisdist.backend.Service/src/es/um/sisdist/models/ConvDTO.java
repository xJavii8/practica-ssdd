package es.um.sisdist.models;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConvDTO {
    private String convName;

    public ConvDTO() {
    }

    public ConvDTO(String convName) {
        this.convName = convName;
    }

    public String getConvName() {
        return convName;
    }

    public void setConvName(String convName) {
        this.convName = convName;
    }

}
