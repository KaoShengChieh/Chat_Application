import javax.swing.JFrame;

public class View extends JFrame {
	private static final long serialVersionUID = 1L;
	static ProxyServer proxyServer;
	ViewType type;
	
	View() {
		proxyServer.changeView(this);
	}
	
	public void getOffline() {}
	public void newMessage(Message message) {}
	public void newFriend(User friend) {}
	public void setErrorMessage(String message) {}
}
