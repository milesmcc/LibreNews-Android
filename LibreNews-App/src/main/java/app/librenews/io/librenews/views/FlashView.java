package app.librenews.io.librenews.views;

import android.content.Intent;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.controllers.FlashManager;
import app.librenews.io.librenews.controllers.SyncManager;

public class FlashView extends AppCompatActivity {
    FlashManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_view);
        manager = new FlashManager(this);
        findViewById(R.id.settings_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(FlashView.this, SettingsActivity.class);
                        startActivity(intent);
                    }
                }
        );
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FlashManager(getApplicationContext()).refresh();
                srl.setRefreshing(false);
            }
        });
    }
}
