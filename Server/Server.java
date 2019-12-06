import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket; 
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList; 
import java.util.ListIterator;

public class Server 
{ 
	private static final String CONFIG = "./ServerConfig.txt";
	private ServerSocket serverSocket;
	private List<ClientHandler> clientList = new ArrayList<>();

	public static void main(String[] args) throws IOException { 
		Server server = new Server(CONFIG);
		
		System.out.println("Server listening......");
		
		while (true) {
			try {
				ClientHandler newClient = server.accept();
				
				Thread connection = new Thread(newClient);
			
				System.out.println("Adding this client to active client list"); 

				server.clientList.add(newClient); 

				connection.start(); 
			} catch (Exception e) {
				System.out.println("Connection error");
				e.printStackTrace();
				server.close();
			}
		} 
	}
	
	public Server(String configuration) {
		String port = null;
		
		try {
			InputStream inputStream = getClass().getResourceAsStream(configuration); 
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
			String line = bufferReader.readLine();
			while (line != null) {
				StringTokenizer tokens = new StringTokenizer(line);
				switch (tokens.nextToken()) {
					default:
						continue;
					case "Port":
						tokens.nextToken();
						port = tokens.nextToken();
				}
				line = bufferReader.readLine();
			}
			bufferReader.close();
		} catch (IOException e) {
			System.out.println("Can't read server configuration");
			System.exit(0);
		}
			
		if (port == null) {
			System.out.println("Unknown server configuration");
			System.exit(0);
		}
		
		try {
			serverSocket = new ServerSocket(Integer.parseInt(port));
		} catch (IOException e) {
			System.out.println("Server start up error");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private ClientHandler accept() {
		Socket socket = null;
		ObjectInputStream inputObject = null;
		ObjectOutputStream outputObject = null;
		
		try {
			socket = serverSocket.accept();
			System.out.println("New connection request received: " + socket);
			inputObject = new ObjectInputStream(socket.getInputStream());
			outputObject = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("IO error when connecting to client");
		}
		
		System.out.println("Creating a new handler for this client...");
			
		return new ClientHandler(socket, inputObject, outputObject);
	}
	
	private void close() {
		try {
			serverSocket.close();
			System.out.println("Server closed");
		} catch (IOException e) {
			System.out.println("Server socket close error");
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	private class ClientHandler implements Runnable {
		private Socket socket;
		private ObjectInputStream inputObject;
		private ObjectOutputStream outputObject;
		private Packet recv_packet;
		private boolean isLoggedIn;
		private int userID;
		private Client client; 
		
		public ClientHandler(Socket socket, ObjectInputStream inputObject, ObjectOutputStream outputObject) { 
			this.inputObject = inputObject; 
			this.outputObject = outputObject; 
			this.socket = socket; 
			userID = -1;
			client = new Client(socket, outputObject);
		}

		public void run() { 
			while (true) { 
				try { 
					recv_packet = (Packet)inputObject.readObject();
					
					System.out.println("Receive " + recv_packet.type + " request from " + (userID >= 0 ? "unknown user" : "user " + userID));
					
					if (recv_packet.type == Packet.Type.QUIT) {
						break;
					}
					
					if (isLoggedIn == false) {
						if (recv_packet.type != Packet.Type.SIGN_UP
							|| recv_packet.type != Packet.Type.LOG_IN)
						 	continue;
						
						if (recv_packet.type == Packet.Type.SIGN_UP) {
							client.signUp(recv_packet.message);
							continue;
						} else { // packet.type == Packet.Type.LOG_IN
							userID = client.logIn(recv_packet.message);
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
						client.update(recv_packet.message);
					} else {
						continue;
					}
					
					ListIterator<ClientHandler> itr = clientList.listIterator();
					ClientHandler otherUser;
					switch (recv_packet.type) {
						case ADD_FRIEND:
							client.addFriend(recv_packet.message);
							while (itr.hasNext()) {
								otherUser = itr.next();
								if (otherUser.userID == recv_packet.message.receiverID) {
									//TODO
									//otherUser.send_packet;
								}
							}
							break;
						case MESSAGE:
							client.sendMsg(recv_packet.message);
														while (itr.hasNext()) {
								otherUser = itr.next();
								if (otherUser.userID == recv_packet.message.receiverID) {
									//TODO
									//otherUser.send_packet;
								}
							}
							break;
						//case READ:
					}
				} catch (IOException e) { 
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			try {
				if (inputObject != null) {
					inputObject.close(); 
				}
				if (outputObject != null) {
					outputObject.close();
				}
				if (socket != null) {
					socket.close();
				}
				System.out.println("Connection with " + (userID >= 0 ? "unknown user" : "user " + userID) + " closed");
			} catch (IOException ie) {
				System.out.println("Socket close error");
			} finally {
				clientList.remove(this);
			}
		} 
	}
	
	private class Client {
		private static final String protocol = "jdbc:sqlite:";
		private static final String filepath = "./data/";
		private static final String database = "database";
		private static final String userTable = "User";
		private static final String friendTable = "Friend";
		private static final String messageTable = "Message";
		private static final String userFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT)";
		private static final String friendFields = "(UserID INTEGER, FriendID INTEGER, PRIMARY KEY (UserID, FriendID))";
		private static final String messageFields = "(MsgID INTEGER PRIMARY KEY, senderID INTEGER, receiverID INTEGER, Timestamp DATETIME, Content TEXT)";
		private Connection conn;
		private Statement stmt;
		private Socket socket;
		
		private ObjectOutputStream outputObject;
		
		public Client(Socket socket, ObjectOutputStream outputObject) {
			this.socket = socket;
			this.outputObject = outputObject;
			
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
		
		private void connect() {
			try {
				conn = DriverManager.getConnection(protocol + filepath + database);
			} catch (SQLException e) {
				System.err.println("Fail to connect database: " + e.getMessage());
				System.exit(0);
			}
		}
		
		private void close() {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				System.err.println("Fail to close database: " + e.getMessage());
				System.exit(0);
			}
		}
		
		public void signUp(Message recv_msg) {
			Message send_msg = recv_msg.clone();
			
			StringTokenizer tokens = new StringTokenizer(recv_msg.content, "/");
			
			String userName = tokens.nextToken();
			String password = tokens.nextToken();
			
			try {
				ResultSet resultSet = stmt.executeQuery(
					"SELECT * " +
					"FROM " + userTable + " " +
					"WHERE UserName = '" + userName + "'");
				
				send_msg.senderID = -1;
				
				//TODO
				
				stmt.execute(
					"INSERT INTO " + userTable + "(UserName, Password) " +
					"VALUES ('" + userName +  "', '" + password + "')");
				
				send_msg.senderID = stmt.executeQuery(
					"SELECT UserID " +
					"FROM " + userTable + " " +
					"WHERE UserName = '" + userName + "'").getInt("UserID");
					
				//TODO
			} catch (SQLException e) {
				send_msg.content = "Unable to sign up: " + e.getMessage();
			}
		}
		
		public int logIn(Message recv_msg) {
			Message send_msg = recv_msg.clone();
			
			StringTokenizer tokens = new StringTokenizer(recv_msg.content, "/");
			
			String userName = tokens.nextToken();
			String password = tokens.nextToken();
			
			try {
				ResultSet resultSet = stmt.executeQuery(
					"SELECT UserID " +
					"FROM " + userTable + " " +
					"WHERE UserName = '" + userName + "' " +
					"AND Password = '" + password + "'");
				
				send_msg.senderID = resultSet.next() ? resultSet.getInt("UserID") : -1;
			
				//TODO
			} catch (SQLException e) {
				send_msg.content = "Unable to log in: " + e.getMessage();
			}
			
			return send_msg.senderID;
		}
		
		public void update(Message recv_msg) {
			//TODO
		}
		
		public void addFriend(Message recv_msg) {
			//TODO
		}
		public void sendMsg(Message recv_msg) {
			//TODO
		}
	}
}
