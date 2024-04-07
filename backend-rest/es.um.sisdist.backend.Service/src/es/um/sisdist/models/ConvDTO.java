package es.um.sisdist.models;

import java.util.List;

import es.um.sisdist.backend.dao.models.Dialogue;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ConvDTO {
    private String convID;
    private String convName;
    private int status;
    private List<Dialogue> dialogues;
    private String nextURL;
    private String endURL;

    public ConvDTO() {
    }

    public ConvDTO(String convID, String convName, int status, List<Dialogue> dialogues, String nextURL,
            String endURL) {
        this.convID = convID;
        this.convName = convName;
        this.status = status;
        this.dialogues = dialogues;
        this.nextURL = nextURL;
        this.endURL = endURL;
    }

    public String getConvName() {
        return convName;
    }

    public void setConvName(String convName) {
        this.convName = convName;
    }

    public String getConvID() {
        return convID;
    }

    public void setConvID(String convID) {
        this.convID = convID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Dialogue> getDialogues() {
        return dialogues;
    }

    public void setDialogues(List<Dialogue> dialogues) {
        this.dialogues = dialogues;
    }

    public String getNextURL() {
        return nextURL;
    }

    public void setNextURL(String nextURL) {
        this.nextURL = nextURL;
    }

    public String getEndURL() {
        return endURL;
    }

    public void setEndURL(String endURL) {
        this.endURL = endURL;
    }

}
