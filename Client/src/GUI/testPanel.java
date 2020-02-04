import javax.swing.JPanel;
import java.util.List;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class testPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private List<Pair<User, Message>> myFriends;
	private int num;
	private JButton btnFriend[];
	private ProxyServer localCache;
	
	/**
	 * Create the panel.
	 */
	public testPanel(ProxyServer localCache) {
		setBackground(new Color(0, 102, 153));
		setLayout(null);
		
		// TODO something wrong here
		//this.localCache = localCache;
		//localCache.changeView(this);
		try {
			myFriends = localCache.getFriendList();
		} catch (SQLException exception) {
			setErrorMessage(exception.getMessage());
		}
		
		num = myFriends.size();
		
		btnFriend = new JButton[10];
		for (int i = 0; i < num; i++) {
			Pair<User, Message> friend = myFriends.get(i);
			String friendName = friend.first.Name;
			String latestMsg = "";
			String time = "";
			if (friend.second != null) {
				latestMsg = friend.second.content;
				time = friend.second.timestamp;
			}
			
			btnFriend[i] = new JButton(friendName + "\t \t" + time + "\n" + latestMsg);
			btnFriend[i].setBounds(6, 6 + 100 * i, 290, 100);
			add(btnFriend[i]);
			btnFriend[i].addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ChatBox chatbox = new ChatBox(localCache, friend.first);
					chatbox.setVisible(true);
				}
			});
		}

	}
	public void getOffline(){}
	public void newMessage(Message message) {
		for (int i = 0; i < num; i++) {
			Pair<User, Message> friend = myFriends.get(i);
			if(friend.first.ID == message.senderID) {
				
				String friendName = friend.first.Name;
				String latestMsg = message.content;
				String time = message.timestamp;
				
				String newMsg = time +  "\n" + friendName + ": " + latestMsg;
				btnFriend[i].setText(newMsg);
				break;
			}
		}
	}
	public void newFriend(User friend){}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
