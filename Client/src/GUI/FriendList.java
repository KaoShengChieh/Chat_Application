import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.util.List;
import java.util.ListIterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FriendList extends View {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private Map<Integer, JButton> buttonsMap = new HashMap<>();
	private Map<Integer, String> friendMap = new HashMap<>();
	private int xx,xy;

	/**
	 * Create the frame.
	 */
	public FriendList() {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 406, 476);
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
				ViewFactory.changeView(FriendList.this, ViewType.ADD_FRIEND);
			}
		});
		lblAddfriend.setIcon(new ImageIcon(FriendList.class.getResource("image/userAdd.png")));
		lblAddfriend.setBounds(0, 112, 48, 71);
		panel.add(lblAddfriend);
		lblAddfriend.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddfriend.setForeground(new Color(241, 57, 83));
		lblAddfriend.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JLabel lblLogout = new JLabel("");
		lblLogout.setIcon(new ImageIcon(FriendList.class.getResource("image/logout.png")));
		lblLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int action = JOptionPane.showConfirmDialog(null, "Do you really want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
				if (action == 0) {
					try {
						proxyServer.logOut();
						ViewFactory.changeView(FriendList.this, ViewType.LOGIN);
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
		lblMsg.setIcon(new ImageIcon(FriendList.class.getResource("image/user.png")));
		lblMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ViewFactory.changeView(FriendList.this, ViewType.PROFILE);
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
		lblClose.setIcon(new ImageIcon(FriendList.class.getResource("image/clientX.png")));
		lblClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (JOptionPane.showConfirmDialog(FriendList.this, 
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
	            FriendList.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-97, -2, 552, 281);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		
		JLabel lblChats = new JLabel("Chats");
		lblChats.setBackground(new Color(0, 102, 204));
		lblChats.setBounds(83, 25, 283, 71);
		contentPane.add(lblChats);
		lblChats.setForeground(new Color(0, 0, 51));
		lblChats.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblChats.setHorizontalAlignment(SwingConstants.CENTER);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(93, 96, 273, 12);
		contentPane.add(separator);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setBounds(60, 107, 327, 359);
		contentPane.add(tabbedPane);
		
		setFriendListPanel();
	}
	
	public void setFriendListPanel() {
		JPanel FriendsListJPanel = new JPanel();
		FriendsListJPanel.setBackground(Color.WHITE);
		tabbedPane.removeAll();
		tabbedPane.addTab("Chats", null, FriendsListJPanel, null);
		
		JScrollPane FriendScrollPane = new JScrollPane();
		JPanel friendsJPanel = new JPanel();
		friendsJPanel.setLayout(new BoxLayout(friendsJPanel, BoxLayout.Y_AXIS));
		
		List<Pair<User, Message>> myFriends = null;
		ListIterator<Pair<User, Message>> itr = null;
		JButton friendButton = null;
		Pair<User, Message> friendInfo = null;
		
		try {
			myFriends = proxyServer.getFriendListWithNewestMessage();
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			Collections.sort(myFriends, new Comparator<Pair<User, Message>>() {
				public int compare(Pair<User, Message> friend1, Pair<User, Message> friend2) {
					 return (int)ChronoUnit.SECONDS.between(
							 LocalDateTime.parse(friend1.second.timestamp,formatter),
							 LocalDateTime.parse(friend2.second.timestamp,formatter));
				}
			});
			itr = myFriends.listIterator();
			while (itr.hasNext()) {
				friendInfo = itr.next();
				if (friendInfo.second == null) {
					System.err.println(friendInfo.first.name + "did not send message");
					continue;
				}
				friendButton = getFriendButton(friendInfo);
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
	
	private JButton getFriendButton(Pair<User, Message> friend) {
		int friendID = friend.first.ID;
		String friendName = friend.first.name;
		String timestamp = friend.second.timestamp;
		String latestMsg = friend.second.content;
		
		JButton friendButton = new JButton(getButtonText(friendName, timestamp, latestMsg));
		friendButton.setPreferredSize(new Dimension(290, 100));
		
		friendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ChatBox chatbox = ViewFactory.getChatBox(friend.first);
				chatbox.setVisible(true);
			}
		});
		
		buttonsMap.put(friendID, friendButton);
		friendMap.put(friendID, friendName);
		
		return friendButton;
	}
	
	private String getButtonText(String name, String timestamp, String content) {
		String text = "\n" +
			name + " ".repeat(20-name.length()) + timestamp + "\n" +
			" ".repeat(40) + "\n" +
			(content.length() < 30 ? content : content.substring(0, 30) + "...") +
			"\n\n";
	
		text = "<html>" + text + "</html>";
	 	text = text.replace("\n", "<br/>").replace(" ", "&nbsp;");
	 	
	 	return text;
	}
	
	public void getOffline() {}
	public void newMessage(Message message) {
		super.newMessage(message);
		setFriendListPanel();
	}
	
	public void newFriend(User friend) {
		setFriendListPanel();
	}
}
