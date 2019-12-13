import java.util.List;

public interface ProxyServer {
	/*
	 * Return true, if connect to Internet successfully
	 * Return false, otherwise
	 */
	boolean getOnline();
	
	/*
	 * Return true, if automatically log in successfully
	 * Return false, otherwise
	 */
	boolean autoLogIn();
	
	/*
	 * Return true, if log in successfully
	 * Return false, otherwise
	 */
	boolean logIn(String userName, String password, boolean isKeepLogIn);
	
	/* 
	 * Return username, if client has logged in
	 * Return null, otherwise
	 */
	String getUserName();
	
	/*
	 * Return true, if sign up successfully
	 * Return false, otherwise
	 */
	boolean signUp(String userName, String password);
	
	/*
	 * Return list of pairs of friend info and most recent message
	 */
	List<Pair<User, Message>> getFriendList();
	
	/*
	 * Return true, if add friend successfully
	 * Return false, otherwise
	 */
	boolean addFriend(String friendName);
	
	/*
	 * Return 30 most recent messages of a friend
	 */
	List<Message> getMsgHistoryOfAFriend(int friendID);
	
	/*
	 * Return true, if send message successfully
	 * Return false, otherwise
	 */
	boolean sendMessage(int friendID, String message);
	
	/*
	 * Return true, if reconnect successfully
	 * Return false, otherwise
	 */
	boolean reconnect();
	
	/*
	 * Return true, if log out successfully
	 * Return false, otherwise
	 */
	boolean logOut();
	
	/*
	 * Call this when app ends.
	 */
	void quit();
}
