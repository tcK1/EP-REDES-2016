import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.Socket;
import java.util.StringTokenizer;

public class HttpRequest implements Runnable{

	final static String CRLF = "\r\n";
	Socket socket;
	private WebServer wb;

	// Construtor
	public HttpRequest(Socket socket, WebServer wb) throws Exception {
		this.socket = socket;
		this.wb = wb;
	}

	// Implemente o método run() da interface Runnable.
	public void run() {

		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		// Construir um buffer de 1K para comportar os bytes no caminho para o socket.
	byte[] buffer = new byte[1024];
		int bytes = 0;
		// Copiar o arquivo requisitado dentro da cadeia de saída do socket.
		while((bytes = fis.read(buffer)) != -1 ) {
			os.write(buffer, 0, bytes);
		}
	}
	
	private static void writeHeader(String statusLine, String contentTypeLine, DataOutputStream os) throws Exception {
		// Enviar a linha de status.
		os.writeBytes(statusLine);
		// Enviar a linha de tipo de conteúdo.
		os.writeBytes(contentTypeLine);
		// Enviar uma linha em branco para indicar o fim das linhas de cabeçalho.
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
		
		// Instancia variaveis do arquivo
		File file = new File(fileName);
		FileInputStream fis = null;
		
		// Instancia variaveis de resposta
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;

		// Ve se o arquivo ou diretório existe
		if(file.exists()){
			if(file.isFile()){ // Se for arquivo
				
				sendServerFile(os, fileName);
				
			} else if(wb.getShowDirectories() == 1) {
				
				writeUnauthorizedDirectory(os);
			
			} else if(wb.getShowDirectories() == 2) {
				
				File indexFile = new File("index.html");
				if(!indexFile.exists()){
					writeUnauthorizedDirectory(os);
				} else {
					sendServerFile(os, "index.html");
				}
				
			} else if(file.isDirectory()){ // Se for diretório
				statusLine = "HTTP/1.0 200";
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>" +
					"<HEAD><TITLE>Diretório</TITLE></HEAD>" +
					"<BODY>";

				File[] filesList = file.listFiles();
				for(File f : filesList){
					if(f.isDirectory())
						//<a href="http://www.w3schools.com">Visit W3Schools</a>
						entityBody = entityBody + "<A HREF='"+fileName+f.getName()+"'>"+f.getName()+"/</A></BR>";
					if(f.isFile()){
						entityBody = entityBody + "<A HREF='"+fileName+f.getName()+"'>"+f.getName()+"</A></BR>";
					}
				}

				entityBody = entityBody + "</BODY></HTML>";
				writeHeader(statusLine, contentTypeLine, os);
					
				// Escreve o corpo da mesagem.
				os.writeBytes(entityBody);
			}
		} else {
			statusLine = "HTTP/1.0 404";
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" +
				"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
				"<BODY>Not Found</BODY></HTML>";
				
			writeHeader(statusLine, contentTypeLine, os);
				
			// Escreve o corpo da mesagem.
			os.writeBytes(entityBody);
		}
		
		//  Exibir a linha de requisição.
		System.out.println();
		System.out.println(requestLine);
		
		// Obter e exibir as linhas de cabeçalho.
		String headerLine = null;
		while ((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
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
		
		// Feche as cadeias e socket.
		os.close();
		br.close();
		socket.close();

	}

	private void sendServerFile(DataOutputStream os, String fileName)
			throws FileNotFoundException, Exception, IOException {
		FileInputStream fis;
		String statusLine;
		String contentTypeLine;
		// Abrir o arquivo requisitado.
		fis = new FileInputStream(fileName);
		statusLine = "HTTP/1.0 200";
		contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;
		
		writeHeader(statusLine, contentTypeLine, os);
		
		// Escreve corpo da mensagem
		sendBytes(fis, os);
		fis.close();
	}

	private void writeUnauthorizedDirectory(DataOutputStream os)
			throws Exception, IOException {
		String statusLine;
		String contentTypeLine;
		String entityBody;
		statusLine = "HTTP/1.0 200";
		contentTypeLine = "Content-type: text/html" + CRLF;
		entityBody = "<HTML>" +
			"<HEAD><TITLE>Diretorio</TITLE></HEAD>" +
			"<BODY>Conteudo nao pode ser mostrado</BODY></HTML>";
		
		writeHeader(statusLine, contentTypeLine, os);
		
		// Escreve o corpo da mesagem.
		os.writeBytes(entityBody);
	}
	
}
