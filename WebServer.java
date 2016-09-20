

/*
Kaic N. Bastidas - 8516048
Bruno I. Murozaki - 8516476

Atividade 2 - Parte A
*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
	
	private ServerSocket socket;
	private int port;
	private boolean showDirectories;
	
	public WebServer(int port, boolean showDirectories){
		
		this.port = port;
		this.showDirectories = showDirectories;
		
	}


	public void startAcceptingClients() {
		try {
			socket = new ServerSocket(this.port);
			
			while (true)  {
				// Instancia um objeto Socket para a requisição.
				Socket client;
				try {
					client = socket.accept();
					//Construir um objeto para processar a mensagem de requisição HTTP.
					HttpRequest request = new HttpRequest (client, this);
					// Criar um novo thread para processar a requisição.
					Thread thread = new Thread(request);
					//Iniciar o thread.
					thread.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isShowDirectories() {
		return showDirectories;
	}
	
}
