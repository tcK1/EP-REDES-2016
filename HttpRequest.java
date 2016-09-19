

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class HttpRequest implements Runnable{

	final static String CRLF = "\r\n";
	Socket socket;

	// Construtor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
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

		// Abrir o arquivo requisitado.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		// Construir a mensagem de resposta.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
			statusLine = "HTTP/1.0 200";
			contentTypeLine = "Content-type: " + contentType( fileName ) + CRLF;
		} else {
			statusLine = "HTTP/1.0 404";
			contentTypeLine = "Content-type: text/html" + CRLF;
			entityBody = "<HTML>" +
				"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
				"<BODY>Not Found</BODY></HTML>";
		}

		// Enviar a linha de status.
		os.writeBytes(statusLine);
		// Enviar a linha de tipo de conteúdo.
		os.writeBytes(contentTypeLine);
		// Enviar uma linha em branco para indicar o fim das linhas de cabeçalho.
		os.writeBytes(CRLF);

		// Enviar o corpo da entidade.
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			// Escreve o corpo da mesagem.
			os.writeBytes(entityBody);
		}

		//  Exibir a linha de requisição.
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
	
}
