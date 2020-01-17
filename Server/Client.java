import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class Client implements Runnable {
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String messageTable = "Message";
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	private ClientHandler clientHandler;
	private List<ClientHandler> clientList;
	private Statement stmt;
	private ResultSet resultSet;
	private boolean isLoggedIn;
	public int userID;
	
	public Client(BlockingQueue<Packet> sendQueue, BlockingQueue<Packet> recvQueue, ClientHandler clientHandler, List<ClientHandler> clientList, Statement stmt) {
		this.sendQueue = sendQueue;
		this.recvQueue = recvQueue;
		this.clientHandler = clientHandler;
		this.clientList = clientList;
		this.stmt = stmt;
		userID = -1;
	}
	
	public void run() {
		Packet recv_packet = null;
		Packet send_packet = null;
		Message send_msg = null;
		ListIterator<ClientHandler> itr = null;
		ClientHandler other = null;
		
		try {
			while (true) { 
				recv_packet = recvQueue.pop();
					
				System.out.println("Receive " +
					recv_packet.type + " request from " +
					(userID == -1 ? "unknown user" : "user " + userID));
					
				if (recv_packet.type == Packet.Type.QUIT) {
					break;
				}
				
				if (isLoggedIn == false) {
					switch (recv_packet.type) {
						case SIGN_UP:
							signUp(recv_packet.message);
							continue;
						case LOG_IN:
							logIn(recv_packet.message);
							continue;
						default:
							continue;
					}
				}
				
				if (recv_packet.type == Packet.Type.LOG_OUT) {
					userID = -1;
					isLoggedIn = false;
					continue;
				}
				
				if (recv_packet.type == Packet.Type.UPDATE) {
					update(recv_packet.message);
					continue;
				}
				
				itr = clientList.listIterator();
				switch (recv_packet.type) {
					case ADD_FRIEND:
						send_msg = addFriend(recv_packet.message);
						
						if (send_msg == null)
							break;
						
						while (itr.hasNext()) {
							other = itr.next();
							if (other.client.userID == recv_packet.message.receiverID
								|| other.client.userID == recv_packet.message.senderID) {
								other.client.addFriendNotice(send_msg);
							}
						}
						break;
					case MESSAGE:
						send_msg = newMessage(recv_packet.message);
						
						if (send_msg == null)
							break;
						
						while (itr.hasNext()) {
							other = itr.next();
							if (other.client.userID == recv_packet.message.receiverID
								|| other.client.userID == recv_packet.message.senderID) {
								other.client.newMessageNotice(send_msg);
							}
						}
						break;
					case FILE:
						while (itr.hasNext()) {
							other = itr.next();
							if (other.client.userID == recv_packet.message.receiverID) {
								other.client.newFileNotice(recv_packet.message);
								break;
							}
						}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			clientHandler.close();
		}
	}
	
	private void signUp(Message recv_msg) {
		Message send_msg = recv_msg.clone();
		
		String userInfo = recv_msg.content;
		String userName = userInfo.substring(0, userInfo.length()-32);
		String password = userInfo.substring(userInfo.length()-32);
		
		try {
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + userName + "'");
				
			if (resultSet.next() == true) {
				send_msg.setErrorMessage("Username has been used");
			} else {
				stmt.execute(
					"INSERT INTO " + userTable + "(UserName, Password) " +
					"VALUES ('" + userName +  "', '" + password + "')");
			}
		} catch (SQLException e) {
			send_msg.setErrorMessage("Unable to sign up: " + e.getMessage());
		} finally {
			sendQueue.push(new Packet(Packet.Type.SIGN_UP, send_msg));
		}
	}
	
	private void logIn(Message recv_msg) {
		Message send_msg = recv_msg.clone();

		String userInfo = recv_msg.content;
		String userName = userInfo.substring(0, userInfo.length()-32);
		String password = userInfo.substring(userInfo.length()-32);
		
		try {
			resultSet = stmt.executeQuery(
				"SELECT UserID " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + userName + "' " +
				"AND Password = '" + password + "'");
			
			if (resultSet.next() == true) {
				send_msg.senderID = resultSet.getInt("UserID");
				userID = send_msg.senderID;
				isLoggedIn = true;
			} else {
				send_msg.setErrorMessage("Username or password is incorrect");
			}
		} catch (SQLException e) {
			send_msg.setErrorMessage("Unable to log in: " + e.getMessage());
		} finally {
			sendQueue.push(new Packet(Packet.Type.LOG_IN, send_msg));
		}
	}
	
	private void update(Message recv_msg) {
		try {
			/* Update friends */
			resultSet = stmt.executeQuery( //TODO
				"SELECT UserID AS FriendID, UserName AS FriendName " +
				"FROM " + friendTable + ", " + userTable + " " +
				"WHERE (FriendAID = " + userID + " AND UserID = FriendBID) "+
				"OR (FriendBID = " + userID + " AND UserID = FriendAID)");
			
			if (recv_msg.content != null) {
				StringTokenizer tokens = new StringTokenizer(recv_msg.content, "/");
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
				while (resultSet.next()) {
					Message send_msg = new Message();
					send_msg.senderID = userID;
					send_msg.receiverID = resultSet.getInt("FriendID");
					send_msg.content = resultSet.getString("FriendName");
					sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				}
			}
			
			/* Update messages */
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + messageTable + " " +
				"WHERE (SenderID = " + userID + " OR ReceiverID = " + userID + ") " +
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
			send_msg.setErrorMessage("Unable to update: " + e.getMessage());
			sendQueue.push(new Packet(Packet.Type.UPDATE, send_msg));
		}
	}
	
	private Message addFriend(Message recv_msg) {
		int friendID;
		Message send_msg = recv_msg.clone();
		
		try {
			resultSet = stmt.executeQuery(
				"SELECT UserID " +
				"FROM " + userTable + " " +
				"WHERE UserName = '" + recv_msg.content + "'");
			
			if (resultSet.next() == false) {
				send_msg.setErrorMessage("Username not found");
				sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				return null;
			}
			friendID = resultSet.getInt("UserID");
			
			if (friendID == userID) {
				send_msg.setErrorMessage("Cannot add yourself");
				sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				return null;
			}
			
			resultSet = stmt.executeQuery(
				"SELECT * " +
				"FROM " + friendTable + " " +
				"WHERE (FriendAID = " + userID + " AND FriendBID = " + friendID + ") "+
				"OR (FriendAID = " + friendID + " AND FriendBID = " + userID + ")");
			
			if (resultSet.next() == true) {
				send_msg.setErrorMessage("You and " + recv_msg.content + " are already friends");
				sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
				return null;
			}
			
			stmt.execute(
				"INSERT INTO " + friendTable + " " +
				"VALUES (" + userID + ", " + friendID + ")");
				
			resultSet = stmt.executeQuery(
				"SELECT userName " +
				"FROM " + userTable + " " +
				"WHERE UserID = '" + userID + "'");

			send_msg.receiverID = friendID;
			send_msg.content += "/" + resultSet.getString("UserName");

			return send_msg;
		} catch (SQLException e) {
			send_msg.setErrorMessage("Unable to add friend: " + e.getMessage());
			sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
			
			return null;
		}
	}
	
	private void addFriendNotice(Message send_msg) {
		sendQueue.push(new Packet(Packet.Type.ADD_FRIEND, send_msg));
	}
	
	private synchronized Message newMessage(Message recv_msg) {
		Message send_msg = recv_msg.clone();
		
		try {
			stmt.execute(
				"INSERT INTO " + messageTable +
				"(senderID, receiverID, Timestamp, Content) " +
				"VALUES (" + userID + ", " +
					recv_msg.receiverID + ", " +
					"datetime('now'), '" +
					recv_msg.content + "')");
			resultSet = stmt.executeQuery(
				"SELECT MsgID, Timestamp " +
				"FROM " + messageTable + " " +
				"WHERE MsgID = (" +
					"SELECT last_insert_rowid() " +
					"FROM " + messageTable + ")");
			
			resultSet.next();
			send_msg.msgID = resultSet.getInt("MsgID");
			send_msg.timestamp = resultSet.getString("Timestamp");
			
			return send_msg;
		} catch (SQLException e) {
			send_msg.setErrorMessage("Unable to send message: " + e.getMessage());
			sendQueue.push(new Packet(Packet.Type.MESSAGE, send_msg));
			
			return null;
		}
	}
	
	private void newMessageNotice(Message send_msg) {
		sendQueue.push(new Packet(Packet.Type.MESSAGE, send_msg));
	}
	
	private void newFileNotice(Message recv_msg) {
		sendQueue.push(new Packet(Packet.Type.FILE, recv_msg));
	}
}
