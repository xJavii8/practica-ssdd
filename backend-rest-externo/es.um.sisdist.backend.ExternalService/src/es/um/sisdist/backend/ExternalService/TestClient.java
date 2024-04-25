package es.um.sisdist.backend.ExternalService;

import java.io.StringReader;
import java.net.URI;
import java.time.Instant;
import java.util.Scanner;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

public class TestClient {
	private static WebTarget service;
	private static String userID;
	private static String token;
	private static String convID;
	private static String convNext;
	private static String convEnd;

	private static final String TEST_USERNAME = "testClientUser";
	private static final String TEST_PASSWORD = "test";
	private static final String TEST_EMAIL = "testClient@test.com";
	private static final String TEST_CONV_NAME = "testConv";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_RESET = "\u001B[0m";

	public static void main(String[] args) {
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		TestClient.service = client.target(getBaseURI());
		System.out.println(ANSI_RED + "INICIO DE LOS TESTS\n" + ANSI_RESET);
		registerUser(); // Registro del usuario
		createConv(); // Creación de la conversación
		getConvData(); // Conversación vacía (abierta)
		sendPrompts(); // Envío de mensajes
		endConv(); // Finalización de la conversación
		getConvData(); // Conversación con mensajes (borrada)
		delConv(); // Eliminación de la conversación
		delUser(); // Eliminación del usuario
		System.out.println(ANSI_RED + "TODOS LOS TESTS FINALIZADOS" + ANSI_RESET);
	}

