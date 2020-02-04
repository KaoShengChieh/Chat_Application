import java.awt.Rectangle;
import javax.swing.JFrame;

public class View extends JFrame {
	private static final long serialVersionUID = 1L;
	protected static ProxyServer localCache;
	protected ViewType type;
	
	public View() {
		localCache.changeView(this);
	}

	public void changeTo(ViewType type) {
		View newView = ViewFactory.getView(type);
		Rectangle oldBounds = this.getBounds();
		
		switch (this.type) {
			case LOGIN: case REGISTER:
				switch (type) {
					case LOGIN: case REGISTER:
						newView.setBounds(this.getBounds());
				}
				break;
			case PROFILE: case FRIEND_LIST: case ADD_FRIEND:
				switch (type) {
					case PROFILE: case FRIEND_LIST: case ADD_FRIEND:
						newView.setBounds(this.getBounds());
				}
				break;
		}
		
		localCache.changeView(newView);
		newView.setVisible(true);
		this.setVisible(false);
	}
	
	public void getOffline() {}
	public void newMessage(Message message) {}
	public void newFriend(User friend) {}
	public void setErrorMessage(String message) {}
}
