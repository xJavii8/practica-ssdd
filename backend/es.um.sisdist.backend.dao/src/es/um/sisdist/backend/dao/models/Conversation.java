package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.swing.text.html.Option;

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

    public Conversation() {
    }

    public Conversation(String userID, String id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.dialogues = new ArrayList<Dialogue>();
        this.nextURL = ("/u/" + userID + "/dialogue/" + this.id + "/next/"
                + String.valueOf(System.currentTimeMillis()));
        this.endURL = ("/u/" + userID + "/dialogue/" + this.id + "/end");
    }

    public Conversation(String userID, String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = Conversation.READY;
        this.dialogues = new ArrayList<Dialogue>();
        this.nextURL = ("/u/" + userID + "/dialogue/" + this.id + "/next/"
                + String.valueOf(System.currentTimeMillis()));
        this.endURL = ("/u/" + userID + "/dialogue/" + this.id + "/end");
    }

    public Conversation(String userID, String id, String name, int status, List<Dialogue> dialogos) {
        this(userID, id, name, status);
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

    public void addDialogue(Dialogue dialogue) {
        this.dialogues.add(dialogue);
    }

    public void deleteDialogue(Dialogue dialogue) {
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

    public void setNewTimestamp(String userID, long timestamp) {
        this.nextURL = ("/u/" + userID + "/dialogue/" + this.id + "/next/"
                + String.valueOf(timestamp));
    }

    public boolean addResponse(String idDialogue, String response) {
        Optional<Dialogue> dialogo = dialogues.stream()
                .filter(dialogue -> idDialogue.equals(dialogue.getId()))
                .findFirst();
        if (dialogo.isPresent()) {
            Dialogue d = dialogo.get();
            int index = dialogues.indexOf(d);
            d.setAnswer(response);
            dialogues.set(index, d);
            return true;
        }

        return false;
    }
}
