package es.um.sisdist.models;

public class ConversationSummary {
    private String name;
    private int status;

    public ConversationSummary() {
    }

    public ConversationSummary(String name, int status) {
        this.name = name;
        this.status = status;
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
}
