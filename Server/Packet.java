public class Packet implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
		
	public enum Type {
		LOG_IN, SIGN_UP, ADD_FRIEND, MESSAGE, UPDATE, LOG_OUT, QUIT;
	}
	public Type type;
	public Message message;

	Packet(Type type, Message message) {
		this.type = type;
		this.message = message;
	}
	
	public Packet clone() {
		try {
			Packet pktCopy = (Packet)super.clone();
			pktCopy.message = message.clone();
			return pktCopy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
