import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/*
 * This class implements Facade, Singleton and Factory
 * design patterns to manage calling flow among views.
 * The content is simple but quite significant.
 */

public class View extends JFrame {
	private static final long serialVersionUID = 1L;
	private static Map<ViewType, View> viewMap = new EnumMap<>(ViewType.class);
	private static Map<Integer, ChatBox> chatBoxMap = new HashMap<>();
	protected static ProxyServer proxyServer;
	private ViewType type;
	
	View() {
		proxyServer.changeView(this);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(View.this, 
					"Are you sure you want to exit?", "Exit Application", 
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					proxyServer.quit();
				}
			}
		});
	}
	
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.WARNING_MESSAGE);
	}
	
	public void newMessage(Message message) {
		User myself = proxyServer.getUser();
		int friendID = message.senderID != myself.ID ? message.senderID : message.receiverID;
		
		try {
			User friend = proxyServer.getUser(friendID);
			ChatBox chatBox = getChatBox(friend);
			chatBox.newMessage(message);
		} catch (java.sql.SQLException e) {
			setErrorMessage(e.getMessage());
		}
	}
	
	public void newFriend(User friend) {}

	public void setOnline(boolean isOnline) {
		Iterator<Map.Entry<Integer, ChatBox>> chatBoxEntry = chatBoxMap.entrySet().iterator();
		while (chatBoxEntry.hasNext()) {
			chatBoxEntry.next().getValue().setOnline(isOnline);
		}
	}
	
	public static void setProxyServer(ProxyServer proxyServer) {
		View.proxyServer = proxyServer;
	}
	
	public static void clear() {
		Iterator<Map.Entry<Integer, ChatBox>> chatBoxEntry = chatBoxMap.entrySet().iterator();
		while (chatBoxEntry.hasNext()) {
			chatBoxEntry.next().getValue().dispose();
		}
		
		chatBoxMap.clear();
		
		Iterator<Map.Entry<ViewType, View>> viewEntry = viewMap.entrySet().iterator();
		while (viewEntry.hasNext()) {
			viewEntry.next().getValue().dispose();
		}
		
		viewMap.clear();
	}

	public static View get(ViewType type) {
		if (viewMap.containsKey(type) == false) {
			View view = null;
			switch (type) {
				case LOGIN:
					view = new Login();
					break;
				case REGISTER:
					view = new Register();
					break;
				case PROFILE:
					view = new Home();
					break;
				case FRIEND_LIST:
					view = new FriendList();
					break;
				case ADD_FRIEND:
					view = new AddFriend();
					break;
			}
			view.type = type;
			viewMap.put(type, view);
		}
		return viewMap.get(type);
	}
	
	public static void change(View oldView, ViewType type) {
		View newView = get(type);
		
		switch (oldView.type) {
			case LOGIN: case REGISTER:
				switch (type) {
					default:
						break;
					case LOGIN: case REGISTER:
						newView.setBounds(oldView.getBounds());

				}
				break;
			case PROFILE: case FRIEND_LIST: case ADD_FRIEND:
				switch (type) {
					default:
						break;
					case PROFILE: case FRIEND_LIST: case ADD_FRIEND:
						newView.setBounds(oldView.getBounds());
				}
				break;
		}
		
		proxyServer.changeView(newView);
		newView.setVisible(true);
		oldView.setVisible(false);
	}
	
	public static ChatBox getChatBox(User friend) {
		if (chatBoxMap.containsKey(friend.ID) == false) {
			chatBoxMap.put(friend.ID, new ChatBox(friend));
		}
		return chatBoxMap.get(friend.ID);
	}
}
