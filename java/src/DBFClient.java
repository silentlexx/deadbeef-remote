import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DBFClient {
	
	private final static int PACKETSIZE = 100 ;
	 
    public static final String PLAY = "1";
    public static final String PREV = "2";
    public static final String NEXT = "3";
    public static final String STOP = "4";
    public static final String PLAY_PAUSE = "5";
    public static final String PLAY_RANDOM = "6";
    public static final String STOP_AFTER_CURRENT = "7";
    public static final String VOL_UP = "8";
    public static final String VOL_DOWN = "9";
    public static final String SEEK_FW = "a";
    public static final String SEEK_BK = "b";
	
	
	private String mIp;
	private String mPort;

	DBFClient(String ip, String port){
		mIp = ip;
		mPort = port;
	}

	public void send(String cmd){   
		send(cmd, false);
	}
	
	public String send(String cmd, boolean answer){   
		 DatagramSocket socket = null ;

	      try
	      {
	         // Convert the arguments first, to ensure that they are valid
	         InetAddress host = InetAddress.getByName(mIp) ;
	         int port         = Integer.parseInt(  mPort ) ;

	         // Construct the socket
	         socket = new DatagramSocket() ;

	         // Construct the datagram packet
	         byte [] data = cmd.getBytes() ;
	         DatagramPacket packet = new DatagramPacket( data, data.length, host, port ) ;

	         // Send it
	         socket.send( packet ) ;

	         if(answer){
	         // Set a receive timeout, 2000 milliseconds
	         socket.setSoTimeout( 2000 ) ;

	         // Prepare the packet for receive
	         packet.setData( new byte[PACKETSIZE] ) ;

	         // Wait for a response from the server
	         socket.receive( packet ) ;

	         // Print the response
	        // System.out.println( new String(packet.getData()) ) ;
	         if( socket != null )
		            socket.close() ;
	          return new String(packet.getData());
	         }
	         
	         if( socket != null )
		            socket.close() ;
	      }
	      catch( Exception e )
	      {
	       //  System.out.println( e ) ;
	      }
	      
	      return null;

	}
	
}
