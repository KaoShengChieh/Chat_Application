import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import java.sql.SQLException;

//https://github.com/YUbuntu0109/Instant-messaging-software---Java-swing/blob/master/Instant%20messaging%20software%20-%20MyQQ/Source/Document.txt
public class Login extends View {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private JCheckBox chckbxAutologin;
	private boolean autoLogin = false;
	private int xx,xy;
	
	// going to borrow code from a gist to move frame.
	

	/**
	 * Create the frame.
	 */
	public Login() {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 729, 476);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 51));
		panel.setBounds(0, 0, 346, 490);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblTitle = new JLabel("Computer Network so easy~");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblTitle.setForeground(new Color(240, 248, 255));
		lblTitle.setBounds(38, 304, 274, 27);
		panel.add(lblTitle);
		
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
	            Login.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-97, -2, 552, 281);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		lblImg.setIcon(new ImageIcon(Login.class.getResource("image/chat3.png")));
		panel.add(lblImg);
		
		JLabel lblViseTitle = new JLabel("...Enjoy Online Chatting...");
		lblViseTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblViseTitle.setForeground(new Color(240, 248, 255));
		lblViseTitle.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblViseTitle.setBounds(85, 343, 178, 27);
		panel.add(lblViseTitle);
		
		Button btnRegister = new Button("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				View.change(Login.this, ViewType.REGISTER);
			}
		});
		btnRegister.setForeground(new Color(255, 0, 102));
		btnRegister.setBackground(new Color(255, 0, 102));
		btnRegister.setBounds(547, 341, 131, 36);
		contentPane.add(btnRegister);
		
		textField = new JTextField();
		textField.setBounds(395, 178, 283, 36);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblUsername = new JLabel("USERNAME");
		lblUsername.setBounds(395, 152, 114, 14);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("PASSWORD");
		lblPassword.setBounds(395, 245, 96, 14);
		contentPane.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(395, 271, 283, 36);
		contentPane.add(passwordField);
		
		JLabel lbl_close = new JLabel("");
		lbl_close.setIcon(new ImageIcon(Login.class.getResource("image/X.png")));
		lbl_close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (JOptionPane.showConfirmDialog(Login.this, 
					"Are you sure you want to exit?", "Exit Application", 
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					proxyServer.quit();
				}
			}
		});
		lbl_close.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_close.setForeground(new Color(241, 57, 83));
		lbl_close.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lbl_close.setBounds(681, -11, 48, 71);
		contentPane.add(lbl_close);
		
		JLabel lblLogin = new JLabel("Login");
		lblLogin.setForeground(new Color(0, 0, 51));
		lblLogin.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblLogin.setHorizontalAlignment(SwingConstants.CENTER);
		lblLogin.setBounds(395, 23, 283, 71);
		contentPane.add(lblLogin);
		
		Button btnLogin = new Button("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = textField.getText().toString();
				String password = String.valueOf(passwordField.getPassword());
				boolean logInSuccessful = false;
				
				try {
					logInSuccessful = proxyServer.logIn(username, password, autoLogin);
				} catch (SQLException exception) {
					setErrorMessage(exception.getMessage());
				}
				
				if (logInSuccessful) {
					View.change(Login.this, ViewType.PROFILE);
				}
			}
		});
		
		btnLogin.setForeground(new Color(51, 255, 102));
		btnLogin.setBackground(new Color(51, 255, 102));
		btnLogin.setBounds(395, 341, 131, 36);
		contentPane.add(btnLogin);
		
		chckbxAutologin = new JCheckBox("Keep log in");
		chckbxAutologin.setBounds(466, 394, 195, 23);
		contentPane.add(chckbxAutologin);
		
		chckbxAutologin.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// TODO Auto-generated method stub
				autoLogin = chckbxAutologin.isSelected();
			}
		});
	}
}
