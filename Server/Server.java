import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket; 
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
			
		return new ClientHandler(socket, inputObject, outputObject, clientList);
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
}
