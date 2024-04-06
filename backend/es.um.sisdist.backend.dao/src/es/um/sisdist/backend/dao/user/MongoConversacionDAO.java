package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.models.Conversacion;
import es.um.sisdist.backend.dao.models.Dialogo;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

public class MongoConversacionDAO implements IConversacionDAO{
    private Supplier<MongoCollection<Conversacion>> collection;
    private static final Logger logger = Logger.getLogger(MongoConversacionDAO.class.getName());
    private MongoDialogoDAO dialogoDAO = new MongoDialogoDAO();

     public MongoConversacionDAO() {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@"
                + Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        collection = Lazy.lazily(() -> {
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient
                    .getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
                    .withCodecRegistry(pojoCodecRegistry);
            return database.getCollection("conversaciones", Conversacion.class);
        });
    }

    @Override
    public Optional<Conversacion> getConversacionById(String id) {
        Optional<Conversacion> conversacion = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return conversacion;
    }

    @Override
    public Optional<Conversacion> crearConversacion(String id, int estado) {
          Optional<Conversacion> conversacionBD = getConversacionById(id);
        if (!conversacionBD.isPresent()) {
            Conversacion document = new Conversacion(id, estado);
            try{
                collection.get().insertOne(document);
            }catch(MongoException e){
                e.printStackTrace();
                return Optional.empty();
            }
           return Optional.of(document);

        }
        return Optional.empty();
    }

    @Override
    public boolean deleteConversacion(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteConversacion'");
    }

    @Override
    public Optional<Conversacion> modifyConversacion(String id, int estado) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modifyConversacion'");
    }

    @Override
    public Optional<Conversacion> addDialogo(String idConversacion, String idDialogo) {
        Optional<Conversacion> conversacionBD = getConversacionById(idConversacion);
        Optional<Dialogo> dialogo = dialogoDAO.getDialogoById(idDialogo);
        
        if(conversacionBD.isPresent() && dialogo.isPresent()){
            Conversacion cv = conversacionBD.get();
            Dialogo d = dialogo.get();
            Bson filter = Filters.eq("id", idConversacion);
            ArrayList<Bson> updates = new ArrayList<>();
            updates.add(Updates.set("dialogos", d));
            UpdateResult result = collection.get().updateOne(filter, Updates.combine(updates));
            if (result.getModifiedCount() == 1) {
                    return getConversacionById(idConversacion);
                }
            }
        return Optional.empty();
    }
    
    
}
