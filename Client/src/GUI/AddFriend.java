import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.sql.SQLException;

public class AddFriend extends View {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField SearchFriend;
	private int xx,xy;

	/**
	 * Create the frame.
	 */
	public AddFriend() {
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
				ViewFactory.changeView(AddFriend.this, ViewType.ADD_FRIEND);
			}
		});
		lblAddfriend.setIcon(new ImageIcon(AddFriend.class.getResource("image/userAdd.png")));
		lblAddfriend.setBounds(0, 112, 48, 71);
		panel.add(lblAddfriend);
		lblAddfriend.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddfriend.setForeground(new Color(241, 57, 83));
		lblAddfriend.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JLabel lblLogout = new JLabel("");
		lblLogout.setIcon(new ImageIcon(AddFriend.class.getResource("image/logout.png")));
		lblLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int action = JOptionPane.showConfirmDialog(null, "Do you really want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
				if (action == 0) {
					try {
						proxyServer.logOut();
						ViewFactory.changeView(AddFriend.this, ViewType.LOGIN);
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
		lblMsg.setIcon(new ImageIcon(AddFriend.class.getResource("image/message.png")));
		lblMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ViewFactory.changeView(AddFriend.this, ViewType.FRIEND_LIST);
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
		lblClose.setIcon(new ImageIcon(AddFriend.class.getResource("image/clientX.png")));
		lblClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (JOptionPane.showConfirmDialog(AddFriend.this, 
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
	            AddFriend.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-97, -2, 552, 281);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		
		JLabel lblAddFriend = new JLabel("Add Friend");
		lblAddFriend.setBackground(new Color(0, 102, 204));
		lblAddFriend.setBounds(83, 25, 283, 71);
		contentPane.add(lblAddFriend);
		lblAddFriend.setForeground(new Color(0, 0, 51));
		lblAddFriend.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblAddFriend.setHorizontalAlignment(SwingConstants.CENTER);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(88, 108, 273, 12);
		contentPane.add(separator);
		
		JLabel label = new JLabel("");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(241, 57, 83));
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setBounds(681, -11, 48, 71);
		contentPane.add(label);
		
		JLabel lblSearch = new JLabel("Search ID");
		lblSearch.setBounds(98, 143, 96, 14);
		contentPane.add(lblSearch);
		
		SearchFriend = new JTextField();
		SearchFriend.setColumns(10);
		SearchFriend.setBounds(88, 169, 283, 36);
		contentPane.add(SearchFriend);
		
		Button button = new Button("Search");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String friendName = SearchFriend.getText().toString();
				
				try {
					boolean successful = proxyServer.addFriend(friendName);
					if (successful) {
						JOptionPane.showMessageDialog(null, "Say Hi to your new friend!");
					}
				} catch (SQLException ee) {
					setErrorMessage(ee.getMessage());
				} finally {
					SearchFriend.setText("");
				}
			}
		});
		button.setForeground(Color.ORANGE);
		button.setBackground(Color.YELLOW);
		button.setBounds(164, 229, 131, 36);
		contentPane.add(button);
	}
	
	public void getOffline(){}
}
