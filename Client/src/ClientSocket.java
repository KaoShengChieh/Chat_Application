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
	private boolean outputIsClosed;

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
			this.close("Fail to connect server");
		}
		
		localCache.setOnlineBySocket(serverAddress);
		
		try {
			new Thread(new Runnable() {
				public void run() { 
					Packet send_packet = null;
					try {
						do {
							send_packet = getPacket();
							writeSocket(send_packet);
						} while (send_packet.type != Packet.Type.QUIT);
						outputIsClosed = true;
					} catch (IOException e) {
						outputIsClosed = true;
		            	if (inputIsClosed == false) {
                        	close("Disconnected with server: " + e.getMessage());
                        }
                    } finally {
                    	localCache.setOfflineBySocket();
                    	recvQueue.push(new Packet(Packet.Type.QUIT, null));
                    }
				}
			}).start(); 
			
			new Thread(new Runnable() {
				public void run() { 
					Packet recv_packet = null;
					try {
						while (true) { 
							recv_packet = readSocket();
							setPacket(recv_packet);
						}
					} catch (IOException e) { 
                        inputIsClosed = true;
		            	if (outputIsClosed == false) {
		                    close("Disconnected with server: " + e.getMessage());
		                }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        close("Fail to Serialized/Deserialized");
                    } finally {
                    	localCache.setOfflineBySocket();
                    	recvQueue.push(new Packet(Packet.Type.QUIT, null));
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
