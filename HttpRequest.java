import java.io.*;
import java.net.*;
import java.util.*;

public class HttpRequest implements Runnable{

	final static String CRLF = "\r\n";
	Socket socket;
	private WebServer wb;

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
			System.out.println(e);
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

		// Obter a linha de requisição da mensagem de requisição HTTP.
		String requestLine = br.readLine();

		// Extrair o nome do arquivo a linha de requisição.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // pular o método, que deve ser "GET"
		String fileName = tokens.nextToken();
		// Acrescente um "." de modo que a requisição do arquivo esteja dentro do diretório atual.
		fileName = "." + fileName;

		if(wb.isProtectedDir(fileName)){
			writeProtectedDirectory(os);
		}
		
		// Instancia variaveis do arquivo
		File file = new File(fileName);
		FileInputStream fis = null;

		
		// Instancia variaveis de resposta
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		String[] parts = requestLine.split(" ");
		switch(parts[0]){
			case "GET":
			case "HEAD":
				// Ve se o arquivo ou diretório existe
				if(file.exists()){
					if(file.isFile()){ // Se for arquivo

						sendServerFile(os, fileName, parts[0]);

					} else if(wb.getShowDirectories() == 1) { // Não mostra os diretórios, só os arquivos

						writeUnauthorizedDirectory(os, parts[0]);

					} else if(wb.getShowDirectories() == 2) { // Mostra um index padrão caso ele exista

						File indexFile = new File("index.html");
						if(!indexFile.exists()){
							writeUnauthorizedDirectory(os, parts[0]);
						} else {
							sendServerFile(os, "index.html", parts[0]);
						}

					} else if(file.isDirectory()){ // Se for diretório

						writeDirectory(os, file, fileName, parts[0]);

					}
				} else {

					fileNotFound(os, parts[0]);

				}
				break;
			case "POST":
				writeResponsePost(os, br, fileName);
				break;
		}

		//  Exibir a linha de requisição.
		System.out.println();
		System.out.println(requestLine);

		// Obter e exibir as linhas de cabeçalho.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}

		/*
		// Imprime os arquivos e pastas do diretorio raiz
		File curDir = new File(".");
		File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isDirectory())
                System.out.println(f.getName());
            if(f.isFile()){
                System.out.println(f.getName());
            }
        }
		*/

		// ip e porta
		System.out.println(socket.toString());
		// só o ip
		//System.out.println(socket.getInetAddress().toString());
		// conteudo requisitado
		System.out.println(fileName);
		// hora
		System.out.println(Calendar.getInstance().getTime().toString());
		// quantidade de dados transmitidos
		System.out.println(os.size());
		// tipo da requisição
		System.out.println(parts[0]);

		// Feche as cadeias e socket.
		os.close();
		br.close();
		socket.close();

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
