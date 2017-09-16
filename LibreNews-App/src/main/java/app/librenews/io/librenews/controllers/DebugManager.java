package app.librenews.io.librenews.controllers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import app.librenews.io.librenews.R;

/**
 * Created by miles on 7/15/17.
 */

public class DebugManager {
    public static void sendDebugNotification(String message, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean("debug", false)){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message))
                            .setContentText(message);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.drawable.ic_debug);
            } else {
                mBuilder.setSmallIcon(R.drawable.ic_debug_compat);
            }
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // for Android 8 compatibility
            System.out.println(Build.VERSION.SDK_INT);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String CHANNEL_ID = "librenews_debug";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "LibreNews Debug", importance);
                mNotificationManager.createNotificationChannel(mChannel);
                mBuilder.setChannel(CHANNEL_ID);
                System.out.println("OREO");
            }
            mNotificationManager.notify(message.hashCode(), mBuilder.build());
            System.out.println("LibreNewz Debug: " + message);
        }
    }
}
