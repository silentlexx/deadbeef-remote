
public class Main {

	private static String host = "localhost";
	private static String port = "11122";
	
	public static void main(String[] args) {
		
		int n = args.length;
		String cmd = null;
		
		if(n==0){
			System.out.println( "client CMD <IP> <PORT>\n" ) ;
			return;
		} else if(n==1){
			cmd = args[0];
		} else if(n==2){
			cmd = args[0];
			host = args[1];
		} else if(n>=3){
			cmd = args[0];
			host = args[1];
			port = args[2];
		}
		
		if(cmd==null){
			return;
		}

		if(cmd.toLowerCase().equals("play")){ cmd = DBFClient.PLAY; } else 
		if(cmd.toLowerCase().equals("stop")){ cmd = DBFClient.STOP; } else 
		if(cmd.toLowerCase().equals("next")){ cmd = DBFClient.NEXT; } else 
		if(cmd.toLowerCase().equals("prev")){ cmd = DBFClient.PREV; } else 
		if(cmd.toLowerCase().equals("random")){ cmd = DBFClient.PLAY_RANDOM; } else 
		if(cmd.toLowerCase().equals("pause")){ cmd = DBFClient.PLAY_PAUSE; } else 
	    if(cmd.toLowerCase().equals("volup")){ cmd = DBFClient.VOL_UP; } else 
		if(cmd.toLowerCase().equals("voldown")){ cmd = DBFClient.VOL_DOWN; } else 
    	if(cmd.toLowerCase().equals("ff")){ cmd = DBFClient.SEEK_FW; } else 
  		if(cmd.toLowerCase().equals("fw")){ cmd = DBFClient.SEEK_BK; } ;
	    	
		DBFClient dbc = new DBFClient(host, port);
		dbc.send(cmd);
		
	
	}

}
