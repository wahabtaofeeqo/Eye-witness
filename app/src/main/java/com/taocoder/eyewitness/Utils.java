package com.taocoder.eyewitness;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static boolean isWifiCOn;
    private  static boolean isMobileCon;

    public static final int VIDEO_REQUEST_CODE = 100;
    public static final int PICTURE_REQUEST_CODE = 200;
    public static final int AUDIO_REQUEST_CODE = 300;

    public static final int AUDIO_PERMISSION = 50;

    public static final String BASE_URL = "";
    public static final String VIDEOS_URL = BASE_URL + "/uploads/videos/";
    public static final String PICTURES_URL = BASE_URL + "/uploads/pictures/";
    public static final String AUDIOS_URL = BASE_URL + "/uploads/audios/";

    public static void showMessage(final Context context, final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void logMessage(Context context, String msg) {
        Log.i(Utils.class.getSimpleName(), msg);
    }

    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(new Date());
    }

    public static String getTime() {

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        return format.format(new Date());
    }

    public static boolean isNetworkOn(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            for (Network network: manager.getAllNetworks()) {

                NetworkInfo info = manager.getNetworkInfo(network);

                if (info.getType() == ConnectivityManager.TYPE_WIFI)
                    isWifiCOn = info.isConnectedOrConnecting();
                else
                    isMobileCon = info.isConnectedOrConnecting();
            }
        }
        else {

            for(NetworkInfo info : manager.getAllNetworkInfo()) {
                if (info.getType() == ConnectivityManager.TYPE_WIFI)
                    isWifiCOn = info.isConnectedOrConnecting();
                else isMobileCon = info.isConnectedOrConnecting();
            }
        }

        return isWifiCOn || isMobileCon;
    }
}