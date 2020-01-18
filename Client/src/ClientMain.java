import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import java.awt.Color;
import java.awt.Button;
import java.awt.SystemColor;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import java.sql.SQLException;

public class ClientMain extends JFrame implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private ProxyServer localCache;
	private boolean connected;
	String Name = "Ricky";
	String Password = "123456";
	private JLabel lblReconnect;
	
	int xx,xy;

	/**
	 * Launch the application.
	 */ /*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientMain frame = new ClientMain();
					frame.setUndecorated(true);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/
	
	
	// going to borrow code from a gist to move frame.
	// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	

	/**
	 * Create the frame.
	 */
	public ClientMain(ProxyServer localCache) {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 406, 476);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		this.localCache = localCache;
		localCache.changeView(this);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 102, 204));
		panel.setBounds(0, 0, 48, 490);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblAddfriend = new JLabel("");
		lblAddfriend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				AddFriend addFriend = new AddFriend(localCache);
				addFriend.setVisible(true);
				setVisible(false);
				dispose();
			}
		});
		lblAddfriend.setIcon(new ImageIcon(ClientMain.class.getResource("image/userAdd.png")));
		lblAddfriend.setBounds(0, 112, 48, 71);
		panel.add(lblAddfriend);
		lblAddfriend.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddfriend.setForeground(new Color(241, 57, 83));
		lblAddfriend.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		lblReconnect = new JLabel("");
		lblReconnect.setIcon(new ImageIcon(ClientMain.class.getResource("image/web-icon.png")));
		lblReconnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					connected = localCache.reconnect();
					if (connected)
						lblReconnect.setVisible(false);
				} catch (SQLException e) {
					setErrorMessage(e.getMessage());
				}
			}
		});
		lblReconnect.setHorizontalAlignment(SwingConstants.CENTER);
		lblReconnect.setForeground(new Color(241, 57, 83));
		lblReconnect.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblReconnect.setBounds(0, 358, 48, 71);
		lblReconnect.setVisible(false);
		panel.add(lblReconnect);
		
		JLabel lblLogout = new JLabel("");
		lblLogout.setIcon(new ImageIcon(ClientMain.class.getResource("image/logout.png")));
		lblLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int action = JOptionPane.showConfirmDialog(null, "Do you really want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
				if (action == 0) {
					try {
						localCache.logOut();
						Login login = new Login(localCache);
						login.setVisible(true);
						setVisible(false);
						dispose();
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
		lblMsg.setIcon(new ImageIcon(ClientMain.class.getResource("image/message.png")));
		lblMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				FriendMsg chats = new FriendMsg(localCache);
				chats.setVisible(true);
				setVisible(false);
				dispose();
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
		lblClose.setIcon(new ImageIcon(ClientMain.class.getResource("image/clientX.png")));
		lblClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				System.exit(0);
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
	            ClientMain.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-97, -2, 552, 281);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		
		JLabel lblFriendList = new JLabel("Friend List");
		lblFriendList.setBackground(new Color(0, 102, 204));
		lblFriendList.setBounds(83, 25, 283, 71);
		contentPane.add(lblFriendList);
		lblFriendList.setForeground(new Color(0, 0, 51));
		lblFriendList.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblFriendList.setHorizontalAlignment(SwingConstants.CENTER);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(93, 96, 273, 12);
		contentPane.add(separator);
		
		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(241, 57, 83));
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setBounds(681, -11, 48, 71);
		contentPane.add(label);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setBounds(60, 107, 327, 359);
		contentPane.add(tabbedPane);
		
		JPanel InfoJPanel = new JPanel();
		InfoJPanel.setBackground(Color.WHITE);
		tabbedPane.addTab("Profile", null, InfoJPanel, null);
		
		//get my name and password, need fix
		Name = localCache.getUserName();
		Password = "才不告訴承滿的哥哥";
		
		JLabel myName = new JLabel("My Name: " + Name);
		myName.setHorizontalAlignment(SwingConstants.CENTER);
		myName.setForeground(Color.RED);
		myName.setFont(new Font("Aloisen Groove Text", Font.PLAIN, 30));
		myName.setBackground(new Color(0, 102, 204));
		InfoJPanel.add(myName);
		
		JLabel myPassword = new JLabel("Password: " + Password);
		myPassword.setHorizontalAlignment(SwingConstants.CENTER);
		myPassword.setForeground(Color.ORANGE);
		myPassword.setFont(new Font("Aloisen Groove Text", Font.PLAIN, 23));
		myPassword.setBackground(new Color(0, 102, 204));
		InfoJPanel.add(myPassword);
		
		JPanel myPanel = new JPanel();
		myPanel.setBackground(Color.WHITE);
		tabbedPane.addTab("Friends", null, myPanel, null);
		
		JScrollPane FriendScrollPane = new JScrollPane();
		myPanel.add(FriendScrollPane);
		
		FriendScrollPane.setViewportView(new testPanel(localCache));
		
		GroupLayout gl_FriendsJPanel = new GroupLayout(myPanel);
		gl_FriendsJPanel.setHorizontalGroup(
			gl_FriendsJPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(FriendScrollPane, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
		);
		gl_FriendsJPanel.setVerticalGroup(
			gl_FriendsJPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(FriendScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
		);
		myPanel.setLayout(gl_FriendsJPanel);
	}
	
	public void getOffline() {
		connected = false;
		lblReconnect.setVisible(true);
	}
	public void newMessage(Message message){}
	public void newFriend(User friend){}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
