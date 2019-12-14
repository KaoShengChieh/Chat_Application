public interface View {
	void getOffline();
	void newMessage(Message message);
	void newFriend(User friend);
	void setErrorMessage(String message);
}
