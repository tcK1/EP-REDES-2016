

/*
Kaic N. Bastidas - 8516048
Bruno I. Murozaki - 8516476

Atividade 2 - Parte A
*/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.logging.*;

public class WebServer {

	private ServerSocket socket;
	private int port;
	private int showDirectories;
	private String protectedDir;
	private String user;
	private String pass;

	Logger logger;
	FileHandler fh;
	SimpleFormatter formatter;

	public WebServer(int port, int showDirectories, String protectedDir, String user, String pass) throws Exception {
		this.port = port;
		this.showDirectories = showDirectories;
		this.protectedDir = protectedDir;
		this.user = user;
		this.pass = pass;

		this.logger = Logger.getLogger("LOG");
		logger.setUseParentHandlers(false);
		this.fh = new FileHandler("./Log.txt", true);
		logger.addHandler(fh);
		this.formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}


	public boolean isProtectedDir(String fileName){

		String[] splittedPath = fileName.split("/");

		for(String s : splittedPath){
			if(s.equals(this.protectedDir))
				return true;
		}


		return false;
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

	public boolean loginClient(HTTP http){
		String auth = http.getHeaderValue("Authorization");
		String key = auth.split("\\s")[2];

		String decoded = new String(Base64.getDecoder().decode(key));

		String[] login = decoded.split(":");

		if(login[0].equals(this.user) && login[1].equals(this.pass))
			return true;

		return false;
	}

	public int getShowDirectories() {
		return showDirectories;
	}

	public String getProtectedDir() {
		return protectedDir;
	}
}
