import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Statement;
import java.util.List;

public class ClientHandler implements Runnable {
	private Socket socket;
	private ObjectInputStream inputObject;
	private ObjectOutputStream outputObject;
	private List<ClientHandler> clientList;
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	private boolean inputIsClosed;
	private boolean outputIsClosed;
	public Client client;
	
	public ClientHandler(Socket socket, ObjectInputStream inputObject,
		ObjectOutputStream outputObject, List<ClientHandler> clientList, Statement stmt) { 
		this.socket = socket;
		this.inputObject = inputObject; 
		this.outputObject = outputObject; 
		this.clientList = clientList;
		sendQueue = new BlockingQueue<>();
		recvQueue = new BlockingQueue<>();
		SocketAddress socketAddress = socket.getRemoteSocketAddress();
		InetAddress inetAddress = ((InetSocketAddress)socketAddress).getAddress();
		String clientAddress = inetAddress.toString().split("/")[1];
		client = new Client(clientAddress, sendQueue, recvQueue, this, clientList, stmt);
	}

	public void run() {
		try {
			new Thread(client).start(); 
			
			new Thread(new Runnable() { 
				public void run() { 
					Packet recv_packet = null;
					try {
						while (true) { 
							recv_packet = readSocket();
							printPacket(recv_packet, "recv from client");
							setPacket(recv_packet);
						}
					} catch (IOException e) {
						inputIsClosed = true;
		            	if (outputIsClosed == false) {
				        	close(e.getMessage());
		            	}
		            } catch (ClassNotFoundException e) {
		                e.printStackTrace();
		                close("Fail to Serialized/Deserialized");
		            }
				} 
			}).start();
			
			new Thread(new Runnable() { 
				public void run() { 
					Packet send_packet = null;
					try {
						do {
							send_packet = getPacket();
							printPacket(send_packet, "send from server");
							writeSocket(send_packet);
						} while (send_packet.type != Packet.Type.QUIT);
						outputIsClosed = true;
					} catch (IOException e) {
						outputIsClosed = true;
		            	if (inputIsClosed == false) {
				        	close(e.getMessage());
		            	}
		            }
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			close("Unexpected error");
		}
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
			clientList.remove(this);
			System.out.println("Connection with " + (client.userID >= 0 ? "user " + client.userID : "unknown user") + " closed");
		}
	}
	
	private void close(String errMessage) {
		System.err.println(errMessage);
		close();
	}
	
	private void printPacket(Packet packet, String assistantMessage) {
		Message message = packet.message;
		
		System.err.println("----------" + assistantMessage + "----------");
		System.err.println("[type] " + packet.type.toString());
		
		if (message != null) {
			System.err.println("[msgID] " + message.msgID);
			System.err.println("[senderID] " + message.senderID);
			System.err.println("[receiverID] " + message.receiverID);
			System.err.println("[timestamp] " + message.timestamp);
			System.err.println("[content] " + message.content);
		}
	}
}
