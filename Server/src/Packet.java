public class Packet implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
		
	public enum Type {
		LOG_IN, SIGN_UP, ADD_FRIEND, MESSAGE, FILE, UPDATE, LOG_OUT, QUIT;
	}
	public Type type;
	public Message message;

	Packet(Type type, Message message) {
		this.type = type;
		this.message = message;
	}
}
