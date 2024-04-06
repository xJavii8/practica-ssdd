package es.um.sisdist.backend.dao.user;

import java.util.Date;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.Dialogo;

public interface IDialogoDAO {

    public Optional<Dialogo> getDialogoById(String id);

    public Optional<Dialogo> crearDialogo(String id, String response, String prompt, Date timestamp);

    public boolean deleteDialogo(String id);
    
    public Optional<Dialogo> modifyDialogo(String id, String prompt, Optional<Date> timestamp, String response);
}
