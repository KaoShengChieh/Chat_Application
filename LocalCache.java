import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class LocalCache
{
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "./data/";
	private static final String database = "cache";
	private static final String userList = "UserList";
	private static final String friendList = "FriendList";
	private static final String userListFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT)";
	private static final String friendListFields = "(UserID INTEGER, FriendID INTEGER, PRIMARY KEY (UserID, FriendID), FriendName  TEXT)";
	private static final String msgFields = "(MsgID INTEGER PRIMARY KEY, FriendID INTEGER, Timestamp TEXT, Content TEXT)";
	private Connection conn;
	private Statement stmt;
	
	public LocalCache() {
		connect();
		
		try {
			stmt = conn.createStatement();
			if (conn.getMetaData().getTables(null, null, userList, null).next()) {
				return;
			}
			stmt.execute("CREATE TABLE " + userList + userListFields);
		} catch (Exception e) {
			System.err.println("Fail to fetch UserInfo :" + e.getMessage());
		}
	}
	
	private void connect() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
		} catch (SQLException e) {
			System.err.println("Fail to connect cache database : " + e.getMessage());
		}
	}
	
	private void close() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.err.println("Fail to close cache database :" + e.getMessage());
		}
	}
	
	public void newAccount(int userID, String userName, String password) throws Exception {
		stmt.execute(
			"INSERT INTO " + userList + " " +
			"VALUES (" + userID +  ", '" + userName +  "', '" + password + "')");
		stmt.execute("CREATE TABLE " + userID + msgFields);
		stmt.execute("CREATE TABLE " + friendList + friendListFields);
	}
	
	/* If an accountID is not found, then it will return -1. */
	public int getAccountID(String userName, String password) throws Exception {
		ResultSet resultSet = stmt.executeQuery(
			"SELECT UserID " +
			"FROM " + userList + " " +
			"WHERE UserName = '" + userName + "'");
		
		if (resultSet.next() == false) {
			return -1;
		} else {
			return resultSet.getInt("UserID");
		}
	}
	
	/* If an accountID is not found, then it will return -1. */
	public int logInLocally(String userName, String password) throws Exception {
		ResultSet resultSet = stmt.executeQuery(
			"SELECT UserID " +
			"FROM " + userList + " " +
			"WHERE UserName = '" + userName + "' AND Password = '" + password + "'");
		
		if (resultSet.next() == false) {
			return -1;
		} else {
			return resultSet.getInt("UserID");
		}
	}
	
	public List<Message> getMsgHistoryOfAFriend(int userID, int friendID) throws Exception {
		Message message = null;
		List<Message> msgHistory = new ArrayList<>();
		ResultSet resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + userID + " " +
			"WHERE FriendID = " + friendID);
		
		while (resultSet.next()) {
			message = new Message();
			message.msgID = resultSet.getInt("MsgID");
			message.senderID = resultSet.getInt("FriendID");
			message.timestamp = resultSet.getString("Timestamp");
			message.content = resultSet.getString("Content");
			msgHistory.add(message);
		}
		
		return msgHistory;
	}
	
	public String getNewestMessage(int userID, int friendID) throws Exception {
		Message message = new Message();
		
		ResultSet resultSet = stmt.executeQuery(
			"SELECT Content " +
			"FROM " + userID + " " +
			"WHERE FriendID = " + friendID + " " +
			"GROUP BY MsgID " + 
			"HAVING MAX(MsgID)");
		
		return resultSet.getString("Content");
	}
	
	public void update(int userID, Message message) throws Exception {
		stmt.execute(
			"INSERT INTO " + userID + " " +
			"VALUES (" + message.msgID +  ", " + message.senderID +  ", '" + message.timestamp + "', '" + message.content + "')");
	}
}
