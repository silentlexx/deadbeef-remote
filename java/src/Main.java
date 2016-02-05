
public class Main {

	public static void main(String[] args) {
		
		DBFClient dbc = new DBFClient("192.168.0.2", "11122");
		dbc.send(DBFClient.NEXT);
		
	}

}
