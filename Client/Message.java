public class Message implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	
	public int msgID;
	public int senderID;
	public int receiverID;
	public String timestamp;
	public String content;
	
	public Message clone() {
		try {
			return (Message)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public void setErrorMessage(String errorMessage) {
		msgID = -1;
		content = errorMessage;
	}
}
