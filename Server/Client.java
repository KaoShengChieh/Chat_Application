import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Client {
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "./data/";
	private static final String database = "database";
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String messageTable = "Message";
	private static final String userFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT)";
	private static final String friendFields = "(FrinedAID INTEGER, FriendBID INTEGER, PRIMARY KEY (FriendAID, FriendBID))";
	private static final String messageFields = "(MsgID INTEGER AUTOINCREMENT PRIMARY KEY, senderID INTEGER, receiverID INTEGER, Timestamp DATETIME, Content TEXT)";
	private Connection conn;
	private Statement stmt;
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	private List<ClientHandler> clientList;
	private boolean isLoggedIn;
	private boolean quit;
	public int userID;
	
	public Client(BlockingQueue<Packet> sendQueue, BlockingQueue<Packet> recvQueue, List<ClientHandler> clientList) {
		this.sendQueue = sendQueue;
		this.recvQueue = recvQueue;
		this.clientList = clientList;
		userID = -1;
		
		connectDatabase();
		
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
		
		try {
			if (conn.getMetaData().getTables(null, null, friendTable, null).next()) {
				return;
			}
			stmt.execute("CREATE TABLE " + friendTable + friendFields);
			} catch (SQLException e) {
			System.err.println("Fail to fetch " + friendTable + " Information: " + e.getMessage());
			System.exit(0);
		}
		
		try {
			if (conn.getMetaData().getTables(null, null, messageTable, null).next()) {
				return;
			}
			stmt.execute("CREATE TABLE " + messageTable + messageFields);
		} catch (SQLException e) {
			System.err.println("Fail to fetch " + messageTable + " Information: " + e.getMessage());
			System.exit(0);
		}
	}
	
	public synchronized void start() {
		Packet recv_packet = null;
		Packet send_packet = null;
		Message send_msg = null;
		ListIterator<ClientHandler> itr = null;
		ClientHandler other = null;
		
		try {
			while (true) { 
				recv_packet = recvQueue.pop();
					
				System.out.println("Receive " +
					recv_packet.type +" request from " +
					(userID >= 0 ? "unknown user" : "user " + userID));
					
				if (recv_packet.type == Packet.Type.QUIT) {
					break;
				}
				
				if (isLoggedIn == false) {
					if (recv_packet.type != Packet.Type.SIGN_UP
						&& recv_packet.type != Packet.Type.LOG_IN)
					 	continue;

					if (recv_packet.type == Packet.Type.SIGN_UP) {
						signUp(recv_packet.message);
						continue;
					} else { // packet.type == Packet.Type.LOG_IN
						logIn(recv_packet.message);
						if (userID == -1)
							continue;
						else
							isLoggedIn = true;
					}
				}
				
				if (recv_packet.type == Packet.Type.LOG_OUT) {
					userID = -1;
					isLoggedIn = false;
				}
				
				if (recv_packet.type == Packet.Type.UPDATE) {
					update(recv_packet.message);
				} else {
					continue;
				}
					
				itr = clientList.listIterator();
				switch (recv_packet.type) {
					case ADD_FRIEND:
						send_msg = addFriend(recv_packet.message);
						while (itr.hasNext()) {
							other = itr.next();
							if (other.client.userID() == recv_packet.message.receiverID
								|| other.client.userID == recv_packet.message.senderID) {
								other.client.addFriendNotice(send_msg);
							}
						}
						break;
					case MESSAGE:
						send_msg = newMsg(recv_packet.message);
						while (itr.hasNext()) {
							other = itr.next();
							if (other.client.userID() == recv_packet.message.receiverID
								|| other.client.userID == recv_packet.message.senderID) {
								other.client.newMsgNotice(send_msg);
							}
						}
						break;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			closeDatabase();
			quit = true;
			notifyAll();
		}
	}
	
	public int userID() {
		return userID;
	}
	
	public boolean isQuit() {
		return quit;
	}
	
	private void connectDatabase() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
		} catch (SQLException e) {
			System.err.println("Fail to connect database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void closeDatabase() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.err.println("Fail to close database: " + e.getMessage());
			System.exit(0);
		}
	}
	
	private void signUp(Message recv_msg) {
		Message send_msg = recv_msg.clone();
		
		String userInfo = send_msg.content;
		String userName = userInfo.substring(0, userInfo.length()-16);
		String password = userInfo.substring(userInfo.length()-16);
		
		try {
			ResultSet resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + userName + "'");
			
			if (resultSet.next() == true) {
				send_msg.senderID = -1;
				send_msg.content = "Username has been used";
				sendQueue.push(new Packet(Packet.Type.SIGN_UP, send_msg));
				return;
			}
			
			stmt.execute(
				"INSERT INTO " + userTable + "(UserName, Password) " +
				"VALUES ('" + userName +  "', '" + password + "')");
		} catch (SQLException e) {
			send_msg.content = "Unable to sign up: " + e.getMessage();
		} finally {
			sendQueue.push(new Packet(Packet.Type.SIGN_UP, send_msg));
		}
	}
	
	private void logIn(Message recv_msg) {
		Message send_msg = recv_msg.clone();

		String userInfo = send_msg.content;
		String userName = userInfo.substring(0, userInfo.length()-32);
		String password = userInfo.substring(userInfo.length()-32);
		
		try {
			ResultSet resultSet = stmt.executeQuery(
				"SELECT UserID " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + userName + "' " +
				"AND Password = '" + password + "'");
			
			if (resultSet.next() == true) {
				send_msg.senderID = resultSet.getInt("UserID");
				userID = send_msg.senderID;
			} else {
				send_msg.senderID = -1;
				send_msg.content = "Username or passwrod is incorrect";
			}
		} catch (SQLException e) {
			send_msg.content = "Unable to log in: " + e.getMessage();
		} finally {
			sendQueue.push(new Packet(Packet.Type.LOG_IN, send_msg));
		}
	}
	
	private void update(Message recv_msg) {
		try {
			ResultSet resultSet = stmt.executeQuery(
				"SELECT UserID AS FriendID, UserName AS FriendName" +
				"FROM " + friendTable + ", " + userTable + " " +
				"WHERE (FriendAID = " + userID + " " +
					"AND UserID = FriendBID)"+
				"OR (FriendBID = " + userID + " " +
					"AND UserID = FriendAID)");
			
			StringTokenizer tokens = new StringTokenizer(recv_msg.content, "/");
			int tokenCount = tokens.countTokens();
			
			if (tokenCount != 0) {
				List<Integer> friendList = new ArrayList<>();
				
				while (tokens.hasMoreTokens()) {
					friendList.add(Integer.valueOf(tokens.nextToken()));
				}
			
				while (resultSet.next()) {
					int friendID = resultSet.getInt("FriendID");
					if (friendList.contains(friendID)) {
						continue;
					}
					
					Message send_msg = new Message();
					send_msg.senderID = userID;
					send_msg.receiverID = friendID;
					send_msg.content = resultSet.getString("FriendName");
					sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				}
			} else {
				Message send_msg = new Message();
				send_msg.senderID = userID;
				send_msg.receiverID = resultSet.getInt("FriendID");
				send_msg.content = resultSet.getString("FriendName");
				sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
			}
			
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + messageTable + " " +
				"WHERE SenderID = " + userID + " " +
				"OR ReceiverID = " + userID + ") " +
				"AND MsgID > " + recv_msg.msgID);
			
			while (resultSet.next()) {
				Message send_msg = new Message();
				send_msg.msgID = resultSet.getInt("MsgID");
				send_msg.senderID = resultSet.getInt("SenderID");
				send_msg.receiverID = resultSet.getInt("ReceiverID");
				send_msg.timestamp = resultSet.getString("Timestamp");
				send_msg.content = resultSet.getString("Content");
				sendQueue.push(new Packet(Packet.Type.MESSAGE, send_msg));
			}
		} catch (SQLException e) {
			Message send_msg = recv_msg.clone();
			send_msg.content = "Unable to update: " + e.getMessage();
			sendQueue.push(new Packet(Packet.Type.UPDATE, send_msg));
		}
	}
	
	private Message addFriend(Message recv_msg) {
		Message send_msg = recv_msg.clone();
		int friendID;
		
		try {
			ResultSet resultSet = stmt.executeQuery(
				"SELECT UserID " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + recv_msg.content + "'");
			
			if (resultSet.next() == false) {
				send_msg.receiverID = -1;
				send_msg.content = "Username not found";
				sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				return null;
			}
			
			resultSet.next();
			friendID = resultSet.getInt("FriendID");
			
			stmt.execute(
				"INSERT INTO " + friendTable + " " +
				"VALUES (" + send_msg.senderID + ", " + friendID + ")");
			
			send_msg.receiverID = friendID;
			send_msg.content = recv_msg.content;
			
			return send_msg;
		} catch (SQLException e) {
			send_msg.receiverID = -1;
			send_msg.content = "Unable to add friend: " + e.getMessage();
			sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
			
			return null;
		}
	}
	
	private void addFriendNotice(Message send_msg) {
		sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
	}
	
	private Message newMsg(Message recv_msg) {
		Message send_msg = recv_msg.clone();
		
		try {
			stmt.execute(
				"INSERT INTO " + messageTable +
				"(senderID INTEGER, receiverID, Timestamp, Content) " +
				"VALUES (" + userID + ", " +
					recv_msg.receiverID + ", " +
					"datetime('now'), " +
					recv_msg.content + "')");
			
			ResultSet resultSet = stmt.executeQuery(
				"SELECT last_insert_rowid() AS MsgID " +
				"FROM " + messageTable);
			resultSet.next();
			send_msg.msgID = resultSet.getInt("MsgID");
			send_msg.timestamp = resultSet.getString("Timestamp");
			send_msg.content = recv_msg.content;
			
			return send_msg;
		} catch (SQLException e) {
			send_msg.msgID = -1;
			send_msg.content = "Unable to send message: " + e.getMessage();
			sendQueue.push(new Packet(Packet.Type.MESSAGE, send_msg));
			
			return null;
		}
	}
	
	private void newMsgNotice(Message send_msg) {
		sendQueue.push(new Packet(Packet.Type.MESSAGE, send_msg));
	}
}
