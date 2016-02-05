package com.silentlexx.deadbeefcontrol;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private DBFClient ddb = null;

    private String host = "192.168.0.2";
    private String port = "11122";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ddb = new DBFClient(this, host, port);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    private void sendUDP(String m){
        //Toast.makeText(this, "Sending: "+m, Toast.LENGTH_SHORT).show();
        if(ddb!=null)
            ddb.send(m);
    }

    public void play(View v){
        sendUDP(DBFClient.PLAY);
    }

    public void pause(View v){
        sendUDP(DBFClient.TOGGLE_PAUSE);
    }

    public void stop(View v){
        sendUDP(DBFClient.STOP);
    }

    public void next(View v){
        sendUDP(DBFClient.NEXT);
    }

    public void prev(View v){
        sendUDP(DBFClient.PREV);
    }

    public void random(View v){
        sendUDP(DBFClient.PLAY_RANDOM);
    }

    public void after(View v){  sendUDP(DBFClient.STOP_AFTER_CURRENT);   }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
