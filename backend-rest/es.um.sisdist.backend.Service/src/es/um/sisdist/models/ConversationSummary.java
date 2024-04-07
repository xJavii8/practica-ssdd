package es.um.sisdist.models;

public class ConversationSummary {
    private String name;
    private int status;
    private String id;

    public ConversationSummary() {
    }

    public ConversationSummary(String name, int status, String id) {
        this.name = name;
        this.status = status;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
