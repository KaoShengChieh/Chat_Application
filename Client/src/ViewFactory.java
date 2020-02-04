import java.util.EnumMap;
import java.util.Map;

public class ViewFactory {
	private static Map<ViewType, View> singletonViewMap = new EnumMap<>(ViewType.class);
	
	public static void setProxyServer(ProxyServer localCache) {
		View.localCache = localCache;
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
}
