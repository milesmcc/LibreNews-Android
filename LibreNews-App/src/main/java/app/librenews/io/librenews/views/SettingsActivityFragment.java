package app.librenews.io.librenews.views;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.controllers.FlashManager;
import app.librenews.io.librenews.controllers.SyncManager;

public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        System.out.println(key + " changed!");
        if(key.equals("refresh_preference") || key.equals("automatically_refresh")){
            new SyncManager(getView().getContext(), new FlashManager(getView().getContext())).startSyncService();
        }
        if (getActivity() instanceof MainFlashActivity) {
            ((MainFlashActivity)getActivity()).regenerateToolbarStatus();
        }
    }
}