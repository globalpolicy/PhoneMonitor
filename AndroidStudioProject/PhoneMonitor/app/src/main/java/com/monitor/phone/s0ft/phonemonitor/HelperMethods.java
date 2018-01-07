package com.monitor.phone.s0ft.phonemonitor;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.Callable;


public class HelperMethods {


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

    static String getIMEI(Context context) {
        String retval = "";
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            retval = telephonyManager.getDeviceId();
        }
        return retval;
    }

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
}
