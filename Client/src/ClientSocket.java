import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class ClientSocket
{ 
	private static final String CONFIG = "ClientConfig.txt";
	private String serverAddress;
	private String port;
	private Socket socket;
	private ObjectInputStream inputObject;
	private ObjectOutputStream outputObject;
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	private LocalCache localCache;
	private boolean inputIsClosed;

	public ClientSocket(BlockingQueue<Packet> sendQueue, BlockingQueue<Packet> recvQueue, LocalCache localCache) {
		this(CONFIG);
		this.sendQueue = sendQueue;
		this.recvQueue = recvQueue;
		this.localCache = localCache;
	}

	public ClientSocket(String configuration) {
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
	}
	
	public void connect() {		
		try {
			socket = new Socket(serverAddress, Integer.parseInt(port));
			outputObject = new ObjectOutputStream(socket.getOutputStream());
			inputObject = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			this.close("Fail to connect server: " + e.getMessage());
			return;
		}
		
		localCache.setOnlineBySocket(serverAddress);
		inputIsClosed = false;
		
		try {
			new Thread(new Runnable() {
				public void run() { 
					Packet send_packet = null;
					try {
						while (true) {
							send_packet = getPacket();
							if (inputIsClosed) {
								break;
							}
							writeSocket(send_packet);
						}
					} catch (IOException e) {
						if (e.getMessage() == null) {
							close("Disconnected with server");
						} else {
							close("Upload error: " + e.getMessage());
						}
                    } finally {
						System.err.println("Upload closed");
                    }
				}
			}).start(); 
			
			new Thread(new Runnable() {
				public void run() { 
					Packet recv_packet = null;
					try {
						do { 
							recv_packet = readSocket();
							setPacket(recv_packet);
						} while (recv_packet.type != Packet.Type.QUIT);
					} catch (IOException e) { 
						if (e.getMessage() == null) {
							close("Disconnected with server");
						} else {
							close("Download error: " + e.getMessage());
						}
                    } catch (ClassNotFoundException e) {
                        close("Fail to Serialized/Deserialized: " + e.getMessage());
                        System.exit(0);
                    } finally {
						inputIsClosed = true;
						sendQueue.push(new Packet(Packet.Type.QUIT, null));
						localCache.setOfflineBySocket();
						System.err.println("Download closed");
                    }
				} 
			}).start();
		} catch (Exception e) {
			localCache.setOfflineBySocket();
			this.close("Unexpected error: " + e.getMessage());
		}
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
	
	private void close(String errMessage) {
		System.err.println(errMessage);
	}
	
	private Packet readSocket() throws IOException, ClassNotFoundException {
		return (Packet)inputObject.readObject();
	}
	
	private void writeSocket(Packet packet) throws IOException {
		outputObject.writeObject(packet);
		outputObject.flush();
	}
	
	private Packet getPacket() {
		return sendQueue.pop();
	}
	
	private void setPacket(Packet packet) {
		recvQueue.push(packet);
	}
}
