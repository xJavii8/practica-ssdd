/**
 *
 */
package es.um.sisdist.backend.dao.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class SQLUserDAO implements IUserDAO
{
    Supplier<Connection> conn;

    public SQLUserDAO()
    {
    	conn = Lazy.lazily(() -> 
    	{
    		try
    		{
    			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

    			// Si el nombre del host se pasa por environment, se usa aquí.
    			// Si no, se usa localhost. Esto permite configurarlo de forma
    			// sencilla para cuando se ejecute en el contenedor, y a la vez
    			// se pueden hacer pruebas locales
    			String sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER")).orElse("localhost");
    			String dbName = Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd");
    			return DriverManager.getConnection(
                    "jdbc:mysql://" + sqlServerName + "/" + dbName + "?user=root&password=root");
    		} catch (Exception e)
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
            
    			return null;
    		}
    	});
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        PreparedStatement stm;
        try{
            stm = conn.get().prepareStatement("SELECT * from users WHERE id = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        }catch (SQLException e){
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByEmail(String id)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("SELECT * from users WHERE email = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e)
        {
            // Fallthrough
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> crearUser(String email, String password, String name) {
        PreparedStatement stm;
        try {
            //Prepare insert statement
            stm = conn.get().prepareStatement("INSERT INTO users (id, email, password_hash, name, token, visits) VALUES (?,?,?,?,?,?);");
            String userID = UserUtils.md5pass(email);
            String token = UUID.randomUUID().toString();
            stm.setString(1, userID);
            stm.setString(2, email);
            stm.setString(3, UserUtils.md5pass(password));
            stm.setString(4, name);
            stm.setString(5, token);
            stm.setInt(6, 0);
            int result = stm.executeUpdate();
            if (result == 1) {
                return Optional.of(new User(userID, email, UserUtils.md5pass(password), name, token, 0));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Optional.empty();
    }
        
    private Optional<User> createUser(ResultSet result)
    {
        try
        {
            return Optional.of(new User(result.getString(1), // id
                    result.getString(2), // email
                    result.getString(3), // pwhash
                    result.getString(4), // name
                    result.getString(5), // token
                    result.getInt(6))); // visits
        } catch (SQLException e)
        {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteUser(String id) {
        // TODO Auto-generated method stub
        Optional<User> user = getUserById(id);
        if (user.isPresent()){
            try {
                PreparedStatement stm = conn.get().prepareStatement("DELETE from users WHERE id = ?");
                stm.setString(1, id);
                int result = stm.executeUpdate();
                if (result == 1 ){
                    return true;
                }
    
             
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        return false;
    }

    @Override
    public Optional<User> modifyUser(User user) {
        // TODO Auto-generated method stub
        Optional<User> user2 = getUserById(user.getId());
        if (!user2.get().getEmail().equals(user.getEmail())){
            Optional<User> userOther = getUserByEmail(user.getEmail());
            if(userOther.isPresent()){
                return Optional.empty();
            }
        }
        try {
            PreparedStatement stm = conn.get().prepareStatement("UPDATE users SET email = ?, password_hash = ?, name = ?, token = ?, visits = ? WHERE id = ?");
            stm.setString(1, user.getEmail());
            stm.setString(2, user.getPassword_hash());
            stm.setString(3, user.getName());
            stm.setString(4, user.getToken());
            stm.setInt(5, user.getVisits());
            stm.setString(6, user.getId());
            int result = stm.executeUpdate();
            if(result!=0){
                return Optional.of(user);
            }
         
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Optional.empty();

    }

}
