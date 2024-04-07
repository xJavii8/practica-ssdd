package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.print.attribute.standard.DialogTypeSelection;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

public class MongoDialogueDAO implements IDialogueDAO {
    private Supplier<MongoCollection<Dialogue>> collection;
    private static final Logger logger = Logger.getLogger(MongoDialogueDAO.class.getName());

    public MongoDialogueDAO() {
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
            return database.getCollection("dialogos", Dialogue.class);
        });
    }

    @Override
    public Optional<Dialogue> getDialogueByID(String id) {
        Optional<Dialogue> dialogo = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return dialogo;
    }

    @Override
    public Optional<Dialogue> createDialogue(String id, String response, String prompt, Date timestamp) {
        Optional<Dialogue> dialogoBD = getDialogueByID(id);
        if (!dialogoBD.isPresent()) {
            Dialogue document;
            if(response == null){
                document = new Dialogue(id, prompt, timestamp);
            }else {
                 document = new Dialogue(id, response, prompt, timestamp);
            }
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
    public boolean deleteDialogue(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteDialogo'");
    }

    @Override
    public Optional<Dialogue> modifyDialogue(String id, String prompt, Optional<Date> timestamp, String response) {
        boolean error = false;
        Optional<Dialogue> dialgoExists = getDialogueByID(id);

        if (dialgoExists.isPresent()) {
            Dialogue u = dialgoExists.get();
            boolean changePrompt = true;
            if(!timestamp.isPresent()){
                if(!prompt.isEmpty()){
                    error = true;
                }
                changePrompt = false;
            } else if(prompt.isEmpty()){
                changePrompt = false;
            }

            if (!prompt.isEmpty() && timestamp.isEmpty()) {
                error = true;
            } else if (prompt.isEmpty() && !timestamp.isEmpty()){
                
            }
            if (error == false) {
                Bson filter = Filters.eq("id", id);
                ArrayList<Bson> updates = new ArrayList<>();
                if(changePrompt){
                if (!u.getPrompt().equals(prompt)) {
                    updates.add(Updates.set("prompt", prompt));
                    updates.add(Updates.set("timestamp", timestamp.get()));
                }
                
                }
                if (!response.isEmpty()) {
                    updates.add(Updates.set("response", response));
                }
                UpdateResult result = collection.get().updateOne(filter, Updates.combine(updates));
                if (result.getModifiedCount() == 1) {
                    return getDialogueByID(u.getId());
                }
            }
        }
        return Optional.empty();
    }
    
}
