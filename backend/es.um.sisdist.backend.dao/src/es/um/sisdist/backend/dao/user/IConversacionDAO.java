package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.Conversacion;


public interface IConversacionDAO {
    public Optional<Conversacion> getConversacionById(String id);
    
    public Optional<Conversacion> crearConversacion(String id, int estado);

    public boolean deleteConversacion(String id);
    
    public Optional<Conversacion> modifyConversacion(String id, int estado);
    
    public Optional<Conversacion> addDialogo(String idConversacion, String idDialogo);
}
