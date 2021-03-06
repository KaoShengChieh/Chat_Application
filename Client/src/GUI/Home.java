import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.event.ChangeEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JTextPane;

public class Home extends View {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextPane txtpnOffline;
	private JTabbedPane tabbedPane;
	private JPanel FriendsListJPanel;
	private int xx,xy;

	/**
	 * Create the frame.
	 */
	public Home() {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 406, 490);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 102, 204));
		panel.setBounds(0, 0, 48, 490);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblAddfriend = new JLabel("");
		lblAddfriend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				View.change(Home.this, ViewType.ADD_FRIEND);
			}
		});
		lblAddfriend.setIcon(new ImageIcon(Home.class.getResource("image/userAdd.png")));
		lblAddfriend.setBounds(0, 112, 48, 71);
		panel.add(lblAddfriend);
		lblAddfriend.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddfriend.setForeground(new Color(241, 57, 83));
		lblAddfriend.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JLabel lblLogout = new JLabel("");
		lblLogout.setIcon(new ImageIcon(Home.class.getResource("image/logout.png")));
		lblLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int action = JOptionPane.showConfirmDialog(null, "Do you really want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
				if (action == 0) {
					try {
						proxyServer.logOut();
						View.change(Home.this, ViewType.LOGIN);
					} catch (SQLException e) {
						setErrorMessage(e.getMessage());
					}
				}
			}
		});
		lblLogout.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogout.setForeground(new Color(241, 57, 83));
		lblLogout.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblLogout.setBounds(0, 162, 48, 71);
		panel.add(lblLogout);
		
		JLabel lblMsg = new JLabel("");
		lblMsg.setIcon(new ImageIcon(Home.class.getResource("image/message.png")));
		lblMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				View.change(Home.this, ViewType.FRIEND_LIST);
			}
		});
		lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
		lblMsg.setForeground(new Color(241, 57, 83));
		lblMsg.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblMsg.setBounds(0, 61, 48, 71);
		panel.add(lblMsg);
		
		JLabel lblClose = new JLabel("");
		lblClose.setBounds(0, 6, 48, 71);
		panel.add(lblClose);
		lblClose.setIcon(new ImageIcon(Home.class.getResource("image/clientX.png")));
		lblClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (JOptionPane.showConfirmDialog(Home.this, 
					"Are you sure you want to exit?", "Exit Application", 
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					proxyServer.quit();
				}
			}
		});
		lblClose.setHorizontalAlignment(SwingConstants.CENTER);
		lblClose.setForeground(new Color(241, 57, 83));
		lblClose.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JLabel lblImg = new JLabel("");
		
		lblImg.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				
				 xx = e.getX();
			     xy = e.getY();
			}
		});
		lblImg.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent arg0) {
				
				int x = arg0.getXOnScreen();
	            int y = arg0.getYOnScreen();
	            Home.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-97, -2, 552, 281);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		
		JLabel lblHome = new JLabel("Home");
		lblHome.setBackground(new Color(0, 102, 204));
		lblHome.setBounds(83, 25, 283, 71);
		contentPane.add(lblHome);
		lblHome.setForeground(new Color(0, 0, 51));
		lblHome.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblHome.setHorizontalAlignment(SwingConstants.CENTER);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(93, 96, 273, 12);
		contentPane.add(separator);
		
		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(241, 57, 83));
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setBounds(681, -11, 48, 71);
		contentPane.add(label);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setBounds(60, 107, 327, 359);
		contentPane.add(tabbedPane);
		
		JPanel profilePanel = new JPanel();
		profilePanel.setBackground(Color.WHITE);
		tabbedPane.addTab("Profile", null, profilePanel, null);
		
		//get my name and password, need fix
		String Name = proxyServer.getUser().name;
		String Password = "才不告訴承滿的哥哥";
		
		JLabel myName = new JLabel("My Name: " + Name);
		myName.setHorizontalAlignment(SwingConstants.CENTER);
		myName.setForeground(Color.RED);
		myName.setFont(new Font("Aloisen Groove Text", Font.PLAIN, 30));
		myName.setBackground(new Color(0, 102, 204));
		profilePanel.add(myName);
		
		JLabel myPassword = new JLabel("Password: " + Password);
		myPassword.setHorizontalAlignment(SwingConstants.CENTER);
		myPassword.setForeground(Color.ORANGE);
		myPassword.setFont(new Font("Aloisen Groove Text", Font.PLAIN, 23));
		myPassword.setBackground(new Color(0, 102, 204));
		profilePanel.add(myPassword);
		
		FriendsListJPanel = new JPanel();
		FriendsListJPanel.setBackground(Color.WHITE);
		tabbedPane.addTab("Friends", null, FriendsListJPanel, null);
		
		txtpnOffline = new JTextPane();
		txtpnOffline.setForeground(Color.WHITE);
		txtpnOffline.setFont(new Font("Dialog", Font.BOLD, 12));
		txtpnOffline.setEditable(false);
		txtpnOffline.setText("You are offline right now");
		txtpnOffline.setBackground(Color.LIGHT_GRAY);
		txtpnOffline.setBounds(45, 0, 362, 22);
		txtpnOffline.setVisible(false);
		StyledDocument doc = txtpnOffline.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		contentPane.add(txtpnOffline);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedIndex() == 0) {
					tabbedPane.remove(1);
					tabbedPane.addTab("Friends", null, FriendsListJPanel, null);
				} else if (tabbedPane.getSelectedIndex() == 1) {
					setFriendListPanel();
				}
			}
		});
	}
	
	private void setFriendListPanel() {
		JScrollPane FriendScrollPane = new JScrollPane();
		FriendsListJPanel.add(FriendScrollPane);
		
		JPanel friendsJPanel = new JPanel();
		friendsJPanel.setLayout(new BoxLayout(friendsJPanel, BoxLayout.Y_AXIS));
		
		List<User> myFriends = null;
		ListIterator<User> itr = null;
		JButton friendButton = null;
		
		try {
			myFriends = proxyServer.getFriendList();
			itr = myFriends.listIterator();
			while (itr.hasNext()) {
				friendButton = getFriendButton(itr.next());
				friendsJPanel.add(friendButton);
			}
		} catch (SQLException exception) {
			setErrorMessage(exception.getMessage());
		}
		
		FriendScrollPane.setViewportView(friendsJPanel);
		
		GroupLayout gl_FriendsJPanel = new GroupLayout(FriendsListJPanel);
		gl_FriendsJPanel.setHorizontalGroup(
			gl_FriendsJPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(FriendScrollPane, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
		);
		gl_FriendsJPanel.setVerticalGroup(
			gl_FriendsJPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(FriendScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
		);
		FriendsListJPanel.setLayout(gl_FriendsJPanel);
	}
	
	private JButton getFriendButton(User friend) {
		JButton friendButton = new JButton(getButtonText(friend.name));
		friendButton.setPreferredSize(new Dimension(290, 100));
		
		friendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ChatBox chatbox = View.getChatBox(friend);
				chatbox.setVisible(true);
			}
		});
		
		return friendButton;
	}
	
	private String getButtonText(String name) {
		String text = "\n" + name + "\n\n";
	
		text = "<html>" + text + "</html>";
	 	text = text.replace("\n", "<br/>").replace(" ", "&nbsp;");
	 	
	 	return text;
	}
	
	public void newFriend(User friend) {
		if (tabbedPane == null || tabbedPane.getSelectedIndex() == 0) {
			return;
		} else if (tabbedPane.getSelectedIndex() == 1) {
			tabbedPane.remove(1);
			tabbedPane.addTab("Friends", null, FriendsListJPanel, null);
			setFriendListPanel();
		}
	}
	
	public void setOnline(boolean isOnline) {
		super.setOnline(isOnline);
		txtpnOffline.setVisible(isOnline == false);
	}
}
