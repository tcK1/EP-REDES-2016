import java.util.HashMap;


public class HTTP {

	private String method;
	private String httpFile;
	private String httpVersion;
	private int status;
	
	private HashMap<String, String> headerFields;
	private byte[] message;
	
	public HTTP(){
		this.headerFields = new HashMap<String, String>();
	}
	
	public void addHeader(String key, String val){
		this.headerFields.put(key, val);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public HashMap<String, String> getHeaderFields() {
		return headerFields;
	}

	public void setHeaderFields(HashMap<String, String> headerFields) {
		this.headerFields = headerFields;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}
	
	public void setHttpFile(String httpFile) {
		this.httpFile = httpFile;
	}
	
	public String getHttpFile() {
		return httpFile;
	}
	

	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}
	
	public String getHttpVersion() {
		return httpVersion;
	}
	
	
}
