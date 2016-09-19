

/*
Kaic N. Bastidas - 8516048
Bruno I. Murozaki - 8516476

Atividade 2 - Parte A
*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

	private static final int PORT = 6789;
	private ServerSocket socket;
	
	
	public WebServer(){

		// Estabelecer o socket de escuta.
		try {
			socket = new ServerSocket(PORT);
			
			while (true)  {

				// Instancia um objeto Socket para a requisição.
				Socket client = socket.accept();

				//Construir um objeto para processar a mensagem de requisição HTTP.
				HttpRequest request = new HttpRequest (client);
				// Criar um novo thread para processar a requisição.
				Thread thread = new Thread(request);
				//Iniciar o thread.
				thread.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
