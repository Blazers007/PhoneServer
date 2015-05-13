package com.blazers.app.phoneserver;

import android.content.*;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private TextView console;
    private ServiceConnection serviceConnection;
    /* Receiver */
    private MyReceiver receiver;
    /* AIDL Binder */
    private IMyAidlInterface aidlInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    void init() {
        /* Bind Views */
        console = (TextView) findViewById(R.id.console);
        /* Init Receiver */
        receiver = new MyReceiver();
        /* Init Connection */
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                aidlInterface = IMyAidlInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* Bind Receiver For receiver message */
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.console.update");
        registerReceiver(receiver, filter);
        /* Start Server */
        Intent bindService = new Intent(this, HttpServer.class);
        bindService(bindService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        /* Stop Receiver */
        unregisterReceiver(receiver);
    }

    public void startServer(View view) {
        try {
            aidlInterface.startServer(8086);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            console.setText(console.getText().toString() + "\r\n" + intent.getStringExtra("msg"));
        }
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
