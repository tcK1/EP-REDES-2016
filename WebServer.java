import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer {

	public static void main(String arvg[]) throws Exception {
		int port = 6789;
	
		// Estabelecer o socket de escuta.
		?
		// Processar a requisição de serviço HTTP em um laço infinito.
		While (true)  {
			// Escutar requisição de conexão TCP.
			?
			
			//Construir um objeto para processar a mensagem de requisição HTTP.
			HttpRequest request = new HttpRequest ( ? );
			// Criar um novo thread para processar a requisição.
			Thread thread = new Thread(request);
			//Iniciar o thread.
			Thread.start();

			
		}
	}
}
final class HttpRequest implements Runnable {
	
	final static String CRLF = “\r\n”;
	Socket socket;
	
	// Construtor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	// Implemente o método run() da interface Runnable.
	Public void run() {
		
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	private void processRequest() throws Exception {
		
		// Obter uma referência para os trechos de entrada e saída do socket.
		InputStream is = ?;
		DataOutputStream os = ?;
		// Ajustar os filtros do trecho de entrada.
		?
		BufferedReader br = ?;
		
		// Obter a linha de requisição da mensagem de requisição HTTP.
		String requestLine = ?;
		//  Exibir a linha de requisição.
		System.out.println();
		System.out.println(requestLine);

		// Obter e exibir as linhas de cabeçalho.
		String headerLine = null;
		While ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		// Feche as cadeias e socket.
		os.close();
		br.close();
		socket.close();
		
	}

}
