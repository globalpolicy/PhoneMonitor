package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class OnBootCompletedReceiver extends BroadcastReceiver {
    //starts main service on device boot
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent startMainServiceIntent=new Intent(context,MainService.class);
            context.startService(startMainServiceIntent);
        }
    }
}
