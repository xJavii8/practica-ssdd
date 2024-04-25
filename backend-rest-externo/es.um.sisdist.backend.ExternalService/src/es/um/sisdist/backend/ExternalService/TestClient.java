package es.um.sisdist.backend.ExternalService;

public class TestClient {
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_RESET = "\u001B[0m";

	public static void main(String[] args) {
		System.out.println(ANSI_RED + "Por favor, utiliza el cliente que se encuentra en la raíz del proyecto, llamado \"externalServiceClient.py\"" + ANSI_RESET);
	}
}