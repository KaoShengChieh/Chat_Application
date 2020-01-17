import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "./data/";
	private static final String database = "database";
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String messageTable = "Message";
	private static final String userFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT)";
	private static final String friendFields = "(FriendAID INTEGER, FriendBID INTEGER, PRIMARY KEY (FriendAID, FriendBID))";
	private static final String messageFields = "(MsgID INTEGER PRIMARY KEY AUTOINCREMENT, senderID INTEGER, receiverID INTEGER, Timestamp DATETIME, Content TEXT)";
	private Connection conn;
	private Statement stmt;
	private ResultSet resultSet;
	
	public Database() {
		open();
		createTableIfNotExists(userTable, userFields);
		createTableIfNotExists(friendTable, friendFields);
		createTableIfNotExists(messageTable, messageFields);
	}
	
	public void open() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Fail to connect database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close(); 
			}
		} catch (SQLException e) {
			System.err.println("Fail to close database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void createTableIfNotExists(String tableName, String fields) {
		try {
			if (conn.getMetaData().getTables(null, null, tableName, null).next() == false) {
				stmt.execute("CREATE TABLE " + tableName + fields);
			}
		} catch (SQLException e) {
			System.err.println("Fail to fetch " + tableName + " Information: " + e.getMessage());
			System.exit(0);
		}
	}
}
