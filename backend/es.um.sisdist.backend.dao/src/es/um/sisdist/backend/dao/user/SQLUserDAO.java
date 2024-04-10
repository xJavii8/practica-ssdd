/**
 *
 */
package es.um.sisdist.backend.dao.user;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class SQLUserDAO implements IUserDAO {
    Supplier<Connection> conn;

    public SQLUserDAO() {
        conn = Lazy.lazily(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

                // Si el nombre del host se pasa por environment, se usa aquí.
                // Si no, se usa localhost. Esto permite configurarlo de forma
                // sencilla para cuando se ejecute en el contenedor, y a la vez
                // se pueden hacer pruebas locales
                String sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER")).orElse("localhost");
                String dbName = Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd");
                return DriverManager.getConnection(
                        "jdbc:mysql://" + sqlServerName + "/" + dbName + "?user=root&password=root");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                return null;
            }
        });
    }

    @Override
    public Optional<User> getUserById(String id) {
        PreparedStatement stm;
        try {
            stm = conn.get().prepareStatement("SELECT * from users WHERE id = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserByEmail(String id) {
        PreparedStatement stm;
        try {
            stm = conn.get().prepareStatement("SELECT * from users WHERE email = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e) {
            // Fallthrough
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> crearUser(String email, String password, String name) {
        PreparedStatement stm;
        try {
            // Prepare insert statement
            stm = conn.get().prepareStatement(
                    "INSERT INTO users (id, email, password_hash, name, token, visits) VALUES (?,?,?,?,?,?);");
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

    private Optional<User> createUser(ResultSet result) {
        try {
            return Optional.of(new User(result.getString(1), // id
                    result.getString(2), // email
                    result.getString(3), // pwhash
                    result.getString(4), // name
                    result.getString(5), // token
                    result.getInt(6))); // promptCalls
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteUser(String id) {
        // TODO Auto-generated method stub
        Optional<User> user = getUserById(id);
        if (user.isPresent()) {
            try {
                PreparedStatement stm = conn.get().prepareStatement("DELETE from users WHERE id = ?");
                stm.setString(1, id);
                int result = stm.executeUpdate();
                if (result == 1) {
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
    public Optional<User> modifyUser(String id, String actualEmail, String newEmail, String name, String newPass) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method");

    }

    @Override
    public Optional<Conversation> createConversation(String userID, String convName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createConversation'");
    }

    @Override
    public boolean endConversation(String userID, String convID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endConversation'");
    }

    @Override
    public boolean checkIfConvExists(String userID, String convID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkIfConvExists'");
    }

    @Override
    public Optional<Conversation> getConvByID(String userID, String convID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getConvByID'");
    }

    @Override
    public boolean createDialogue(String userID, String convID, String dialogueID, String prompt, long timestamp) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createDialogue'");
    }

    @Override
    public boolean addResponse(String userID, String convID, String dialogueID, String response) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addResponse'");
    }

    @Override
    public boolean delConversation(String userID, String convID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delConversation'");
    }

    @Override
    public boolean delAllConvs(String userID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delAllConvs'");
    }

    @Override
    public boolean updatePromptCalls(String userID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePromptCalls'");
    }

}
