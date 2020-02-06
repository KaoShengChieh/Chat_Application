import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/*
 * This static class implements Singleton and Factory
 * design pattern to manage calling flow among views.
 * The content is simple but quite significant.
 */

public class ViewFactory {
	private static Map<ViewType, View> viewMap = new EnumMap<>(ViewType.class);
	private static Map<Integer, ChatBox> chatBoxMap = new HashMap<>();
	
	public static void setProxyServer(ProxyServer proxyServer) {
		View.proxyServer = proxyServer;
	}
	
	public static void clear() {
		chatBoxMap.clear();
		viewMap.clear();
	}

	public static View getView(ViewType type) {
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
	
	public static void changeView(View oldView, ViewType type) {
		View newView = ViewFactory.getView(type);
		
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
		
		View.proxyServer.changeView(newView);
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
