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
    private int estado;
    private List<Dialogue> dialogos;

    public Conversation() {}

    public Conversation(String id, String name, int estado) {
        this.id = id;
        this.name = name;
        this.estado = estado;
        this.dialogos = new ArrayList<Dialogue>();
    }

    public Conversation(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.estado = Conversation.READY;
        this.dialogos = new ArrayList<Dialogue>();
    }

    public Conversation(String id, String name, int estado, List<Dialogue> dialogos) {
        this(id, name, estado);
        this.dialogos = dialogos;
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

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public List<Dialogue> getDialogos() {
        return dialogos;
    }

    public void setDialogos(List<Dialogue> dialogos) {
        this.dialogos = dialogos;
    }

    public void addDialogos(Dialogue dialogo) {
        this.dialogos.add(dialogo);
    }

    public void deleteDialogo(Dialogue dialogo) {
        this.dialogos.remove(dialogo);
    }

}
