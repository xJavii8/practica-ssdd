/**
 *
 */
package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static java.util.Arrays.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javax.management.openmbean.OpenType;

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
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class MongoUserDAO implements IUserDAO
{
    private Supplier<MongoCollection<User>> collection;

    public MongoUserDAO()
    {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@" 
        		+ Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        collection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        		.withCodecRegistry(pojoCodecRegistry);
        	return database.getCollection("users", User.class);
        });
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("email", id)).first());
        return user;
    }

    @Override
    public Optional<User> crearUser(String email, String password, String name) {

        Optional<User> e = getUserByEmail(email);
        if(!e.isPresent()){
            String token = UUID.randomUUID().toString();
        //Nota: El id se genera ya en la función.
            User document = new User(email, UserUtils.md5pass(password), name, token, 0);
            try {
                collection.get().insertOne(document);
            }catch(MongoException except ){
                except.printStackTrace();
                return Optional.empty();
            }
        return Optional.of(document);
    }
        return Optional.empty(); 
   
    }

    @Override
    public boolean deleteUser(String id) {
        // TODO Auto-generated method stub
        Optional<User> e = getUserById(id);
        if (e.isPresent()){
          DeleteResult result =  collection.get().deleteOne((Bson) e.get()); 
           if(result.getDeletedCount() == 1){
            return true;
           }
        }
        return false;
    }

    @Override
    public Optional<User> modifyUser(User user) {
        Optional<User> user2 = getUserById(user.getId());
        if(user2.isPresent()){
            if(!user.getEmail().equals(user2.get().getEmail())){
                Optional<User> user3 = getUserByEmail(user.getEmail());
                if (user3.isPresent()){
                    return Optional.empty();
                }
            }
           UpdateResult result =  collection.get().updateOne((Bson) user2.get(), (Bson) new User(user.getEmail(),user.getPassword_hash(),user.getName(),user.getToken(),user.getVisits()));
            if(result.getModifiedCount() == 1){
                return Optional.of(user);
            }
        }
        return Optional.empty();

    }

}
