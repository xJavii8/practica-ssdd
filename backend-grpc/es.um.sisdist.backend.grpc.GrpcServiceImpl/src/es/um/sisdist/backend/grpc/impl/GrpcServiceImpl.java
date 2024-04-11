package es.um.sisdist.backend.grpc.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Logger;

import org.bson.Document;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.POSTRequest;
import es.um.sisdist.backend.grpc.POSTResponse;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.grpc.GETRequest;
import es.um.sisdist.backend.grpc.GETResponse;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
	private Logger logger;

	public GrpcServiceImpl(Logger logger) {
		super();
		this.logger = logger;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		logger.info("Recived PING request, value = " + request.getV());
		responseObserver.onNext(PingResponse.newBuilder().setV(request.getV()).build());
		responseObserver.onCompleted();
	}

	@Override
	public void promptPOST(POSTRequest request, StreamObserver<POSTResponse> responseObserver) {
		try {
			URL url = new URL("http://llamachat:5020/prompt");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

			// Habilitar envío de datos
			connection.setDoOutput(true);

			// Crear el cuerpo de la petición
			Document doc = new Document("prompt", request.getPrompt());
			String json = doc.toJson();

			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = json.getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			int responseCode = connection.getResponseCode();
			logger.info("POST realizado.\nPrompt: " + json + "\nRespuesta: " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			POSTResponse resp = POSTResponse.newBuilder().setLocalization(connection.getHeaderField("Location"))
					.build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void promptGET(GETRequest request, StreamObserver<GETResponse> responseObserver) {
		try {
			String locationID = request.getAnswerURL().split("/")[2];
			URL url = new URL("http://llamachat:5020" + request.getAnswerURL());
			new InnerGrpcServiceImplToLlama(url, request.getIdUser(), locationID, request.getIdConversation()).run();
			GETResponse resp = GETResponse.newBuilder().setAnswerText(locationID).build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class InnerGrpcServiceImplToLlama extends Thread {
	private URL url;
	private String locationID;
	private String convID;
	private String userID;
	IDAOFactory daoFactory;
	IUserDAO dao;

	public InnerGrpcServiceImplToLlama(URL connection, String userID, String locationID, String convID) {
		this.url = connection;
		this.userID = userID;
		this.locationID = locationID;
		this.convID = convID;

		daoFactory = new DAOFactoryImpl();
		Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));

		if (backend.isPresent() && backend.get().equals("mongo"))
			dao = daoFactory.createMongoUserDAO();
		else
			dao = daoFactory.createSQLUserDAO();
	}

	@Override
	public void run() {
		super.run();
		StringBuffer response = new StringBuffer();
		HttpURLConnection connection;
		int responseCode;
		System.out.println("URL: " + url.toString());

		while (true) {
			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				responseCode = connection.getResponseCode();
				System.out.println("GET realizado. Respuesta: " + responseCode);

				if (responseCode == 200) {
					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

					String inputLine;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}

					in.close();
					break;
				} else {
					sleep(5000);
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Document doc = Document.parse(response.toString());
		String answer = doc.getString("answer");
		dao.addResponse(userID, convID, locationID, answer);
	}

}