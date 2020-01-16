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
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChatBox extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private JPanel contentPane;
	
	private JTextField textFieldSend;
	private JTextArea textAreaChat;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatBox frame = new ChatBox();
					//frame.setUndecorated(true);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	

	/**
	 * Create the frame.
	 */
	public ChatBox() {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 729, 476);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panelFriendInfo = new JPanel();
		panelFriendInfo.setBackground(SystemColor.windowBorder);
		panelFriendInfo.setBounds(0, 0, 773, 71);
		contentPane.add(panelFriendInfo);
		panelFriendInfo.setLayout(null);
		
		JLabel lblTitle = new JLabel("Friend Username");
		lblTitle.setBackground(UIManager.getColor("Button.darkShadow"));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Toppan Bunkyu Gothic", Font.PLAIN, 24));
		lblTitle.setForeground(Color.DARK_GRAY);
		lblTitle.setBounds(10, 16, 243, 49);
		panelFriendInfo.add(lblTitle);
		
		JPanel panelSend = new JPanel();
		panelSend.setLayout(null);
		panelSend.setBackground(Color.WHITE);
		panelSend.setBounds(0, 403, 773, 71);
		contentPane.add(panelSend);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(54, 175, 615, 48);
		panelSend.add(scrollPane);
		
		textFieldSend = new JTextField();
		textFieldSend.setColumns(10);
		textFieldSend.setBounds(54, 17, 615, 48);
		panelSend.add(textFieldSend);
		
		//scrollPane.setViewportView(textFieldSend);
		
		JLabel lblSend = new JLabel("");
		lblSend.setBounds(666, 6, 61, 65);
		panelSend.add(lblSend);
		lblSend.setIcon(new ImageIcon(ChatBox.class.getResource("/send.png")));
		lblSend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Function_SendMessage();
			}
		});
		lblSend.setHorizontalAlignment(SwingConstants.CENTER);
		lblSend.setForeground(new Color(241, 57, 83));
		lblSend.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		JLabel lblFile = new JLabel("");
		lblFile.setHorizontalAlignment(SwingConstants.CENTER);
		lblFile.setForeground(new Color(241, 57, 83));
		lblFile.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblFile.setBounds(0, 6, 61, 65);
		panelSend.add(lblFile);
		lblFile.setIcon(new ImageIcon(ChatBox.class.getResource("/paperclip.png")));
		lblFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Function_SendFile();
			}
		});
		
		JScrollPane scrollPaneChat = new JScrollPane();
		scrollPaneChat.setBounds(-1, 67, 731, 339);
		contentPane.add(scrollPaneChat);
		
		textAreaChat = new JTextArea();
		textAreaChat.setBackground(SystemColor.window);
		textAreaChat.setLineWrap(true);
		textAreaChat.setFont(new Font("Toppan Bunkyu Gothic", Font.BOLD, 16));
		scrollPaneChat.setViewportView(textAreaChat);
		lblFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}
		});
		
		InitChat();
	}
	public void InitChat() {
		int port;
		while(true) {
			Function_receiveMessage();
		}
	}
	
	public void Function_receiveMessage()
	{
		String recvMsg = null;
		//Add the message to the chat box.
		textAreaChat.append("Friend: " + "\n" + recvMsg +"\n\n");
		//Let the scroll bar to the bottom.
		textAreaChat.setCaretPosition(textAreaChat.getText().length());
	}
	
	protected void Function_SendMessage()
	{
		String friend_IPAdress;
		String friend_RemotePort;
		
		
		//Get the data you want to send.
		String sendMessage = textFieldSend.getText().toString();
		//Store the data.
		byte data[] = sendMessage.getBytes();
		//Display the message to the chat box if you send it to your friend.
		textAreaChat.append("Me: " + "\n" + sendMessage +"\n\n");
		//Let the scroll bar to the bottom.
		textAreaChat.setCaretPosition(textAreaChat.getText().length());
		//Send the message.
		textFieldSend.setText("");
	}
	
	protected void Function_SendFile()
	{
//		String filePath = new FileChooser_JFrame().getFilePath();
//		//Test
//		System.out.println(filePath);
	}
}
