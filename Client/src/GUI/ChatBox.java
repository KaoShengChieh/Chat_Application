import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ChatBox extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textFieldSend;
	private JTextArea textAreaChat;
	private ProxyServer localCache;
	private User friend;

	/**
	 * Create the frame.
	 */
	public ChatBox(ProxyServer localCache, User friend) {
		// TODO something wrong here
		//this.localCache = localCache;
		//localCache.changeView(this);
		this.friend = friend;
		
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 729, 500);
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
		
		String friendName = friend.Name;
		
		JLabel lblTitle = new JLabel(friendName);
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
		lblSend.setIcon(new ImageIcon(ChatBox.class.getResource("image/send.png")));
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
		lblFile.setIcon(new ImageIcon(ChatBox.class.getResource("image/paperclip.png")));
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
		Function_loadHistory();
	}
	public void Function_loadHistory()
	{
		List<Message> history;
		//Get the history
		try {
			int smallestMessageID = -1;
			history = localCache.getMsgHistory(friend, smallestMessageID);
			
			for (int i = history.size() - 1; i >= 0; i--) {
				String message_i = history.get(i).content;
				String time_i = history.get(i).timestamp;
				String from;
				int ID = history.get(i).senderID;
				if (ID == friend.ID) {
					from = friend.Name;
				}
				else {
					from = "Me";
				}
				//Add the message to the chat box.
				textAreaChat.append("(" + time_i + ")" + from + ": \n" + message_i +"\n\n");
			}
			//Let the scroll bar to the bottom.
			textAreaChat.setCaretPosition(textAreaChat.getText().length());
		} catch (SQLException e) {
			setErrorMessage(e.getMessage());
		}
	}
	public void Function_SendMessage()
	{
		//Get the data you want to send.
		String sendMessage = textFieldSend.getText().toString();
		localCache.sendMessage(friend, sendMessage);
	}
	
	public void Function_SendFile()
	{
		if (localCache.sendFile(friend, textFieldSend.getText().toString())) {
			
			textAreaChat.append("(time)" + localCache.getUserName() + ": \n" + textFieldSend.getText().toString() +"\n\n");
			//Let the scroll bar to the bottom.
			textAreaChat.setCaretPosition(textAreaChat.getText().length());
			textFieldSend.setText("");
		}
	}
	
	public void getOffline(){}
	public void newMessage(Message message) {
		String recvMsg = message.content;
		//Add the message to the chat box.
		String usr;
		String time_i = message.timestamp;
		if(message.senderID == friend.ID) {
			usr = friend.Name;
		}
		else {
			usr = "Me";
		}
		textAreaChat.append("(" + time_i + ")" + usr + ": \n" + recvMsg +"\n\n");
		//Let the scroll bar to the bottom.
		textAreaChat.setCaretPosition(textAreaChat.getText().length());
		textFieldSend.setText("");
	}
	public void newFriend(User friend){}
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
}
