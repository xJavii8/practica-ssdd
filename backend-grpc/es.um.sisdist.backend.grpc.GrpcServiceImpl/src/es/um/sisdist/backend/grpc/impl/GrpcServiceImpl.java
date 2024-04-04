package es.um.sisdist.backend.grpc.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.POSTRequest;
import es.um.sisdist.backend.grpc.POSTResponse;
import es.um.sisdist.backend.dao.models.Conversacion;
import es.um.sisdist.backend.dao.models.Dialogo;
import es.um.sisdist.backend.dao.user.MongoConversacionDAO;
import es.um.sisdist.backend.dao.user.MongoDialogoDAO;
import es.um.sisdist.backend.grpc.GETRequest;
import es.um.sisdist.backend.grpc.GETResponse;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;
	MongoDialogoDAO dialogoDAO = new MongoDialogoDAO();
	MongoConversacionDAO conversacionDAO = new MongoConversacionDAO();
	
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
			Conversacion conv = conversacionDAO.getConversacionById(request.getIdConversation()).get();
			String idDialogo = request.getAnswerURL().split("/")[2];
			logger.info(idDialogo);
			//conversacionDAO.modifyConversacion(conv.getId(), Conversacion.BUSY);
			while(true) {

				URL url = new URL("http://llamachat:5020" + request.getAnswerURL());
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				connection.setRequestMethod("GET");
				int responseCode = connection.getResponseCode();
				System.out.println("Response Code: " + responseCode);

				if(responseCode == 200) {
					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            		String inputLine;
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					Optional<Dialogo> dialogo = dialogoDAO.modifyDialogo(idDialogo, "", Optional.empty(), response.toString());
					conversacionDAO.addDialogo(conv.getId(), idDialogo);
					//conversacionDAO.modifyConversacion(conv.getId(), Conversacion.READY);
					System.out.println(response.toString());
					break;
				} else {
					Thread.sleep(5000);
					continue;
				}
			}

			GETResponse resp = GETResponse.newBuilder().setAnswerText(response.toString()).build();
			responseObserver.onNext(resp);
			responseObserver.onCompleted();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}


/*
	@Override
	public void storeImage(ImageData request, StreamObserver<Empty> responseObserver)
    {
		logger.info("Add image " + request.getId());
    	imageMap.put(request.getId(),request);
    	responseObserver.onNext(Empty.newBuilder().build());
    	responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<ImageData> storeImages(StreamObserver<Empty> responseObserver) 
	{
		// La respuesta, sólo un objeto Empty
		responseObserver.onNext(Empty.newBuilder().build());

		// Se retorna un objeto que, al ser llamado en onNext() con cada
		// elemento enviado por el cliente, reacciona correctamente
		return new StreamObserver<ImageData>() {
			@Override
			public void onCompleted() {
				// Terminar la respuesta.
				responseObserver.onCompleted();
			}
			@Override
			public void onError(Throwable arg0) {
			}
			@Override
			public void onNext(ImageData imagedata) 
			{
				logger.info("Add image (multiple) " + imagedata.getId());
		    	imageMap.put(imagedata.getId(), imagedata);	
			}
		};
	}

	@Override
	public void obtainImage(ImageSpec request, StreamObserver<ImageData> responseObserver) {
		// TODO Auto-generated method stub
		super.obtainImage(request, responseObserver);
	}

	@Override
	public StreamObserver<ImageSpec> obtainCollage(StreamObserver<ImageData> responseObserver) {
		// TODO Auto-generated method stub
		return super.obtainCollage(responseObserver);
	}
	*/
}