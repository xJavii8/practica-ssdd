package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversacion {

    public static final int READY=1;
    public static final int BUSY=2;
    public static final int FINISHED=3;
    private String id;
    private int estado;
    private  List<Dialogo> dialogos;
    
    public Conversacion(String id, int estado) {
       this.id = id;
       this.estado = estado;
       this.dialogos = new ArrayList<Dialogo>();
    }
    public Conversacion() {
        this.id  =UUID.randomUUID().toString();
        this.estado = Conversacion.READY;
        this.dialogos = new ArrayList<Dialogo>();
    }

    public Conversacion(String id, int estado, List<Dialogo> dialogos){
        this(id, estado);
        this.dialogos = dialogos;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getEstado() {
        return estado;
    }
    public void setEstado(int estado) {
        this.estado = estado;
    }
    public List<Dialogo> getDialogos() {
        return dialogos;
    }
    public void setDialogos(List<Dialogo> dialogos) {
        this.dialogos = dialogos;
    }
    public void addDialogos(Dialogo dialogo){
        this.dialogos.add(dialogo);
    }
    public void deleteDialogo(Dialogo dialogo){
        this.dialogos.remove(dialogo);
    }


    

}
