import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ClientHandler implements Runnable {
	private Socket socket;
	private ObjectInputStream inputObject;
	private ObjectOutputStream outputObject;
	private List<ClientHandler> clientList;
	private BlockingQueue<Packet> sendQueue;
	private BlockingQueue<Packet> recvQueue;
	public Client client;
	
	public ClientHandler(Socket socket, ObjectInputStream inputObject,
		ObjectOutputStream outputObject, List<ClientHandler> clientList) { 
		this.socket = socket;
		this.inputObject = inputObject; 
		this.outputObject = outputObject; 
		clientList = clientList;
		sendQueue = new BlockingQueue<>();
		recvQueue = new BlockingQueue<>();
		client = new Client(sendQueue, recvQueue, clientList);
	}

	public void run() {
		try {
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
		            	e.printStackTrace();
		            	close("Disconnected with server");
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
					} catch (IOException e) { 
		                e.printStackTrace();
		                close("Disconnected with server");
		            }
				}
			}).start();
			
			client.start();
			
			synchronized (client) {
				while (client.isQuit() == false) {
					try {
						wait();
					} catch (InterruptedException e)  {
						Thread.currentThread().interrupt(); 
					}
				}
			}
			
			close();
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
	
	private void close() {
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
			System.out.println("Connection with " + (client.userID() >= 0 ? "unknown user" : "user " + client.userID()) + " closed");
			clientList.remove(this);
		}
	}
	
	private void close(String errMessage) {
		System.err.println(errMessage);
		close();
	}
	
	private void printPacket(Packet packet, String assistMessage) {
		Message message = packet.message;
		
		System.err.println("----------" + assistMessage + "----------");
		System.err.println("[type] " + packet.type.toString());
		System.err.println("[msgID] " + message.msgID);
		System.err.println("[senderID] " + message.senderID);
		System.err.println("[receiverID] " + message.receiverID);
		System.err.println("[timestamp] " + message.timestamp);
		System.err.println("[content] " + message.content);
	}
}
