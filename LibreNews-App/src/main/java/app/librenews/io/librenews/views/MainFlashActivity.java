package app.librenews.io.librenews.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.controllers.FlashManager;
import app.librenews.io.librenews.controllers.SyncManager;

public class MainFlashActivity extends AppCompatActivity {
    public static MainFlashActivity activeInstance;
    public static boolean forceDisableWelcomeScreen = false;

    FlashManager manager;

    public void regenerateToolbarStatus(){
        TextView connStatus = (TextView) findViewById(R.id.server_status);
        TextView statusTextView = (TextView) findViewById(R.id.status_text);
        connStatus.setTextColor(FlashManager.lastContactSuccessful.color());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean syncing = prefs.getBoolean("automatically_refresh", true);
        boolean notifications = prefs.getBoolean("notifications_enabled", true);
        String host = prefs.getString("server_url", "https://librenews.io");
        if(!(host.toLowerCase().startsWith("https://"))){
            statusTextView.setText(getResources().getText(R.string.https_required));
            return;
        }
        if(!syncing){
            statusTextView.setText(getResources().getText(R.string.sync_disabled));
            return;
        }
        if(!notifications){
            statusTextView.setText(getResources().getText(R.string.notifications_disabled));
            return;
        }
        int index = 0;
        String refreshValue = prefs.getString("refresh_preference", "1");
        for(String s : getResources().getStringArray(R.array.refresh_values)){
            if(s.equals(refreshValue)){
                break;
            }
            index++;
        }
        String entry = getResources().getStringArray(R.array.refresh_rates)[index];
        statusTextView.setText(getResources().getText(R.string.status_text).toString().replace("{rate}", entry));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activeInstance = this;
        setContentView(R.layout.activity_flash_view);
        manager = new FlashManager(this);
        if(manager.getLatestPushedFlashes().size() == 0 && !forceDisableWelcomeScreen){
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.refresh();
            }
        });
        findViewById(R.id.app_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        regenerateToolbarStatus();
        activeInstance = this;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        activeInstance = null;
    }
}
