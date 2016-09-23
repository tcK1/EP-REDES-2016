import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class HttpRequest implements Runnable{

	final static String CRLF = "\r\n";
	Socket socket;
	private WebServer wb;
	public static boolean waitingForLogin;
	

	// Construtor
	public HttpRequest(Socket socket, WebServer wb) throws Exception {
		this.socket = socket;
		this.wb = wb;
	}

	// Implemente o metodo run() da interface Runnable.
	public void run() {

		try {
			processRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static int sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		// Construir um buffer de 1K para comportar os bytes no caminho para o socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;
		// Copiar o arquivo requisitado dentro da cadeia de saida do socket.
		int bytesSent = 0;
		while((bytes = fis.read(buffer)) != -1 ) {
			os.write(buffer, 0, bytes);
			bytesSent++;
		}

		return bytesSent;
	}
	
	private static int sendChars(String fis, OutputStream os) throws Exception {
		// Construir um buffer de 1K para comportar os bytes no caminho para o socket.
		int bytes = 0;
		// Copiar o arquivo requisitado dentro da cadeia de saida do socket.
		int bytesSent = 0;
		os.write(fis.getBytes(), 0, bytes);
		bytes = fis.length();

		return bytesSent;
	}
	

	private static void writeHeader(String statusLine, String contentTypeLine, DataOutputStream os) throws Exception {
		// Enviar a linha de status.
		os.writeBytes(statusLine);
		// Enviar a linha de tipo de conteudo.
		os.writeBytes(contentTypeLine);
		// Enviar uma linha em branco para indicar o fim das linhas de cabecalho.
		os.writeBytes(CRLF);
	}

	private static String contentType ( String fileName ) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".txt") || fileName.endsWith(".java")) {
			return "text/plain";
		}
		if(fileName.endsWith(".gif")) {
			return "image/gif";
		}
		if(fileName.endsWith(".png") || fileName.endsWith(".x-png")) {
			return "image/png";
		}
		if(fileName.endsWith(".jfif") || fileName.endsWith(".jfif-tbnl") || fileName.endsWith(".jpe") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
			return "image/jpeg";
		}
		if(fileName.endsWith(".xml")) {
			return "application/xml";
		}
		if(fileName.endsWith(".json")) {
			return "application/json";
		}

		return "application/octet-stream";
	}


	private void processRequest() throws Exception {

		// Obter uma referência para os trechos de entrada e saída do socket.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// Ajustar os filtros do trecho de entrada.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		HTTP message = HTTPParser.bufferToHTTP(br);
		
		String fileName = message.getHttpFile();
		String method = message.getMethod();
		
		if(waitingForLogin){
			waitingForLogin = false;
			
			boolean canLogin = wb.loginClient(message);
			
			if(!canLogin) {
				writeProtectedDirectory(os);
				return;
			}
				
		} else if(wb.isProtectedDir(fileName)){
			writeProtectedDirectory(os);
			return;
		}
			
		File file = new File(fileName);
		FileInputStream fis = null;

		switch(method){
			case "GET":
			case "HEAD":
				// Ve se o arquivo ou diretório existe
				if(file.exists()){
					if(file.isFile()){ // Se for arquivo

						sendServerFile(os, fileName, method);

					} else if(wb.getShowDirectories() == 1) { // Não mostra os diretórios, só os arquivos

						writeUnauthorizedDirectory(os, method);

					} else if(wb.getShowDirectories() == 2) { // Mostra um index padrão caso ele exista

						File indexFile = new File("index.html");
						if(!indexFile.exists()){
							writeUnauthorizedDirectory(os, method);
						} else {
							sendServerFile(os, "index.html", method);
						}

					} else if(file.isDirectory()){ // Se for diretório

						writeDirectory(os, file, fileName, method);

					}
				} else {

					fileNotFound(os, method);

				}
				break;
			case "POST":
				writeResponsePost(os, br, fileName);
				break;
		}
		
		printHTTP(message);

		// Feche as cadeias e socket.
		os.close();
		br.close();
		//socket.close();

	}
	
	private static void printHTTP(HTTP message){
		
		System.out.println(message.getMethod() + " " + message.getHttpFile() + " " + message.getHttpVersion() + CRLF);
		
		for(String key: message.getHeaderFields().keySet()){
			System.out.println(key + ": " + message.getHeaderFields().get(key));
		}
		
		
	}

	
	private void writeResponsePost(DataOutputStream os, BufferedReader br, String fileName) throws Exception {
		String statusLine;
		String contentTypeLine;
		String entityBody;
		
		statusLine = "HTTP/1.0 200";
		contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;
		entityBody = "<HTML>" +
					"<HEAD><TITLE>POST DATA</TITLE></HEAD>" +
					"<BODY>";
					
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0){
			entityBody = entityBody + headerLine;
		}

		// Dados do POST
		StringBuilder payload = new StringBuilder();
		while(br.ready()){
			payload.append((char) br.read());
		}
		
		entityBody = entityBody + payload.toString();
	
	}
	
	private void writeProtectedDirectory(DataOutputStream os) throws Exception{
		String statusLine;
		String authenticationLine = "WWW-Authenticate: Basic realm=\"User Visible Realm\"" + CRLF;
		String entityBody;
		
		statusLine = "HTTP/1.0 401"+CRLF;

		writeHeader(statusLine, authenticationLine, os);

		// Escreve corpo da mensagem
		sendChars("sendBytes", os);
		
		waitingForLogin = true;
		
	}
	
	
	private void sendServerFile(DataOutputStream os, String fileName, String MIME)
			throws FileNotFoundException, Exception, IOException {
		String statusLine;
		String contentTypeLine;
		String entityBody;
		switch (MIME) {
			case "GET":
				FileInputStream fis;
				// Abrir o arquivo requisitado.
				fis = new FileInputStream(fileName);
				statusLine = "HTTP/1.0 200";
				contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;

				writeHeader(statusLine, contentTypeLine, os);

				// Escreve corpo da mensagem
				sendBytes(fis, os);
				fis.close();
				break;
			case "HEAD":
				// Abrir o arquivo requisitado.
				statusLine = "HTTP/1.0 200";
				contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;

				writeHeader(statusLine, contentTypeLine, os);

				break;
		}
	}

	private void fileNotFound(DataOutputStream os, String MIME) throws Exception {
		String statusLine;
		String contentTypeLine;
		switch (MIME) {
			case "GET":
				String entityBody;

				statusLine = "HTTP/1.0 404";
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>" +
					"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
					"<BODY>Not Found</BODY></HTML>";

				writeHeader(statusLine, contentTypeLine, os);

				// Escreve o corpo da mesagem.
				os.writeBytes(entityBody);
				break;
			case "HEAD":
				statusLine = "HTTP/1.0 404";
				contentTypeLine = "Content-type: text/html" + CRLF;

				writeHeader(statusLine, contentTypeLine, os);
				break;
		}
	}

	private void writeDirectory(DataOutputStream os, File directory, String path, String MIME) throws Exception {
		String statusLine;
		String contentTypeLine;
		switch (MIME) {
			case "GET":
				String entityBody;

				statusLine = "HTTP/1.0 200";
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>" +
					"<HEAD><TITLE>"+path+"</TITLE></HEAD>" +
					"<BODY>";

				File[] filesList = directory.listFiles();
				for(File f : filesList){
					if(path.equals("./")){
						if(f.isDirectory())
							entityBody = entityBody + "<A HREF='/"+f.getName()+"'>"+f.getName()+"/</A></BR>";
						if(f.isFile()){
							entityBody = entityBody + "<A HREF='/"+f.getName()+"'>"+f.getName()+"</A></BR>";
						}
					} else {
						if(f.isDirectory())
							entityBody = entityBody + "<A HREF='/"+path+"/"+f.getName()+"'>"+f.getName()+"/</A></BR>";
						if(f.isFile()){
							entityBody = entityBody + "<A HREF='/"+path+"/"+f.getName()+"'>"+f.getName()+"</A></BR>";
						}
					}
				}

				entityBody = entityBody + "</BODY></HTML>";
				writeHeader(statusLine, contentTypeLine, os);

				// Escreve o corpo da mesagem.
				os.writeBytes(entityBody);
				break;
			case "HEAD":
				statusLine = "HTTP/1.0 200";
				contentTypeLine = "Content-type: text/html" + CRLF;
				writeHeader(statusLine, contentTypeLine, os);

				break;
		}
	}

	private void writeUnauthorizedDirectory(DataOutputStream os, String MIME)
			throws Exception, IOException {
		String statusLine;
		String contentTypeLine;
		switch (MIME) {
			case "GET":
				String entityBody;
				statusLine = "HTTP/1.0 401";
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>" +
					"<HEAD><TITLE>Diretorio</TITLE></HEAD>" +
					"<BODY>Conteudo nao pode ser mostrado</BODY></HTML>";

				writeHeader(statusLine, contentTypeLine, os);

				// Escreve o corpo da mesagem.
				os.writeBytes(entityBody);
				break;
			case "HEAD":
				statusLine = "HTTP/1.0 401";
				contentTypeLine = "Content-type: text/html" + CRLF;
				
				writeHeader(statusLine, contentTypeLine, os);

				break;
		}
	}

}
