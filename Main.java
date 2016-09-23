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
			
			System.out.println(showDirs);
			
			WebServer wb = new WebServer(port, showDirs, protectedDir);
			wb.startAcceptingClients();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
