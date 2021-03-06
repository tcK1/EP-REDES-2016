/*
Kaic N. Bastidas - 8516048
Bruno I. Murozaki - 8516476
*/

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {

	public static void main(String[] args) {

		try {
			Properties p = new Properties();
			InputStream is = new FileInputStream("server.config");
			p.load(is);

			int port = Integer.parseInt(p.getProperty("port"));
			int showDirs = Integer.parseInt(p.getProperty("showOptions"));
			String protectedDir = p.getProperty("protectedDir");
			String user = p.getProperty("user");
			String pass = p.getProperty("pass");

			System.out.println("Abrindo o servidor...");
			
			WebServer wb = new WebServer(port, showDirs, protectedDir, user, pass);
			wb.startAcceptingClients();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
