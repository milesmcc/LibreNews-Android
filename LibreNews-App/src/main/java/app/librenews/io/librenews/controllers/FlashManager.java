package app.librenews.io.librenews.controllers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.models.Flash;
import app.librenews.io.librenews.views.MainFlashActivity;

import static android.graphics.Color.argb;

/**
 * Created by miles on 7/14/17.
 */

public class FlashManager {

    int flashesToStoreInDatabase = 100;
    final String flashFileLocation = "flashes.json";
    SharedPreferences prefs;
    String serverUrl;
    String serverName;
    Context context;
    public static ConnectionStatus lastContactSuccessful = ConnectionStatus.NEUTRAL;

    private static NotificationChannel mChannel = null;
    public static NotificationChannel getNotificationChannel(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mChannel == null) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String CHANNEL_ID = "librenews_alert";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                mChannel = new NotificationChannel(CHANNEL_ID, "LibreNews", importance);
                mChannel.setLightColor(argb(0, 121, 145, 1));
                mNotificationManager.createNotificationChannel(mChannel);
            }
            return mChannel;
        }else{
            return null;
        }
    }

    public enum ConnectionStatus {
        VALID,
        INVALID,
        NEUTRAL;

        public int color(){
            if(this.equals(VALID)){
                return Color.parseColor("#3fee3f");
            }
            if(this.equals(INVALID)){
                return Color.parseColor("#ff3f3f");
            }
            if(this.equals(INVALID)){
                return Color.parseColor("#808080");
            }
            return Color.parseColor("#808080");
        }
    }

    public FlashManager(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.serverUrl = prefs.getString("server_url", "https://librenews.io/api");
        lastContactSuccessful = ConnectionStatus.NEUTRAL;
        try {
            loadFlashesFromStorage();
        } catch (FileNotFoundException exception) {
            try {
                refresh();
            } catch (Exception exception2) {
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.internal_storage_setup_fail), Toast.LENGTH_LONG);
                exception2.printStackTrace();
            }
        } catch (Exception exception) {
            Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.internal_storage_read_fail), Toast.LENGTH_LONG);
            exception.printStackTrace();
        }

        // first things first: get everything syncing!
        SyncManager syncManager = new SyncManager(context, this);
        syncManager.startSyncService();
    }

    private String readFlashFile() throws IOException {
        File file = new File(context.getFilesDir(), flashFileLocation);
        FileInputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append('\n'); // you'd think Java would have a better way of handling this, but no!
        }
        reader.close();
        inputStream.close();
        return builder.toString();
    }

    public ArrayList<Flash> loadFlashesFromStorage() throws JSONException, IOException, ParseException {
        String flashFile = readFlashFile();
        ArrayList<Flash> latestPushedFlashes = new ArrayList<>();
        if(flashFile.trim().equals("")){
            throw new FileNotFoundException("No flash storage file exists or is empty.");
        }

        JSONArray jsonArray = new JSONArray(flashFile);
        for (int i = 0; i < jsonArray.length(); i++) {
            latestPushedFlashes.add(Flash.deserialize((JSONObject) jsonArray.get(i)));
        }
        return latestPushedFlashes;
    }

    public void writeFlashesToStorage(List<Flash> flashes) throws JSONException, IOException {
        FileOutputStream outputStream = context.openFileOutput(flashFileLocation, Context.MODE_PRIVATE);
        String out = convertFlashesToOutputString(flashes);
        outputStream.write(out.getBytes());
        outputStream.close();
    }

    public void sortPushedFlashes(){
        // todo
    }

    private String convertFlashesToOutputString(List<Flash> flashes) throws JSONException {
        Flash[] sorted = flashes.toArray(new Flash[0]);
        Arrays.sort(sorted, new Comparator<Flash>() {
            public int compare(Flash a, Flash b){
                return a.getDate().compareTo(b.getDate());
            }
        });

        int min = 0;
        int max = sorted.length - 1;

        if (max > flashesToStoreInDatabase) {
            min = max - flashesToStoreInDatabase;
        }
        JSONArray output = new JSONArray();
        if(min < 0 || max >= sorted.length){
            min = 0;
            max = sorted.length - 1;
        }
        if(max < 0){
            max = 0;
        }
        for (int i = min; i <= max; i++) {
            output.put(sorted[i].serialize());
        }
        return output.toString(4);
    }

    public ArrayList<Flash> getLatestPushedFlashes() {
        try {
            return loadFlashesFromStorage();
        }catch(Exception e){
            DebugManager.sendDebugNotification("Unable to load flashes from storage: " + e.getLocalizedMessage(), context);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void clearPushedFlashes() throws JSONException, IOException{
        writeFlashesToStorage(new ArrayList<Flash>());
    }

    public String getServerName() {
        return serverName;
    }

    public void pushFlashNotification(Flash flash) throws JSONException, IOException {
        if (!prefs.getBoolean("notifications_enabled", true)) {
            return;
        }
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(flash.getLink()));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        String text = StringEscapeUtils.unescapeHtml4(flash.getText());
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Notification.Builder oBuilder = new Notification.Builder(context, getNotificationChannel(context).getId());
            oBuilder.setContentTitle(flash.getChannel() + " • " + flash.getSource())
                    .setStyle(new Notification.BigTextStyle()
                            .bigText(text))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setSound(Uri.parse(prefs.getString("notification_sound", "DEFAULT")))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_alert);
            mNotificationManager.notify(flash.getIdAsInteger(), oBuilder.build());
        }else{
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setContentTitle(flash.getChannel() + " • " + flash.getSource())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(text))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setSound(Uri.parse(prefs.getString("notification_sound", "DEFAULT")))
                    .setContentIntent(pendingIntent);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBuilder.setSmallIcon(R.drawable.ic_alert);
            } else {
                mBuilder.setSmallIcon(R.drawable.ic_alert_compat);
            }
            mNotificationManager.notify(flash.getIdAsInteger(), mBuilder.build());
        }
    }

    public void refresh() {
        String newServerUrl = prefs.getString("server_url", "https://librenews.io/api");
        Set<String> defaults = new HashSet<>();
        defaults.add("Breaking News");
        defaults.add("Announcements"); // mwahaha
        final Set<String> channels = prefs.getStringSet("channels", defaults);
        ArrayList<Flash> latestPushed = getLatestPushedFlashes();
        final boolean newInstallation = latestPushed.size() == 0;
        if (!newServerUrl.equals(serverUrl)) {
            // they changed their server preferences!
            try {
                clearPushedFlashes();
            } catch (IOException exception) {
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.internal_storage_setup_fail), Toast.LENGTH_LONG);
                exception.printStackTrace();
            } catch (JSONException exception) {
                Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.internal_storage_setup_fail), Toast.LENGTH_LONG);
                exception.printStackTrace();
            }
            serverUrl = newServerUrl;
        }
        long latest = -1;
        for(Flash f : getLatestPushedFlashes()){
            if(Long.valueOf(f.getId()) > latest){
                latest = Long.valueOf(f.getId());
            }
        }
        try {
            FlashRetreiver retreiver = new FlashRetreiver(new URL(serverUrl + "?latest=" + latest));
            retreiver.retrieveFlashes(new FlashRetreiver.FlashHandler() {
                @Override
                public void success(Flash[] flashes, String serverName) {
                    lastContactSuccessful = ConnectionStatus.VALID;
                    for (Flash f : flashes){
                        if(!channels.contains(f.getChannel())){
                            continue;
                        }
                        boolean pushed = false;
                        for (Flash p : getLatestPushedFlashes()) {
                            if (p.getId().equals(f.getId())) {
                                pushed = true;
                            }
                        }
                        try {
                            if (!pushed) {
                                if(!newInstallation) {
                                    pushFlashNotification(f);
                                }
                                ArrayList<Flash> q = getLatestPushedFlashes();
                                q.add(f);
                                writeFlashesToStorage(q); // lots of IO, but it's OK
                            }
                        }catch(Exception exception){
                            exception.printStackTrace();
                            DebugManager.sendDebugNotification("Error occurred while trying push notifications: " + exception.getLocalizedMessage(), context);
                        }
                    }
                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(MainFlashActivity.activeInstance != null){
                                onDone();
                                MainFlashActivity.activeInstance.regenerateToolbarStatus();
                            }
                        }
                    };
                    mainHandler.post(myRunnable);

                }

                @Override
                public void failure(Exception exception) {
                    lastContactSuccessful = ConnectionStatus.INVALID;
                    exception.printStackTrace();
                    DebugManager.sendDebugNotification("An error occurred while trying to receive flashes: " + exception.getLocalizedMessage(), context);
                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if(MainFlashActivity.activeInstance != null){
                                MainFlashActivity.activeInstance.regenerateToolbarStatus();
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }, context);
        } catch (MalformedURLException exception) {
            Toast.makeText(context.getApplicationContext(), context.getResources().getString(R.string.invalid_server_url), Toast.LENGTH_LONG);
            exception.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                if(MainFlashActivity.activeInstance != null){
                    MainFlashActivity.activeInstance.regenerateToolbarStatus();
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    public interface DoneCallback{
        void onDone();
    }

    ArrayList<DoneCallback> callbacks = new ArrayList<DoneCallback>();
    public void addDoneCallback(DoneCallback d) {
        this.callbacks.add(d);
    }

    public void onDone(){
        for (DoneCallback d : callbacks) {
            d.onDone();
        }
    }
}
