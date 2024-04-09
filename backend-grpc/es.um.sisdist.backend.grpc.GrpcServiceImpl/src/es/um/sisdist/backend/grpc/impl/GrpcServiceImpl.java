package es.um.sisdist.backend.grpc.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.bson.Document;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.POSTRequest;
import es.um.sisdist.backend.grpc.POSTResponse;
import es.um.sisdist.backend.dao.models.Conversation;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.user.MongoConvDAO;
import es.um.sisdist.backend.dao.user.MongoDialogueDAO;
import es.um.sisdist.backend.dao.user.MongoUserDAO;
import es.um.sisdist.backend.grpc.GETRequest;
import es.um.sisdist.backend.grpc.GETResponse;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;

	
    public GrpcServiceImpl(Logger logger) 
    {
		super();
		this.logger = logger;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) 
	{
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
			connection.setRequestProperty("Content-Type", "application/json");

			// Habilitar envío de datos
			connection.setDoOutput(true);

			// Crear el cuerpo de la petición
			String jsonInputString = "{\"prompt\": \"" + request.getPrompt() + "\"}";

			try(DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				wr.writeBytes(jsonInputString);
			}

			int responseCode = connection.getResponseCode();
			logger.info("Response Code: " + responseCode);

			Map<String, List<String>> headerFields = connection.getHeaderFields();

            // Imprimir todos los headers
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                System.out.println("Key: " + key + " Value: " + value);
            }

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.info(response.toString());

			POSTResponse resp = POSTResponse.newBuilder().setLocalization(connection.getHeaderField("Location")).build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void promptGET(GETRequest request, StreamObserver<GETResponse> responseObserver) 
	{
		try {
			StringBuffer response = new StringBuffer();
			String idDialogo = request.getAnswerURL().split("/")[2];
			
			URL url = new URL("http://llamachat:5020" + request.getAnswerURL());
			new InnerGrpcServiceImplToLlama(url,request.getIdUser(),idDialogo,request.getIdConversation()).run();
			GETResponse resp = GETResponse.newBuilder().setAnswerText(idDialogo).build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}


 class InnerGrpcServiceImplToLlama extends Thread{
	private URL urlLlamachat;
	private String idDialogo;
	private String idConv;
	private String idUser;
	MongoUserDAO userDAO = new MongoUserDAO();
	public InnerGrpcServiceImplToLlama(URL connection, String idUser, String idDialogo, String idConv) {
		this.urlLlamachat = connection;
		this.idUser= idUser;
		this.idDialogo = idDialogo;
		this.idConv = idConv;
	}
	@Override
	public void run(){
		super.run();
		StringBuffer response = new StringBuffer();
		HttpURLConnection connection;
		int responseCode;
		while(true) {
			try {
				connection = (HttpURLConnection) urlLlamachat.openConnection();
				connection.setRequestMethod("GET");
				responseCode = connection.getResponseCode();
				System.out.println("Response Code: " + responseCode);
				if(responseCode == 200) {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            		String inputLine;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					break;				
				}else {
					 sleep(5000);
					 continue;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			}
		}

		Document doc = Document.parse(response.toString());
		String answer = doc.getString("answer");
		userDAO.addResponse(idUser, idConv, idDialogo, answer);
	}




	
}