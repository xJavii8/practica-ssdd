package es.um.sisdist.backend.dao.models;

import java.util.Date;

public class Dialogue {
    private String id;
    private String response;
    private String prompt;
    private Date timestamp;

    public Dialogue() {}

    public Dialogue(String id, String response, String prompt, Date timestamp) {
        this.id = id;
        this.response = response;
        this.prompt = prompt;

        this.timestamp = timestamp;
    }

    public Dialogue(String id, String prompt, Date timestamp) {
        this.id = id;
        this.prompt = prompt;

        this.timestamp = timestamp;
        this.response = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
