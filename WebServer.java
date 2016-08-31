import java.io.* ;
import java.net.* ;
import java.util.* ;

public final class WebServer {

	public static void main(String arvg[]) throws Exception {
		int port = 6789;
	
		// Estabelecer o socket de escuta.
		ServerSocket sock = new ServerSocket(port);
		
		
		// Processar a requisiÃ§Ã£o de serviÃ§o HTTP em um laÃ§o infinito.
		while (true)  {	
		
			Socket client = sock.accept();
			//Construir um objeto para processar a mensagem de requisiÃ§Ã£o HTTP.
			HttpRequest request = new HttpRequest ( client );
			// Criar um novo thread para processar a requisiÃ§Ã£o.
			Thread thread = new Thread(request);
			//Iniciar o thread.
			thread.start();
		}
	
	}
}
final class HttpRequest implements Runnable {
	
	final static String CRLF = "\r\n";
	Socket socket;
	
	// Construtor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}
	
	// Implemente o mÃ©todo run() da interface Runnable.
	public void run() {
		
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
	
	private void processRequest() throws Exception {
		
		// Obter uma referÃªncia para os trechos de entrada e saÃ­da do socket.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		// Ajustar os filtros do trecho de entrada.
		//?
		BufferedReader br = ?;
		
		// Obter a linha de requisiÃ§Ã£o da mensagem de requisiÃ§Ã£o HTTP.
		String requestLine = ?;
		
		//  Exibir a linha de requisiÃ§Ã£o.
		System.out.println();
		System.out.println(requestLine);

		// Obter e exibir as linhas de cabeÃ§alho.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		// Feche as cadeias e socket.
		os.close();
		br.close();
		socket.close();
		
	}
