package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {

    public static final int READY = 1;
    public static final int BUSY = 2;
    public static final int FINISHED = 3;
    private String id;
    private String name;
    private int status;
    private List<Dialogue> dialogues;
    private String nextURL;
    private String endURL;

    public Conversation() {}

    public Conversation(String id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.dialogues = new ArrayList<Dialogue>();
    }

    public Conversation(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = Conversation.READY;
        this.dialogues = new ArrayList<Dialogue>();
    }

    public Conversation(String id, String name, int status, List<Dialogue> dialogos) {
        this(id, name, status);
        this.dialogues = dialogos;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
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

    public List<Dialogue> getDialogues() {
        return dialogues;
    }

    public void setDialogues(List<Dialogue> dialogues) {
        this.dialogues = dialogues;
    }

    public void addDialogos(Dialogue dialogue) {
        this.dialogues.add(dialogue);
    }

    public void deleteDialogo(Dialogue dialogue) {
        this.dialogues.remove(dialogue);
    }

    public String getNextURL() {
        return nextURL;
    }

    public void setNextURL(String next) {
        this.nextURL = next;
    }

    public String getEndURL() {
        return endURL;
    }

    public void setEndURL(String end) {
        this.endURL = end;
    }
}
