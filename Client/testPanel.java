import javax.swing.JPanel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class testPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public testPanel() {
		setBackground(new Color(0, 102, 153));
		setLayout(null);
		
		int num = 2;
		
		JButton btnFriend[] = new JButton[10];
		for (int i = 0; i < num; i++) {
			String friendName = "FriendName";
			
			btnFriend[i] = new JButton(friendName);
			btnFriend[i].setBounds(6, 6 + 100 * i, 290, 100);
			add(btnFriend[i]);
			btnFriend[i].addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ChatBox chatbox = new ChatBox();
					chatbox.setVisible(true);
					//JOptionPane.showMessageDialog(chatbox, "hello");
				}
			});
		}

	}
}
