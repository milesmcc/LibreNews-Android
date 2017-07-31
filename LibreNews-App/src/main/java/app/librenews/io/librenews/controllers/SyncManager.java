package app.librenews.io.librenews.controllers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import app.librenews.io.librenews.R;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static app.librenews.io.librenews.controllers.DebugManager.sendDebugNotification;

/**
 * Created by miles on 7/15/17.
 */

public class SyncManager {
    Context context;
    FlashManager flashManager;
    SharedPreferences prefs;

    public SyncManager(Context context, FlashManager flashManager) {
        this.context = context;
        this.flashManager = flashManager;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void startSyncService(){
        Intent intent = new Intent(context, RefreshBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 11719, intent, 0);
        String val = prefs.getString("refresh_preference", "60");
        int syncRaw = Integer.valueOf(val);
        long syncInterval = syncRaw*60000; // milliseconds
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(pendingIntent); // to ensure we don't have two sync daemons running
        if(!prefs.getBoolean("automatically_refresh", true)){
            sendDebugNotification("Sync service not enabled (would have been @ " + syncInterval + ")", context);
            return;
            // don't sync if they have disabled syncs!
        }
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + syncInterval,
                syncInterval, pendingIntent);
        sendDebugNotification("Sync service started at an interval of " + syncInterval, context);
    }

    public static class RefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getBoolean("wifi_sync_only", false)) {
                ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                int networkType = connManager.getActiveNetworkInfo().getType();
                if(networkType != ConnectivityManager.TYPE_WIFI){
                    sendDebugNotification("Not on WiFi, not syncing...", context);
                    return;
                }
            }
            sendDebugNotification("Syncing with server...", context);
            FlashManager flashManager = new FlashManager(context);
            flashManager.refresh();
            sendDebugNotification("Sync completed!", context);
        }
    }

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            SyncManager manager = new SyncManager(context.getApplicationContext(), new FlashManager(context.getApplicationContext()));
            manager.startSyncService();
        }
    }
}
