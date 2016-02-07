package com.silentlexx.deadbeefcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity implements DBFClient.DBFResult {

    private DBFClient ddb = null;

    private  String host;
    private  String port;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setLogo(R.mipmap.ic_launcher);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    public void getVars(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        host = prefs.getString("host", c.getString(R.string.def_host));
        port = prefs.getString("port", c.getString(R.string.def_port));
    }

    private void init(){
        getVars(this);
        if(port==null||host==null||host.equals(getString(R.string.def_host))||host.equals("")||port.equals("")){
            Toast.makeText(this, getString(R.string.def_msg),Toast.LENGTH_SHORT).show();
            startPrefs();
        } else {
            ddb = new DBFClient(this, host, port);
         }
    }

    private void sendUDP(String m) {
        //Toast.makeText(this, "Sending: "+m, Toast.LENGTH_SHORT).show();
        if (ddb != null)
            ddb.send(m);
    }

    public void play(View v) {
        sendUDP(DBFClient.PLAY);
    }

    public void pause(View v) {
        sendUDP(DBFClient.TOGGLE_PAUSE);
    }

    public void stop(View v) {
        sendUDP(DBFClient.STOP);
    }

    public void next(View v) {
        sendUDP(DBFClient.NEXT);
    }

    public void prev(View v) {
        sendUDP(DBFClient.PREV);
    }

    public void shuffle(View v) {
        sendUDP(DBFClient.PLAY_RANDOM);
    }

    public void after(View v) {
        sendUDP(DBFClient.STOP_AFTER_CURRENT);
    }


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
            startPrefs();
            return true;
        } if (id == R.id.action_stop_after) {
            sendUDP(DBFClient.STOP_AFTER_CURRENT);
            Toast.makeText(this, getString(R.string.stop_after), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startPrefs(){
        Intent intent = new Intent( this, SettingsActivity.class );
        intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true );

        this.startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.silentlexx.deadbeefcontrol/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);


        init();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.silentlexx.deadbeefcontrol/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void giveResult(String res) {

    }
}
