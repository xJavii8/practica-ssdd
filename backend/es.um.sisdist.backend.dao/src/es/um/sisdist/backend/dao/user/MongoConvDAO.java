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
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

public class MongoConvDAO implements IConvDAO {
    private Supplier<MongoCollection<Conversation>> collection;
    private static final Logger logger = Logger.getLogger(MongoConvDAO.class.getName());
    private MongoDialogueDAO dialogoDAO = new MongoDialogueDAO();

    public MongoConvDAO() {
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
            return database.getCollection("conversaciones", Conversation.class);
        });
    }

    @Override
    public Optional<Conversation> getConvByID(String id) {
        Optional<Conversation> conversacion = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return conversacion;
    }

    @Override
    public Optional<Conversation> createConv(String id, String name, int status) {
        Optional<Conversation> conversacionBD = getConvByID(id);
        logger.info("CREATE CONV: ID OBTENIDA");
        if (!conversacionBD.isPresent()) {
            //Conversation convDoc = new Conversation(id, name, status);
            logger.info("CONVERSACION CREADA");
            try {
               // collection.get().insertOne(convDoc);
                logger.info("CREATE CONV: INSERTADA");
            } catch (MongoException e) {
                logger.info("CREATE CONV: EXCEPTION");
                logger.info(e.getLocalizedMessage());
                return Optional.empty();
            }
            logger.info("CREATE CONV: RETORNADA");
            return Optional.empty();
        }
        logger.info("CREATE CONV: EMPTY");
        return Optional.empty();
    }

    @Override
    public boolean deleteConv(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteConversacion'");
    }

    @Override
    public Optional<Conversation> modifyConv(String id, int status) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modifyConversacion'");
    }

    @Override
    public Optional<Conversation> addDialogue(String convID, String dialogueID) {
        Optional<Conversation> conv = getConvByID(convID);
        Optional<Dialogue> dialogo = dialogoDAO.getDialogueByID(dialogueID);

        if (conv.isPresent() && dialogo.isPresent()) {
            Conversation cv = conv.get();
            Dialogue d = dialogo.get();
            Bson filter = Filters.eq("id", convID);
            ArrayList<Bson> updates = new ArrayList<>();
            updates.add(Updates.set("dialogos", d));
            UpdateResult result = collection.get().updateOne(filter, Updates.combine(updates));
            if (result.getModifiedCount() == 1) {
                return getConvByID(convID);
            }
        }
        return Optional.empty();
    }

}
