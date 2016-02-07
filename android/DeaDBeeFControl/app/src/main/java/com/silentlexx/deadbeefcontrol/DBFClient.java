package com.silentlexx.deadbeefcontrol;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DBFClient {
    public interface DBFResult {
        public void giveResult(String res);
    }

    private final static String NOT_NEED_ANSWER = "not_need_answer";
    private final static String NEED_ANSWER = "need_answer";
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


    private DBFResult mCtx = null;

    private Handler h = new Handler();

    DBFClient(DBFResult ctx, String ip, String port){
        mCtx = ctx;
        mIp = ip;
        mPort = port;

    }

    public void send(final String cmd) {
            new sending().execute(NOT_NEED_ANSWER, cmd);
    }

    public void send(final String cmd, boolean na) {
        if(na) {
            new sending().execute(NEED_ANSWER, cmd);
        } else {
            new sending().execute(NOT_NEED_ANSWER, cmd);
        }

    }

    private void result(String res){
        mCtx.giveResult(res);
    }

    private class sending extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... cmd) {
            DatagramSocket socket = null;


            try {
                // Convert the arguments first, to ensure that they are valid
                InetAddress host = InetAddress.getByName(mIp);
                int port = Integer.parseInt(mPort);

                // Construct the socket
                socket = new DatagramSocket();

                // Construct the datagram packet
                byte[] data = cmd[1].getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, host, port);

                // Send it
                socket.send(packet);

                if(cmd[0].equals(NEED_ANSWER)) {
                    // Set a receive timeout, 2000 milliseconds
                    socket.setSoTimeout(2000) ;

                    // Prepare the packet for receive
                     packet.setData(new byte[PACKETSIZE]) ;

                    // Wait for a response from the server
                     socket.receive( packet ) ;

                    // Print the response
                    if (socket != null) socket.close();
                    return new String(packet.getData()) ;

                }

                if (socket != null) socket.close();
            } catch (Exception e) {
                Log.e("UDP", "Sendind error", e);
                //  System.out.println( e ) ;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            result(res);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
