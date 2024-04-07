/**
 *
 */
package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.management.openmbean.OpenType;

import org.bson.BsonDocument;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class MongoUserDAO implements IUserDAO {
    private Supplier<MongoCollection<User>> collection;
    private static final Logger logger = Logger.getLogger(MongoUserDAO.class.getName());

    public MongoUserDAO() {
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
            return database.getCollection("users", User.class);
        });
    }

    @Override
    public Optional<User> getUserById(String id) {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String id) {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("email", id)).first());
        return user;
    }

    @Override
    public Optional<User> crearUser(String email, String password, String name) {

        Optional<User> u = getUserByEmail(email);
        if (!u.isPresent()) {
            String token = UUID.randomUUID().toString();
            // Nota: El id se genera ya en la función.
            User user = new User(email, UserUtils.md5pass(password), name, token, 0);
            try {
                collection.get().insertOne(user);
            } catch (MongoException except) {
                except.printStackTrace();
                return Optional.empty();
            }
            return Optional.of(user);
        }
        return Optional.empty();

    }

    @Override
    public boolean deleteUser(String id) {
        Optional<User> e = getUserById(id);

        if (e.isPresent()) {
            DeleteResult result = collection.get().deleteOne(Filters.eq("id", id));
            if (result.getDeletedCount() == 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<User> modifyUser(String id, String actualEmail, String newEmail, String name, String newPass) {
        boolean error = false;
        Optional<User> userExists = getUserByEmail(actualEmail);

        if (userExists.isPresent()) {
            User u = userExists.get();

            if (!newEmail.isEmpty() && !actualEmail.equals(newEmail)) {
                Optional<User> userWithNewEmail = getUserByEmail(newEmail);
                if (userWithNewEmail.isPresent()) {
                    error = true;
                }
            }

            if (error == false) {
                Bson filter = Filters.eq("email", actualEmail);
                ArrayList<Bson> updates = new ArrayList<>();

                if (!u.getName().equals(name)) {
                    updates.add(Updates.set("name", name));
                }

                if (!newEmail.isEmpty()) {
                    updates.add(Updates.set("id", UserUtils.md5pass(newEmail)));
                    updates.add(Updates.set("email", newEmail));
                }

                if (!newPass.isEmpty()) {
                    updates.add(Updates.set("password_hash", UserUtils.md5pass(newPass)));
                }

                UpdateResult result = collection.get().updateOne(filter, Updates.combine(updates));
                if (result.getModifiedCount() == 1) {
                    return getUserById(u.getId());
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Conversation> createConversation(String userID, String convName) {
        Optional<User> u = getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            Optional<Conversation> conv = user.createConversation(convName);
            if(conv.isPresent()) {
                List<Conversation> conversations = user.getConversations();
                Bson filter = Filters.eq("id", userID);
                UpdateResult result = collection.get().updateOne(filter, Updates.set("conversations", conversations));
                if (result.getModifiedCount() == 1) {
                    return conv;
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean checkIfConvExists(String userID, String convName) {
        Optional<User> u = getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            List<Conversation> conversations = user.getConversations();
            for(Conversation c : conversations) {
                if(c.getName().equals(convName) && c.getEstado() != Conversation.FINISHED) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean endConversation(String userID, String convName) {
        Optional<User> u = getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            Optional<List<Conversation>> conv = user.endConversation(convName);
            if(conv.isPresent()) {
                List<Conversation> conversations = conv.get();
                Bson filter = Filters.eq("id", userID);
                UpdateResult result = collection.get().updateOne(filter, Updates.set("conversations", conversations));
                if (result.getModifiedCount() == 1) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Optional<List<Dialogue>> getAllDialoguesFromUser(String userID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllDialogosOfUser'");
    }

    @Override
    public Optional<Dialogue> getDialogueFromUser(String userID, String dialogueID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDialogoOfUser'");
    }
}