	public static void registerUser() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Registro de un usuario" + ANSI_RESET);
		JsonObject json = Json.createObjectBuilder().add("email", TEST_EMAIL).add("name", TEST_USERNAME)
				.add("password", TEST_PASSWORD).build();
		Response response = service.path("u").path("register").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));

		if (response.getStatus() == 200) {
			String strJSON = response.readEntity(String.class);
			JsonReader jsonReader = Json.createReader(new StringReader(strJSON));
			JsonObject receivedJSON = jsonReader.readObject();
			jsonReader.close();
			userID = receivedJSON.getString("id");
			token = receivedJSON.getString("token");
			System.out.println(ANSI_GREEN + "UserID: " + ANSI_PURPLE + userID + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Token: " + ANSI_PURPLE + token + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Usuario registrado correctamente" + ANSI_RESET);
		} else {
			System.err.println(ANSI_RESET + "Error al registrar el usuario. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Registro de un usuario\n" + ANSI_RESET);
	}

	public static void createConv() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Creación de una conversación" + ANSI_RESET);
		JsonObject json = Json.createObjectBuilder().add("convName", TEST_CONV_NAME).build();
		Response response = service.path("u").path(userID).path("dialogue").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));

		if (response.getStatus() == 201) {
			String strJSON = response.readEntity(String.class);
			JsonReader jsonReader = Json.createReader(new StringReader(strJSON));
			JsonObject receivedJSON = jsonReader.readObject();
			jsonReader.close();
			convID = receivedJSON.getString("ID");
			convNext = receivedJSON.getString("nextURL");
			convEnd = receivedJSON.getString("endURL");
			System.out.println(ANSI_GREEN + "ConvID: " + ANSI_PURPLE + convID + ANSI_RESET);
			System.out.println(ANSI_GREEN + "ConvNextURL: " + ANSI_PURPLE + convNext + ANSI_RESET);
			System.out.println(ANSI_GREEN + "ConvEndURL: " + ANSI_PURPLE + convEnd + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Conversación creada correctamente" + ANSI_RESET);
		} else {
			System.err.println(ANSI_RESET + "Error al crear la conversación. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Creación de una conversación\n" + ANSI_RESET);
	}

	public static void getConvData() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Obtención de los datos de una conversación" + ANSI_RESET);
		Response response = service.path("u").path(userID).path("dialogue").path(convID)
				.request(MediaType.APPLICATION_JSON).get();

		if (response.getStatus() == 200) {
			String strJSON = response.readEntity(String.class);
			JsonReader jsonReader = Json.createReader(new StringReader(strJSON));
			JsonObject receivedJSON = jsonReader.readObject();
			jsonReader.close();
			System.out.println(ANSI_GREEN + "Conv: " + ANSI_PURPLE + receivedJSON + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Datos de la conversación obtenidos correctamente" + ANSI_RESET);
		} else {
			System.err.println(
					ANSI_RESET + "Error al obtener los datos de la conversación. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Obtención de los datos de una conversación\n" + ANSI_RESET);
	}

	public static void sendPrompts() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Envío de mensajes" + ANSI_RESET);
		Scanner scanner = new Scanner(System.in);
		String prompt;
		JsonObject json;
		long timestamp;
		while (true) {
			System.out.print(ANSI_GREEN + "Introduce un prompt. Para salir, escribe " + ANSI_RESET + "\"q\""
					+ ANSI_GREEN + ": " + ANSI_RESET);
			prompt = scanner.nextLine();
			System.out.println(ANSI_GREEN + "Prompt: " + ANSI_PURPLE + prompt + ANSI_RESET);
			if (prompt.equals("q")) {
				scanner.close();
				System.out.println(ANSI_GREEN + "Saliendo..." + ANSI_RESET);
				break;
			} else {
				System.out.println(ANSI_GREEN + "Enviando prompt..." + ANSI_RESET);
				timestamp = Instant.now().toEpochMilli();
				json = Json.createObjectBuilder().add("userID", userID).add("convID", convID).add("prompt", prompt)
						.add("timestamp", timestamp).build();
				WebTarget targetNext = service.path(convNext.replaceFirst("^/", ""));
				Response response = targetNext.request(MediaType.APPLICATION_JSON)
						.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
				if (response.getStatus() == 200) {
					String strJSON = response.readEntity(String.class);
					JsonReader jsonReader = Json.createReader(new StringReader(strJSON));
					JsonObject receivedJSON = jsonReader.readObject();
					jsonReader.close();
					System.out.println(ANSI_GREEN + "Conversación: " + ANSI_PURPLE + receivedJSON + ANSI_RESET);
					System.out.println(ANSI_GREEN + "Respuesta obtenida correctamente" + ANSI_RESET);
				} else {
					System.err.println(ANSI_RESET + "Error al enviar el prompt. Status code: " + response.getStatus());
				}
			}
		}

		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Envío de mensajes\n" + ANSI_RESET);
	}

	public static void endConv() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Finalización de una conversación" + ANSI_RESET);
		WebTarget targetEnd = service.path(convEnd.replaceFirst("^/", ""));
		Response response = targetEnd.request(MediaType.APPLICATION_JSON).post(null);

		if (response.getStatus() == 200) {
			convNext = null;
			convEnd = null;
			System.out.println(ANSI_GREEN + "ConvID: " + ANSI_PURPLE + convID + ANSI_RESET);
			System.out.println(ANSI_GREEN + "ConvNextURL: " + ANSI_PURPLE + convNext + ANSI_RESET);
			System.out.println(ANSI_GREEN + "ConvEndURL: " + ANSI_PURPLE + convEnd + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Conversación finalizada correctamente" + ANSI_RESET);
		} else {
			System.err.println(ANSI_RESET + "Error al finalizar la conversación. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Finalización de una conversación\n" + ANSI_RESET);
	}

	public static void delConv() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Eliminación de una conversación" + ANSI_RESET);
		Response response = service.path("u").path(userID).path("dialogue").path(convID).path("del")
				.request(MediaType.APPLICATION_JSON).delete();

		if (response.getStatus() == 200) {
			convID = null;
			System.out.println(ANSI_GREEN + "ConvID: " + ANSI_PURPLE + convID + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Conversación eliminada correctamente" + ANSI_RESET);
		} else {
			System.err.println(ANSI_RESET + "Error al eliminar la conversación. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Eliminación de una conversación\n" + ANSI_RESET);
	}

	public static void delUser() {
		System.out.println(ANSI_GREEN + "TEST INICIADO: Eliminación de un usuario" + ANSI_RESET);
		Response response = service.path("u").path("deleteUser").path(userID).request(MediaType.APPLICATION_JSON)
				.delete();

		if (response.getStatus() == 200) {
			userID = null;
			token = null;
			System.out.println(ANSI_GREEN + "UserID: " + ANSI_PURPLE + userID + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Token: " + ANSI_PURPLE + token + ANSI_RESET);
			System.out.println(ANSI_GREEN + "Usuario eliminado correctamente" + ANSI_RESET);
		} else {
			System.err.println(ANSI_RESET + "Error al eliminar el usuario. Status code: " + response.getStatus());
		}
		System.out.println(ANSI_GREEN + "TEST FINALIZADO: Eliminación de un usuario\n" + ANSI_RESET);
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8180/ExternalService/").build();
	}
}