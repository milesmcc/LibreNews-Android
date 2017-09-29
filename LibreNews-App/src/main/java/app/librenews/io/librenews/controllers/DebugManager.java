package app.librenews.io.librenews.controllers;

import android.app.Notification;
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
    private static NotificationChannel mChannel = null;

    public static NotificationChannel getNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mChannel == null) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String CHANNEL_ID = "librenews_debug";
                int importance = NotificationManager.IMPORTANCE_LOW;
                mChannel = new NotificationChannel(CHANNEL_ID, "LibreNews Debug", importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }
            return mChannel;
        } else {
            return null;
        }
    }

    public static void sendDebugNotification(String message, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("debug", false)) {
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder mBuilder =
                        new Notification.Builder(context, getNotificationChannel(context).getId())
                                .setStyle(new Notification.BigTextStyle()
                                        .bigText(message))
                                .setContentText(message);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBuilder.setSmallIcon(R.drawable.ic_debug);
                } else {
                    mBuilder.setSmallIcon(R.drawable.ic_debug_compat);
                }
                mNotificationManager.notify(message.hashCode(), mBuilder.build());
            } else {
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
                mNotificationManager.notify(message.hashCode(), mBuilder.build());
            }
            System.out.println("LibreNews Debug: " + message);
        }
    }
}
