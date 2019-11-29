public class Packet implements java.io.Serializable, Cloneable {
	public enum Type {
		LOG_IN, SIGN_UP, ADD_FRINED, MESSAGE, READ, LOG_OUT, QUIT;
	}
	public Type type;
	public Message message;
}
