import java.io.BufferedReader;
import java.io.IOException;


public class HTTPParser {

	public static HTTP stringToHTTP(String str){
		
		
		return null;
	}
	
	public static HTTP bufferToHTTP(BufferedReader buffer){
		
		String line = null;
		String[] splittedLine = null;
		HTTP http = new HTTP();
		
		String headerKey = null, headerVal = null;

		try {
			// Primeira Linha do HTTP
			line = buffer.readLine();
			splittedLine = line.split("\\s");
		
			http.setMethod(splittedLine[0]);
			http.setHttpFile("." + splittedLine[1]);
			http.setHttpVersion(splittedLine[2]);
			
			while((line = buffer.readLine()).length() != 0){
				splittedLine = line.split(":", 2);
				
				headerKey = splittedLine[0];
				headerVal = splittedLine[1];
				
				http.addHeader(headerKey, headerVal);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return http;
	}
	
	
}
