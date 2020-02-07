import java.awt.Color;
import java.awt.SystemColor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ListIterator;
import java.sql.SQLException;

public class ChatBox extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JPanel panelFriendInfo;
	private JTextPane txtpnOffline;
	private JTextField textFieldSend;
	private JTextArea textAreaChat;
	private User friend;

	/**
	 * Create the frame.
	 */
	public ChatBox(User friend) {
		this.friend = friend;
		
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 729, 500);
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panelFriendInfo = new JPanel();
		panelFriendInfo.setBackground(SystemColor.windowBorder);
		panelFriendInfo.setBounds(0, 0, 729, 71);
		contentPane.add(panelFriendInfo);
		panelFriendInfo.setLayout(null);
		
		JLabel lblTitle = new JLabel(friend.name);
		lblTitle.setBackground(UIManager.getColor("Button.darkShadow"));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Toppan Bunkyu Gothic", Font.PLAIN, 24));
		lblTitle.setForeground(Color.DARK_GRAY);
		lblTitle.setBounds(10, 4, 243, 49);
		panelFriendInfo.add(lblTitle);
		
		txtpnOffline = new JTextPane();
		txtpnOffline.setForeground(Color.WHITE);
		txtpnOffline.setFont(new Font("Dialog", Font.BOLD, 12));
		txtpnOffline.setEditable(false);
		txtpnOffline.setText("You are offline right now");
		txtpnOffline.setBackground(Color.LIGHT_GRAY);
		txtpnOffline.setBounds(0, 49, 729, 22);
		txtpnOffline.setVisible(false);
		StyledDocument doc = txtpnOffline.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);
		contentPane.add(txtpnOffline);
		
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
		textAreaChat.setEditable(false);
		textAreaChat.setBackground(SystemColor.window);
		textAreaChat.setLineWrap(true);
		textAreaChat.setFont(new Font("Toppan Bunkyu Gothic", Font.BOLD, 16));
		scrollPaneChat.setViewportView(textAreaChat);
		
		try { 
			Function_loadHistory(View.proxyServer.getMsgHistory(friend, -1));
		} catch (SQLException e) {
			setErrorMessage(e.getMessage());
		}
	}
	
	private void Function_loadHistory(List<Message> history) {
		ListIterator<Message> itr = history.listIterator(history.size());
		
		while(itr.hasPrevious()) {
			Message message = itr.previous();
			int senderID = message.senderID;
			String from = senderID == friend.ID ? friend.name : "Me";
			
			//Add the message to the chat box.
			textAreaChat.append("(" + message.timestamp + ")" + from + ": \n" + message.content +"\n\n");
		}
		//Let the scroll bar to the bottom.
		textAreaChat.setCaretPosition(textAreaChat.getText().length());
	}
	
	private void Function_SendMessage() {
		//Get the data you want to send.
		String sendMessage = textFieldSend.getText().toString();
		View.proxyServer.sendMessage(friend, sendMessage);
	}
	
	private void Function_SendFile() {
		if (View.proxyServer.sendFile(friend, textFieldSend.getText().toString())) {
			
			textAreaChat.append("(time)" + View.proxyServer.getUser().name + ": \n" + textFieldSend.getText().toString() +"\n\n");
			//Let the scroll bar to the bottom.
			textAreaChat.setCaretPosition(textAreaChat.getText().length());
			textFieldSend.setText("");
		}
	}
	
	public void newMessage(Message message) {
		String recvMsg = message.content;
		//Add the message to the chat box.
		String usr = message.senderID == friend.ID ? friend.name : "Me";
		String time_i = message.timestamp;
		
		textAreaChat.append("(" + time_i + ")" + usr + ": \n" + recvMsg + "\n\n");
		//Let the scroll bar to the bottom.
		textAreaChat.setCaretPosition(textAreaChat.getText().length());
		textFieldSend.setText("");
	}
	
	public void setErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, "Error: " + message);
	}
	
	public void setOnline(boolean isOnline) {
		if (isOnline) {
			panelFriendInfo.setBounds(0, 0, 729, 71);
			txtpnOffline.setVisible(false);
		} else {
			panelFriendInfo.setBounds(0, 0, 729, 49);
			txtpnOffline.setVisible(true);
		}
	}
}
