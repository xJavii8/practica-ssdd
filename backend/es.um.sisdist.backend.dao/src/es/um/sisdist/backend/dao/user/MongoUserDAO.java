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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

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
            User user = new User(email, UserUtils.md5pass(password), name, token, 0, 0);
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

            // Comprobación de que el mail no es el mismo y que este no existe ya
            if (!newEmail.isEmpty() && !actualEmail.equals(newEmail)) {
                Optional<User> userWithNewEmail = getUserByEmail(newEmail);
                if (userWithNewEmail.isPresent()) {
                    error = true;
                }
            }

            // Actualización de los datos
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

            // Actualización del usuario en la DB
            if (conv.isPresent()) {
                List<Conversation> conversations = user.getConversations();
                int createdConvs = user.getCreatedConvs();
                Bson filter = Filters.eq("id", userID);
                ArrayList<Bson> updates = new ArrayList<>();
                updates.add(Updates.set("conversations", conversations));
                updates.add(Updates.set("createdConvs", createdConvs));
                UpdateResult result = collection.get().updateOne(filter, Updates.combine(updates));

                if (result.getModifiedCount() == 1) {
                    return conv;
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Conversation> getConvByID(String userID, String convID) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            List<Conversation> conversations = user.getConversations();

            for (Conversation c : conversations) {
                if (c.getID().equals(convID)) {
                    return Optional.of(c);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean endConversation(String userID, String convID) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            Optional<List<Conversation>> conv = user.endConversation(convID);

            if (conv.isPresent()) {
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
    public boolean delConversation(String userID, String convID) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            boolean deleted = user.delConversation(convID);

            if (deleted == true) {
                List<Conversation> conversations = user.getConversations();
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
    public boolean delAllConvs(String userID) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            List<Conversation> conversations = user.delAllConvs();
            Bson filter = Filters.eq("id", userID);
            UpdateResult result = collection.get().updateOne(filter, Updates.set("conversations", conversations));

            if (result.getModifiedCount() == 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean createDialogue(String userID, String convID, String dialogueID, String prompt, long timestamp) {
        Optional<User> u = getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            Optional<List<Conversation>> conv = user.addDialogue(convID, new Dialogue(dialogueID, prompt, timestamp));

            if (conv.isPresent()) {
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
    public boolean addResponse(String userID, String convID, String dialogueID, String response) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            Optional<List<Conversation>> conv = user.addResponse(convID, dialogueID, response);

            if (conv.isPresent()) {
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
    public boolean updatePromptCalls(String userID) {
        Optional<User> u = getUserById(userID);

        if (u.isPresent()) {
            User user = u.get();
            user.updatePromptCalls();
            Bson filter = Filters.eq("id", userID);
            UpdateResult result = collection.get().updateOne(filter, Updates.set("promptCalls", user.getPromptCalls()));

            if (result.getModifiedCount() == 1) {
                return true;
            }
        }

        return false;
    }
}
