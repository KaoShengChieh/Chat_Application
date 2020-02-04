import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Button;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Register extends View {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;
	private int xx,xy;

	/**
	 * Create the frame.
	 */
	public Register() {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 729, 476);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(153, 0, 51));
		panel.setBounds(0, 0, 346, 490);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblTitle = new JLabel("Computer Network so easy~");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblTitle.setForeground(new Color(240, 248, 255));
		lblTitle.setBounds(56, 304, 241, 27);
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
	            Register.this.setLocation(x - xx, y - xy);  
			}
		});
		lblImg.setBounds(-38, 0, 420, 275);
		lblImg.setVerticalAlignment(SwingConstants.TOP);
		lblImg.setIcon(new ImageIcon(Register.class.getResource("image/friends2.png")));
		panel.add(lblImg);
		
		JLabel lblViseTitle = new JLabel("...Enjoy Online Chatting...");
		lblViseTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblViseTitle.setForeground(new Color(240, 248, 255));
		lblViseTitle.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblViseTitle.setBounds(100, 343, 153, 27);
		panel.add(lblViseTitle);
		
		Button button = new Button("Register Complete. Login!");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = textField.getText().toString();
				String password = String.valueOf(passwordField.getPassword());
				String passwordConfirm = String.valueOf(passwordField_1.getPassword());
				boolean signUpSuccessful = false;
				
				if (password.equals(passwordConfirm) && !username.equals("")) {
					signUpSuccessful = localCache.signUp(username, password);
					
					if (signUpSuccessful) {
						JOptionPane.showMessageDialog(null, "Successfully Registered!");

						changeTo(ViewType.LOGIN);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Invalid username or password not matched!");
					textField.setText("");
					passwordField.setText("");
					passwordField_1.setText("");
				}
			}
		});
		button.setForeground(new Color(255, 0, 102));
		button.setBackground(new Color(255, 0, 102));
		button.setBounds(395, 361, 283, 36);
		contentPane.add(button);
		
		textField = new JTextField();
		textField.setBounds(395, 144, 283, 36);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblUsername = new JLabel("USERNAME");
		lblUsername.setBounds(395, 118, 114, 14);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("PASSWORD");
		lblPassword.setBounds(395, 192, 96, 14);
		contentPane.add(lblPassword);
		
		JLabel lblPasswordConfirm = new JLabel("PASSWORD CONFIRM");
		lblPasswordConfirm.setBounds(395, 266, 140, 14);
		contentPane.add(lblPasswordConfirm);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(395, 218, 283, 36);
		contentPane.add(passwordField);
		
		passwordField_1 = new JPasswordField();
		passwordField_1.setBounds(395, 292, 283, 36);
		contentPane.add(passwordField_1);
		
		JLabel lbl_close = new JLabel("");
		lbl_close.setIcon(new ImageIcon(Register.class.getResource("image/X.png")));
		lbl_close.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				System.exit(0);
			}
		});
		lbl_close.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_close.setForeground(new Color(241, 57, 83));
		lbl_close.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lbl_close.setBounds(681, -11, 48, 71);
		contentPane.add(lbl_close);
		
		JLabel lblRegister = new JLabel("Register");
		lblRegister.setForeground(new Color(153, 0, 51));
		lblRegister.setFont(new Font("Yuppy TC", Font.PLAIN, 48));
		lblRegister.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegister.setBounds(395, 23, 283, 71);
		contentPane.add(lblRegister);
	}
	
	public void getOffline(){}
	public void newMessage(Message message){}
	public void newFriend(User friend){}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
