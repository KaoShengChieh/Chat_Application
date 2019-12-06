public class Message implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public int msgID;
	public int senderID;
	public int receiverID;
	public String timestamp;
	public String content;
	
	public Message clone() {
		try {
			Message msgCopy = (Message)super.clone();
			msgCopy.content = null; // For security concern
			return msgCopy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
