import java.awt.EventQueue;
import java.sql.SQLException;

public class ChatApp {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ProxyServer localCache = new LocalCache();
					Login frame = new Login(localCache);
					
					frame.setUndecorated(true);
					frame.setVisible(true);
					
					try {
						if (localCache.autoLogIn()) {
							ClientMain main = new ClientMain(localCache);
							main.setVisible(true);
							frame.setVisible(false);
							frame.dispose();
							return;
						}
					} catch (SQLException e) {
						frame.setErrorMessage(e.getMessage());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
