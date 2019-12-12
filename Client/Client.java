import java.util.StringTokenizer;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Client 
{ 
	private static final String CONFIG = "./ClientConfig.txt";
	private Socket socket;
	private ObjectInputStream inputObject;
	private ObjectOutputStream outputObject;
	private BlockingQueue<Packet> send_queue;
	private BlockingQueue<Packet> recv_queue;
	//private GUI myGUI;

	public static void main(String args[]) {
		Client client = new Client(CONFIG);
		
		try {
			//client.myGUI.start();
		
			new Thread(new Runnable() { 
				public void run() { 
					Packet send_packet = null;
					try {
						do {
							send_packet = client.getPacket();
							client.writeSocket(send_packet);
						} while (send_packet.type != Packet.Type.QUIT);
					} catch (IOException e) { 
                        e.printStackTrace();
                        client.close("Disconnected with server");
                    } 
				}
			}).start(); 
			
			new Thread(new Runnable() { 
				public void run() { 
					Packet recv_packet = null;
					try {
						while (true) { 
							recv_packet = client.readSocket();
							client.setPacket(recv_packet);
						}
					} catch (IOException e) { 
                        e.printStackTrace();
                        client.close("Disconnected with server");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        client.close("Fail to Serialized/Deserialized");
                    }
				} 
			}).start();
			/*
			synchronized (client.myGUI) {
				while (client.myGUI.isQuit() == false) {
					try {
						client.myGUI.wait();
					} catch (InterruptedException e)  {
						Thread.currentThread().interrupt(); 
					}
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			client.close("Unexpected error");
		}
		
		client.close();
		System.exit(0);
	}
	
	public Client(String configuration) {
		String serverAddress = null;
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
					case "ServerAddress":
						tokens.nextToken();
						serverAddress = tokens.nextToken();
						break;
					case "Port":
						tokens.nextToken();
						port = tokens.nextToken();
				}
				line = bufferReader.readLine();
			}
			bufferReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.close("Can't read client configuration");
		}

		if (serverAddress == null || port == null) {
			this.close("Unknown client configuration");
		}
		
		try {
			socket = new Socket(serverAddress, Integer.parseInt(port));
			outputObject = new ObjectOutputStream(socket.getOutputStream());
			inputObject = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			this.close("Connection to server failed");
		}
		
		send_queue = new BlockingQueue<>();
		recv_queue = new BlockingQueue<>();
		//myGUI = new GUI(client.send_queue, recv_queue);
	}
	
	public Packet readSocket() throws IOException, ClassNotFoundException {
		return (Packet)inputObject.readObject();
	}
	
	public void writeSocket(Packet packet) throws IOException {
		outputObject.writeObject(packet);
		outputObject.flush();
	}
	
	public Packet getPacket() {
		return send_queue.pop();
	}
	
	public void setPacket(Packet packet) {
		recv_queue.push(packet);
	}
	
	public void close() {
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
		} catch (IOException e) {
			System.err.println("Socket close error");
		} finally {
			System.out.println("Connection closed");
		}
	}
	
	public void close(String errMessage) {
		System.err.println(errMessage);
		/*
		popupWindow.show(errMessage);
		close();
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			// do nothing
		}
		*/
	}
}
