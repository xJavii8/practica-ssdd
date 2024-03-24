/**
 *
 */
package es.um.sisdist.backend.Service.impl;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.message.internal.Token;

import com.google.protobuf.Option;

import es.um.sisdist.backend.grpc.GETRequest;
import es.um.sisdist.backend.grpc.GETResponse;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.POSTRequest;
import es.um.sisdist.backend.grpc.POSTResponse;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl {
    IDAOFactory daoFactory;
    IUserDAO dao;

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom(); // threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); // threadsafe
    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    // private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl() {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));

        if (backend.isPresent() && backend.get().equals("mongo"))
            dao = daoFactory.createMongoUserDAO();
        else
            dao = daoFactory.createSQLUserDAO();

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid needing certificates.
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        // asyncStub = GrpcServiceGrpc.newStub(channel);
    }

    public static AppLogicImpl getInstance() {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId) {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId) {
        return dao.getUserById(userId);
    }

    public boolean ping(int v) {
        logger.info("Issuing ping, value: " + v);

        // Test de grpc, puede hacerse con la BD
        var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);

        return response.getV() == v;
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass) {
        Optional<User> u = dao.getUserByEmail(email);

        if (u.isPresent()) {
            String hashed_pass = UserUtils.md5pass(pass);
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
                return u;
        }

        return Optional.empty();
    }

    // El frontend, a través del formulario de registro,
    // envía su usuario, email y pass. El ID y el token se generan
    // posteriormente. Se comprueba que el email no este en uso
    public Optional<User> createUser(String email, String name, String pass) {
        Optional<User> u = dao.getUserByEmail(email);

        if (!u.isPresent()) {
            u = dao.crearUser(email, pass, name);
            if (u.isPresent()) {
                return u;
            }
        }
        return Optional.empty();
    }

    public boolean deleteUser(String id) {
        return dao.deleteUser(id);
    }

    public String testPOST() {

        POSTRequest req1 = POSTRequest.newBuilder().setPrompt("Hola! Esto es un test").build();
        POSTResponse resp1;

        GETResponse resp2;
        String answer = "";

        try {
            resp1 = blockingStub.promptPOST(req1);

            logger.info("RESPUESTA POST: " + resp1.getLocalization());

            GETRequest req2 = GETRequest.newBuilder().setAnswerURL(resp1.getLocalization()).build();

            resp2 = blockingStub.promptGET(req2);

            logger.info("RESPUESTA GET: " + resp2.getAnswerText());

            answer = resp2.getAnswerText();

        } catch (StatusRuntimeException e) {
            return "";
        }

        return answer;
    }

    public Optional<User> modifyUser(String actualEmail, String newMail, String name, String password) {
        Optional<User> u = dao.getUserByEmail(actualEmail);
        if (u.isPresent()) {
            return dao.modifyUser(actualEmail, newMail, name, password);
        }
        return Optional.empty();
    }

    // Actualizar visitas podria ser una funcion muy recurrente
    /*
     * public Optional<User> modifyUserVisits(String id, int visits){
     * Optional<User> u = dao.getUserById(id);
     * if(u.isPresent()){
     * u.get().setVisits(visits);
     * return dao.modifyUser(u.get());
     * }
     * return Optional.empty();
     * }
     */
}
