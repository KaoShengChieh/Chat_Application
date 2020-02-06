import java.sql.SQLException;

public class ChatApp {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ProxyServer localCache = new LocalCache();
			ViewFactory.setProxyServer(localCache);
			
			View frame = ViewFactory.getView(ViewType.LOGIN);
			frame.setUndecorated(true);
			frame.setVisible(true);
			
			try {
				if (localCache.autoLogIn()) {
					ViewFactory.changeView(frame, ViewType.PROFILE);
					return;
				}
			} catch (SQLException e) {
				frame.setErrorMessage(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
