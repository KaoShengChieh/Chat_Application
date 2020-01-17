public class User implements Cloneable{
	public int ID;
	public String Name;
	public User(int userID, String userName) {
		this.ID = userID;
		this.Name = userName;
	}
	
	public User clone() {
		try {
			return (User)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
