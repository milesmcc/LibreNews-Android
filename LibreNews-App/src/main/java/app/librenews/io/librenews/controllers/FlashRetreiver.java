package app.librenews.io.librenews.controllers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import app.librenews.io.librenews.models.Flash;

import static app.librenews.io.librenews.controllers.DebugManager.sendDebugNotification;

public class FlashRetreiver {
    interface FlashHandler {
        void success(Flash[] flashes, String serverName);

        void failure(Exception exception);
    }

    URL serverLocation;

    public FlashRetreiver(URL serverLocation) {
        this.serverLocation = serverLocation;
    }


    private Flash[] convertJsonToFlashes(JSONArray jsonFlashes) throws JSONException, ParseException {
        ArrayList<Flash> flashes = new ArrayList<Flash>();
        for (int i = 0; i < jsonFlashes.length(); i++) {
            JSONObject jsonFlash = (JSONObject) jsonFlashes.get(i);
            Flash flash = Flash.deserialize(jsonFlash);
            flashes.add(flash);
        }
        return flashes.toArray(new Flash[0]);
    }

    Flash[] flashes = null;
    String serverName = null;

    private boolean retreiveFlashesNonAsync(Context context) throws IOException, JSONException, ParseException{
        if (!serverLocation.getProtocol().equals("https")) {
            throw new SecurityException("Flashes may only be retrieved over a secure HTTPS connection!");
        }
        sendDebugNotification("Retrieving flashes from " + serverLocation, context);
        HttpsURLConnection urlConnection = (HttpsURLConnection) serverLocation.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(150000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.connect();
        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            String data = sb.toString();
            JSONObject jsonData = new JSONObject(data);
            serverName = jsonData.getString("server");
            flashes = convertJsonToFlashes(jsonData.getJSONArray("latest"));
        }
        sendDebugNotification("Flashes found: " + flashes.length, context);
        return true;
    }

    public void retrieveFlashes(final FlashHandler handler, final Context context) {
        final URL serverLocation = this.serverLocation;
        class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    return retreiveFlashesNonAsync(context);
                }catch(Exception e){
                    handler.failure(e);
                    e.printStackTrace();
                }
                return false;
            }

            protected void onPostExecute(Boolean result) {
                if (!result || flashes == null) {
                    handler.failure(new Exception("An error occurred while trying to receive flashes!"));
                    DebugManager.sendDebugNotification("An error occurred while trying to receive flashes!", context);
                    return;
                }
                handler.success(flashes, serverName);
            }
        }
        DebugManager.sendDebugNotification("Performing an asynchronous flash retrieval...", context);
        new JSONAsyncTask().execute();
    }
}
