import javax.swing.JPanel;

import java.util.List;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class testPanel extends JPanel implements View{
	
	private ProxyServer localCache;
	private List<Pair<User, Message>> myFriends;
	/**
	 * Create the panel.
	 */
	public testPanel(ProxyServer localCache) {
		setBackground(new Color(0, 102, 153));
		setLayout(null);
		
		this.localCache = localCache;
		try {
			myFriends = localCache.getFriendList();
		} catch (SQLException exception) {
			setErrorMessage(exception.getMessage());
		}
		
		int num = myFriends.size();
		
		JButton btnFriend[] = new JButton[10];
		for (int i = 0; i < num; i++) {
			Pair<User, Message> friend = myFriends.get(i);
			String friendName = friend.first.Name;
			String latestMsg = friend.second.content;
			String time = friend.second.timestamp;
			
			btnFriend[i] = new JButton(time +  "\n" + friendName + ": " + latestMsg);
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
		
	}
	public void newFriend(User friend){}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
