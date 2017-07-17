package app.librenews.io.librenews.views;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.controllers.FlashManager;
import app.librenews.io.librenews.controllers.SyncManager;

public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                        if(key.equals("refresh_preference") || key.equals("automatically_refresh")){
                            new SyncManager(getView().getContext(), new FlashManager(getView().getContext())).startSyncService();
                        }
                        if(MainFlashActivity.activeInstance != null){
                            MainFlashActivity.activeInstance.regenerateToolbarStatus();
                        }
                    }
                };

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(spChanged);
    }
}