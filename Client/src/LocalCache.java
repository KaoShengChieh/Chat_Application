import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;  
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class LocalCache implements ProxyServer
{
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "cache/";
	private static final String database = "database";
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String messageTable = "Message";
	private static final String userFields = "(UserName TEXT PRIMARY KEY, Password TEXT)";
	private static final String friendFields = "(FriendID INTEGER PRIMARY KEY, FriendName TEXT)";
	private static final String messageFields = "(MessageID INTEGER PRIMARY KEY, SenderID INTEGER, ReceiverID INTEGER, Timestamp DATETIME, Content TEXT)";
	private Connection conn;
	private Statement stmt;
	private ResultSet resultSet;
	
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	private ClientSocket clientSocket;
	private String serverAddress;
	private boolean socketIsAlive;
	private int userID;
	private String userName;
	private View view;
	private Thread MVCthread;
	
	public LocalCache() {
		this.connectDatabase();
		
		sendQueue = new BlockingQueue<>();
		recvQueue = new BlockingQueue<>();
		clientSocket = new ClientSocket(sendQueue, recvQueue, this);
		clientSocket.connect();
		
		userID = -1;
	}
	
	private void connectDatabase() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Fail to connect cache database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void closeDatabase() {
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
			System.err.println("Fail to close cache database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	public void setOnlineBySocket(String serverAddress) {
		socketIsAlive = true;
		this.serverAddress = serverAddress;
	}
	
	public void setOfflineBySocket() {
		socketIsAlive = false;
		if (view != null)
			view.getOffline();
	}
	
	public boolean autoLogIn() throws SQLException {
		if (checkConnectionState() == false) {
			return false;
		}
		
		if (conn.getMetaData().getTables(null, null, userTable, null).next() == false) {
			return false;
		}
		
		resultSet = stmt.executeQuery("SELECT * FROM " + userTable);
		
		if (resultSet.next() == false) {
			return false;
		} else {
			Message message = new Message();
			message.content = resultSet.getString("UserName") + resultSet.getString("Password");
			sendQueue.push(new Packet(Packet.Type.LOG_IN, message));
			
			Packet recv_packet = recvQueue.pop();
			if (isSuccessful(recv_packet) == false) {
				return false;
			}
			userID = recv_packet.message.senderID;
			userName = resultSet.getString("UserName");
			
			this.update();
			
			return true;
		}
	}
	
	public boolean logIn(String userName, String password, boolean KeepLogIn) throws SQLException {
		if (checkConnectionState() == false) {
			return false;
		}
		
		password = encrypt(password);
		
		Message message = new Message();
		message.content = userName + password;
		sendQueue.push(new Packet(Packet.Type.LOG_IN, message));
		
		Packet recv_packet = recvQueue.pop();
		if (isSuccessful(recv_packet) == false) {
			return false;
		}
		userID = recv_packet.message.senderID;
		this.userName = userName;
		
		closeDatabase();
		connectDatabase();
		stmt.execute("DROP TABLE IF EXISTS " + userTable);
		stmt.execute("DROP TABLE IF EXISTS " + friendTable);
		stmt.execute("DROP TABLE IF EXISTS " + messageTable);
		
		if (KeepLogIn) {
			stmt.execute("CREATE TABLE " + userTable + userFields);
			stmt.execute(
				"INSERT INTO " + userTable + " " +
				"VALUES ('" + userName +  "', '" + password + "')");
		}
		stmt.execute("CREATE TABLE " + friendTable + friendFields);
		stmt.execute("CREATE TABLE " + messageTable + messageFields);
		
		this.update();
		
		return true;
	}
	
	public boolean signUp(String userName, String password) {
		if (checkConnectionState() == false) {
			return false;
		}
		
		password = encrypt(password);
		
		Message message = new Message();
		message.content = userName + password;
		sendQueue.push(new Packet(Packet.Type.SIGN_UP, message));
		
		return isSuccessful(recvQueue.pop());
	}

	public List<Pair<User, Message>> getFriendList() throws SQLException {
		List<Pair<User, Message>> friendList = new ArrayList<>();
		
		resultSet = stmt.executeQuery("SELECT * FROM " + friendTable);
		
		while (resultSet.next()) {
			int friendID = resultSet.getInt("FriendID");
			User friend = new User(friendID, resultSet.getString("FriendName"));
			friendList.add(new Pair<>(friend, getNewestMessage(friendID)));
		}	
		
		return friendList;
	}
	
	private Message getNewestMessage(int friendID) throws SQLException {
		Message message = new Message();
		
		resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + messageTable + " " +
			"WHERE MessageID IN (" +
				"SELECT MAX(MessageID) " +
				"FROM " + messageTable + " " +
				"WHERE SenderID  = " + friendID + " " +
				"OR ReceiverID  = " + friendID + ")");
		
		if (resultSet.next() == false) {
			return null;
		}
		
		message.msgID = resultSet.getInt("MessageID");
		message.senderID = resultSet.getInt("SenderID");
		message.receiverID = resultSet.getInt("ReceiverID");
		message.timestamp = resultSet.getString("Timestamp");
		message.content = resultSet.getString("Content");
		
		return message;
	}
	
	public boolean addFriend(String friendName) throws SQLException {
		if (checkConnectionState() == false) {
			return false;
		}
		
		Message message = new Message();
		message.senderID = userID;
		message.content = friendName;
		
		try {
			recvQueue.push(new Packet(Packet.Type.QUIT, null));
			MVCthread.join();
		} catch (InterruptedException e) {
			System.err.println("Unexpected Error: " + e.getMessage());
		};
		
		sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, message));
		Packet recv_packet = recvQueue.pop();
		
		this.update();
		
		if (isSuccessful(recv_packet) == false) {
			return false;
		}
		newFriend(recv_packet.message);
		
		return true;
	}
	
	public List<Message> getMsgHistory(User friend, int smallestMessageID) throws SQLException {
		List<Message> messageHistory = new ArrayList<>();
		
		if (smallestMessageID == -1) {
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + messageTable + " " +
				"WHERE SenderID = " + friend.ID + " " +
				"OR ReceiverID = " + friend.ID + " " +
				"ORDER BY MessageID DESC LIMIT 30");
		} else {
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + messageTable + " " +
				"WHERE MessageID < " + smallestMessageID + " " +
				"AND (SenderID = " + friend.ID + " " +
				"OR ReceiverID = " + friend.ID + ") " +
				"ORDER BY MessageID DESC LIMIT 30");
		}
		
		while (resultSet.next()) {
			Message message = new Message();
			message.msgID = resultSet.getInt("MessageID");
			message.senderID = resultSet.getInt("SenderID");
			message.receiverID = resultSet.getInt("ReceiverID");
			message.timestamp = resultSet.getString("Timestamp");
			message.content = resultSet.getString("Content");
			messageHistory.add(message);
		}
		
		return messageHistory;
	}
	
	public boolean sendMessage(User friend, String content) {
		if (checkConnectionState() == false) {
			return false;
		}
		
		Message message = new Message();
		message.senderID = userID;
		message.receiverID = friend.ID;
		message.content = content;
	
		sendQueue.push(new Packet(Packet.Type.MESSAGE, message));
		
		return true;
	}
	
	public boolean sendFile(User friend, String fileName) {
		if (checkConnectionState() == false) {
			return false;
		}
		
		Message message = new Message();
		message.senderID = userID;
		message.receiverID = friend.ID;
		message.content = fileName;
		
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("java FileSender " + fileName + " " + serverAddress);
		} catch (IOException e) {
			view.setErrorMessage("Unable to send file: " + e.getMessage());
		}
		
		sendQueue.push(new Packet(Packet.Type.FILE, message));
		
		return true;
	}
	
	public boolean reconnect() throws SQLException {
		clientSocket.connect();
		this.update();
		
		return socketIsAlive;
	}
	
	public void logOut() throws SQLException {
		try {
			recvQueue.push(new Packet(Packet.Type.QUIT, null));
			MVCthread.join();
		} catch (InterruptedException e) {
			System.err.println("Unexpected Error: " + e.getMessage());
		};
		
		closeDatabase();
		connectDatabase();
		stmt.execute("DROP TABLE IF EXISTS " + userTable);
		stmt.execute("DROP TABLE IF EXISTS " + friendTable);
		stmt.execute("DROP TABLE IF EXISTS " + messageTable);
		
		userID = -1;
		userName = null;
		sendQueue.push(new Packet(Packet.Type.LOG_OUT, null));
	}
	
	public void quit() {
		clientSocket.close();
		this.closeDatabase();
		recvQueue.push(new Packet(Packet.Type.QUIT, null));
		sendQueue.push(new Packet(Packet.Type.QUIT, null));
	}
	
	public void changeView(View view) { 
		this.view = view;
	}
	
	public String getUserName() {
		if (userID == -1) {
			view.setErrorMessage("Not log in yet");
		}
		
		return userName;
	}
	
	private void update() throws SQLException {
	/***************** TODO *******************
	 * 1. Update cache                        *
	 * 2. Open thread process received packet *
	 * 3. Monitor client socket               *
	 ******************************************/
		if (socketIsAlive == false) {
			return;
		}
		
		Message message = new Message();
		
		resultSet = stmt.executeQuery(
			"SELECT * " +
			"FROM " + messageTable + " " +
			"WHERE MessageID IN (" +
				"SELECT MAX(MessageID) " +
				"FROM " + messageTable + ")");
		
		message.msgID = resultSet.next() ? resultSet.getInt("MessageID") : -1;
		message.senderID = userID;
		
		resultSet = stmt.executeQuery("SELECT FriendID FROM " + friendTable);
		
		if (resultSet.next()) {
			message.content = "" + resultSet.getInt("FriendID");
			while (resultSet.next()) {
				message.content += "/" + resultSet.getInt("FriendID");
			}
		}
		
		sendQueue.push(new Packet(Packet.Type.UPDATE, message));
		
		MVCthread = new Thread(new Runnable() {
			public void run() {
				Packet recv_packet = null;
				try {
					while (true) {
						recv_packet = recvQueue.pop();
						if (recv_packet.type == Packet.Type.QUIT) {
							break;
						} else if (isSuccessful(recv_packet) == false) {
							continue;
						}
						switch (recv_packet.type) {
							case MESSAGE:
								recvMessage(recv_packet.message);
								break;
							case ADD_FRIEND:
								newFriend(recv_packet.message);
								break;
							case FILE:
								recvFile(recv_packet.message);
								break;
							default:
								throw new SQLException("Unknown packet: receive " + recv_packet.type + " message");
						}
					}
				} catch (SQLException e) {
					view.setErrorMessage(e.getMessage());
					view.getOffline();
				}
			}
		});
		
		MVCthread.start();
	}
	
	private void recvMessage(Message message) throws SQLException {
		view.newMessage(message);

		stmt.execute(
			"INSERT INTO " + messageTable + " " +
			"VALUES (" + message.msgID +  ", " +
				message.senderID +  ", " +
				message.receiverID +  ", '" +
				message.timestamp + "', '" +
				message.content + "')");
	}
	
	private void newFriend(Message message) throws SQLException {
		int friendID = message.receiverID == userID ?
			message.senderID : message.receiverID;
		
		StringTokenizer tokens = new StringTokenizer(message.content, "/");
		String friendName = tokens.nextToken();
		if (friendName.equals(userName)) {
			friendName = tokens.nextToken();
		}
		
		view.newFriend(new User(friendID, friendName));
		
		stmt.execute(
			"INSERT INTO " + friendTable + " " +
			"VALUES (" + friendID +  ", '" + friendName + "')");
	}
	
	private void recvFile(Message message) throws SQLException {
   		LocalDateTime now = LocalDateTime.now(); 
		message.timestamp = now.toString();

		view.newMessage(message);
		
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec("java FileReceiver " + message.content);
		} catch (IOException e) {
			view.setErrorMessage("Unable to receive file: " + e.getMessage());
		}
	}
	
	private String encrypt(String strToEncrypt) {
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			
			byte[] messageDigest = md.digest(strToEncrypt.getBytes()); 
			
			BigInteger no = new BigInteger(1, messageDigest); 
			
			String encryptText = no.toString(16); 
			
			while (encryptText.length() < 32) { 
				encryptText = "0" + encryptText; 
			}
			
			return encryptText;
		} catch (NoSuchAlgorithmException e) { 
			throw new RuntimeException(e); 
		}
	}
	
	private boolean isSuccessful(Packet packet) {
		if (packet.message.msgID == -1 && packet.message.content != null) {
			view.setErrorMessage(packet.message.content);
		}
		return packet.message.msgID != -1;
	}
	
	private boolean checkConnectionState() {
		if (socketIsAlive) {
			return true;
		}
		view.setErrorMessage("Not connect to Internet");
		return false;
	}
}
