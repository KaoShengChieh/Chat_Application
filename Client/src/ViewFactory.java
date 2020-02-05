import java.util.EnumMap;
import java.util.Map;

public class ViewFactory {
	private static Map<ViewType, View> singletonViewMap = new EnumMap<>(ViewType.class);
	
	public static void setProxyServer(ProxyServer proxyServer) {
		View.proxyServer = proxyServer;
	}

	public static View getView(ViewType type) {
		if (singletonViewMap.containsKey(type) == false) {
			View view = null;
			switch (type) {
				case LOGIN:
					view = new Login();
					break;
				case REGISTER:
					view = new Register();
					break;
				case PROFILE:
					view = new Profile();
					break;
				case FRIEND_LIST:
					view = new FriendList();
					break;
				case ADD_FRIEND:
					view = new AddFriend();
					break;
			}
			view.type = type;
			singletonViewMap.put(type, view);
		}
		return singletonViewMap.get(type);
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
}
