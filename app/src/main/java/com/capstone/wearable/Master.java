package com.capstone.wearable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by SidRama on 07/02/16.
 */
public class Master {

    public static final String serverUrl = "http://169.254.107.129:8080";
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static String getLoginUrl() {
        return serverUrl+"/app/loginuser";
    }

    public static String getRegisterUrl() {
        return serverUrl+"/app/register";
    }

    public static String getUploadUrl() {
        return serverUrl+"/app/uploadlocation";
    }

    public static String getEmergencyUrl() {
        return serverUrl + "/app/emergency";
    }
}
