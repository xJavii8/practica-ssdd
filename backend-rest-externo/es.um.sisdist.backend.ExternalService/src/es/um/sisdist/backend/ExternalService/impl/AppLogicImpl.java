/**
 *
 */
package es.um.sisdist.backend.ExternalService.impl;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import java.util.List;

import es.um.sisdist.backend.grpc.GETRequest;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.POSTRequest;
import es.um.sisdist.backend.grpc.POSTResponse;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.models.ConvSummaryDTO;
import es.um.sisdist.models.UserStatsDTO;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.Conversation;
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

    public Optional<UserStatsDTO> getUserStats(String userID) {
        Optional<User> u = dao.getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            int numConvs = user.getCreatedConvs();
            int promptCalls = user.getPromptCalls();
            return Optional.of(new UserStatsDTO(numConvs, promptCalls));
        }

        return Optional.empty();
    }

    public Optional<Conversation> createConversation(String userID, String name) {
        Optional<Conversation> c = dao.createConversation(userID, name);
        if (!c.isPresent()) {
            return Optional.empty();
        }

        return c;
    }

    public Optional<List<ConvSummaryDTO>> getConversations(String userID) {
        Optional<User> u = dao.getUserById(userID);
        if (u.isPresent()) {
            User user = u.get();
            // Creamos un resumen de las conversaciones para reducir la cantidad de datos a enviar
            return Optional.of(user.getConversations().stream()
                    .map(conversation -> new ConvSummaryDTO(conversation.getName(), conversation.getStatus(),
                            conversation.getID()))
                    .collect(Collectors.toList()));
        }

        return Optional.empty();
    }

    public boolean endConversation(String userID, String convID) {
        return dao.endConversation(userID, convID);
    }

    public boolean delConversation(String userID, String convID) {
        return dao.delConversation(userID, convID);
    }

    public boolean delAllConvs(String userID) {
        return dao.delAllConvs(userID);
    }

    public Optional<Conversation> getConversationData(String userID, String convID) {
        Optional<Conversation> c = dao.getConvByID(userID, convID);
        if (c.isPresent()) {
            return c;
        }

        return Optional.empty();
    }

    public boolean isConvReady(String userID, String convID) {
        // Comprobamos si la conversación está lista para recibir más mensajes
        Optional<Conversation> c = dao.getConvByID(userID, convID);
        if (c.isPresent()) {
            Conversation conv = c.get();
            if (conv.getStatus() == Conversation.READY) {
                return true;
            }
        }

        return false;
    }

    public Optional<Conversation> sendPrompt(String userID, String convID, String prompt, long timestamp) {
        dao.updatePromptCalls(userID); // Actualización de estadísticas
        POSTRequest req1 = POSTRequest.newBuilder().setPrompt(prompt).build();
        POSTResponse resp1;

        try {
            resp1 = blockingStub.promptPOST(req1); // POST a LlamaChat
            dao.createDialogue(userID, convID, resp1.getLocalization().split("/")[2], prompt, timestamp); // Creación del diálogo
            GETRequest req2 = GETRequest.newBuilder().setAnswerURL(resp1.getLocalization()).setIdConversation(convID)
                    .setIdUser(userID).build(); // GET para la respuesta
            blockingStub.promptGET(req2);
        } catch (StatusRuntimeException e) {
            return Optional.empty();
        }

        return dao.getConvByID(userID, convID);
    }

    public boolean checkUserAuth(String userID, String date, String authToken, String URI) {
        Optional<User> u = getUserById(userID);
        if(u.isPresent()) {
            User user = u.get();
            ZoneId zoneId = ZoneId.of("Europe/Madrid");
            LocalDate dateObj = LocalDate.now(zoneId);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String currentDate = dateObj.format(formatter);

            String userToken = user.getToken();
            String rawToken = URI + currentDate + userToken;
            String md5Token = UserUtils.md5pass(rawToken);

            if(authToken.equals(md5Token) && date.equals(currentDate)) {
                return true;
            }
        }
        
        return false;
    }
}
