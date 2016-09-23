import java.util.HashMap;


public class HTTP {

	private String method;
	private int status;
	
	private HashMap<String, String> headerFields;
	private byte[] message;
	
	public HTTP(String method, int status,
			HashMap<String, String> headerFields, byte[] message) {
		super();
		this.method = method;
		this.status = status;
		this.headerFields = headerFields;
		this.message = message;
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

}
