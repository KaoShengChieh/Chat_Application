import java.sql.SQLException;

public class ChatApp {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ProxyServer localCache = new LocalCache();
			View.setProxyServer(localCache);
			
			View frame = View.get(ViewType.LOGIN);
			frame.setUndecorated(true);
			frame.setVisible(true);
			
			try {
				if (localCache.autoLogIn()) {
					View.change(frame, ViewType.PROFILE);
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
