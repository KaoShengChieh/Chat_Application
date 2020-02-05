import java.sql.SQLException;
import java.util.List;

public interface ProxyServer {
	/*
	 * Return true, if automatically log in successfully
	 * Return false, otherwise
	 */
	boolean autoLogIn() throws SQLException;
	
	/*
	 * Return true, if log in successfully
	 * Return false, otherwise
	 */
	boolean logIn(String userName, String password, boolean keepLogIn) throws SQLException;
	
	/*
	 * Return true, if sign up successfully
	 * Return false, otherwise
	 */
	boolean signUp(String userName, String password);
	
	/*
	 * Return list of pairs of friend info and most recent message
	 */
	List<Pair<User, Message>> getFriendList() throws SQLException;
	
	/*
	 * Return true, if add friend successfully
	 * Return false, otherwise
	 */
	boolean addFriend(String friendName) throws SQLException;
	
	/*
	 * Return 30 more previous messages of a friend
	 * If smallestMessageID == -1, return most recent 30 ones
	 */
	List<Message> getMsgHistory(User friend, int smallestMessageID) throws SQLException;
	
	/*
	 * Return true, if send message successfully
	 * Return false, otherwise
	 */
	boolean sendMessage(User friend, String message);
	
	boolean sendFile(User friend, String file);
	
	/*
	 * Return true, if reconnect successfully
	 * Return false, otherwise
	 */
	boolean reconnect() throws SQLException;
	
	/*
	 * Return true, if log out successfully
	 * Return false, otherwise
	 */
	void logOut() throws SQLException;
	
	/*
	 * Call this when app ends.
	 */
	void quit();
	
	void changeView(View view);
	
	User getUser();
}
