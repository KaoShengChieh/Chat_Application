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
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;

public class Server 
{ 
	private static final String CONFIG = "./ServerConfig.txt";
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "./data/";
	private static final String database = "database";
	private static final String userTable = "User";
	private static final String friendTable = "Friend";
	private static final String messageTable = "Message";
	private static final String userFields = "(UserID INTEGER PRIMARY KEY, UserName TEXT UNIQUE, Password TEXT)";
	private static final String friendFields = "(FriendAID INTEGER, FriendBID INTEGER, PRIMARY KEY (FriendAID, FriendBID))";
	private static final String messageFields = "(MsgID INTEGER PRIMARY KEY AUTOINCREMENT, senderID INTEGER, receiverID INTEGER, Timestamp DATETIME, Content TEXT)";
	
	private ServerSocket serverSocket;
	private List<ClientHandler> clientList;
	private Connection conn;
	private Statement stmt;

	public static void main(String[] args) throws IOException { 
		Server server = new Server(CONFIG);
		server.clientList = new ArrayList<>();
		server.connectDatabase();
		
		System.out.println("Server listening......");
		
		while (true) {
			try {
				ClientHandler newClient = server.accept();
				
				new Thread(newClient).start(); 
				System.out.println("Adding this client to active client list"); 
				
				server.clientList.add(newClient); 
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
			
		return new ClientHandler(socket, inputObject, outputObject, clientList, stmt);
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
	
	private void connectDatabase() {
		try {
			conn = DriverManager.getConnection(protocol + filepath + database);
			stmt = conn.createStatement();
			createTableIfNotExists(userTable, userFields);
			createTableIfNotExists(friendTable, friendFields);
			createTableIfNotExists(messageTable, messageFields);
		} catch (SQLException e) {
			System.err.println("Fail to connect database: " + e.getMessage());
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
	
	private void closeDatabase() {
		try {
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
}
