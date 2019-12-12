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
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String userFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT, KeepLogIn INTEGER DEFAULT 0)";
	private static final String friendFields = "(UserID INTEGER, FriendID INTEGER, FriendName TEXT, PRIMARY KEY (UserID, FriendID))";
	private static final String msgFields = "(MsgID INTEGER PRIMARY KEY, FriendID INTEGER, Timestamp DATETIME, Content TEXT)";
	private Connection conn;
	private Statement stmt;
	
	public LocalCache() {
		connect();
		
		try {
			stmt = conn.createStatement();
			if (conn.getMetaData().getTables(null, null, userTable, null).next()) {
				return;
			}
			stmt.execute("CREATE TABLE " + userTable + userFields);
		} catch (SQLException e) {
			System.err.println("Fail to fetch " + userTable + " Information: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void connect() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
		} catch (SQLException e) {
			System.err.println("Fail to connect cache database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void close() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.err.println("Fail to close cache database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	public void LogIn(int userID, String userName, String password, boolean isKeepLogIn) throws SQLException {
		ResultSet resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + userTable + " " +
			"WHERE UserID = " + userID);
		
		if (resultSet.next() == false) {
			stmt.execute(
				"INSERT INTO " + userTable + " " +
				"VALUES (" + userID +  ", '" + userName +  "', '" + password + "', " + (isKeepLogIn ? "1)": "0)"));
			stmt.execute("CREATE TABLE " + userID + msgFields);
			stmt.execute("CREATE TABLE " + friendTable + friendFields);
		} else {
			stmt.execute(
			"UPDATE " + userTable + " " +
			"SET KeepLogIn = " + (isKeepLogIn ? "1 " : "0 ") +
			"WHERE UserID = " + userID);
		}
	}
	
	public User AutoLogIn() throws SQLException {
		ResultSet resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + userTable + " " +
			"WHERE KeepLogIn = 1");
		
		if (resultSet.next() == false) {
			return null;
		} else {
			return new User(resultSet.getInt("UserID"), resultSet.getString("UserName"));
		}
	}
	
	public void update(int userID, Message message) throws SQLException {
		stmt.execute(
			"INSERT INTO " + userID + " " +
			"VALUES (" + message.msgID +  ", " + message.senderID +  ", '" + message.timestamp + "', '" + message.content + "')");
	}
	
	public List<Pair<User, Message>> getFriendList(int userID) throws SQLException {
		int friendID;
		User friend = null;
		Message newestMsg = null;
		List<Pair<User, Message>> friendList = new ArrayList<>();
		
		ResultSet friendSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + friendTable + " " +
			"WHERE UserID = " + userID);
		
		while (friendSet.next()) {
			friendID = friendSet.getInt("FriendID");
			friend = new User(friendID, friendSet.getString("FriendName"));
			friendList.add(new Pair<>(friend , getNewestMessage(userID, friendID)));
		}	
		
		return friendList;
	}
	
	private Message getNewestMessage(int userID, int friendID) throws SQLException {
		Message message = new Message();
		
		ResultSet resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + userID + " " +
			"WHERE FriendID = " + friendID + " " +
			"GROUP BY MsgID " + 
			"HAVING MAX(MsgID)");
		
		if (resultSet.next() == true) {
			message.msgID = resultSet.getInt("MsgID");
			message.senderID = resultSet.getInt("FriendID");
			message.timestamp = resultSet.getString("Timestamp");
			message.content = resultSet.getString("Content");
		}
		
		return message;
	}
	
	public List<Message> getMsgHistoryOfAFriend(int userID, int friendID) throws SQLException {
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
	
	public void addFriend(int userID, int friendID, String friendName) throws SQLException {
		stmt.execute(
			"INSERT INTO " + friendTable + " " +
			"VALUES (" + userID +  ", " + friendID +  ", '" + friendName + "')");
	}
}
