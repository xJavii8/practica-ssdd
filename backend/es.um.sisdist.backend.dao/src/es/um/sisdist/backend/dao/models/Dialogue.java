package es.um.sisdist.backend.dao.models;

public class Dialogue {
    private String id;
    private String answer;
    private String prompt;
    private long timestamp;

    public Dialogue() {
    }

    public Dialogue(String id, String answer, String prompt, long timestamp) {
        this.id = id;
        this.answer = answer;
        this.prompt = prompt;
        this.timestamp = timestamp;
    }

    public Dialogue(String id, String prompt, long timestamp) {
        this.id = id;
        this.prompt = prompt;

        this.timestamp = timestamp;
        this.answer = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
