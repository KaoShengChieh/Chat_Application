import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class View extends JFrame {
	private static final long serialVersionUID = 1L;
	static ProxyServer proxyServer;
	ViewType type;
	
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
	
	public void getOffline() {}
	public void newMessage(Message message) {
		User myself = proxyServer.getUser();
		int friendID = message.senderID != myself.ID ? message.senderID : message.receiverID;
		
		try {
			User friend = proxyServer.getUser(friendID);
			ChatBox chatBox = ViewFactory.getChatBox(friend);
			chatBox.newMessage(message);
		} catch (SQLException e) {
			setErrorMessage(e.getMessage());
		}
	}
	public void newFriend(User friend) {}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
