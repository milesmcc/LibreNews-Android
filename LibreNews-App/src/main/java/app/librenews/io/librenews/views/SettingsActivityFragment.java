package app.librenews.io.librenews.views;

import android.content.SharedPreferences;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import app.librenews.io.librenews.R;
import app.librenews.io.librenews.controllers.FlashManager;
import app.librenews.io.librenews.controllers.SyncManager;

public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        MultiSelectListPreference pref = (MultiSelectListPreference) findPreference("channels");
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> channels = prefs.getStringSet("available_channels", new HashSet<String>());
        CharSequence[] achannels = channels.toArray(new CharSequence[channels.size()]);
        pref.setEntries(achannels);
        pref.setEntryValues(achannels);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        System.out.println(key + " changed!");
        if(key.equals("refresh_preference") || key.equals("automatically_refresh")){
            FlashManager fmgr = new FlashManager(getView().getContext());
            new SyncManager(getView().getContext(), fmgr).startSyncService();
        } else if (key.equals("server_url")) {
            FlashManager fmgr = new FlashManager(getView().getContext());
            fmgr.addDoneCallback(new FlashManager.DoneCallback() {
                @Override
                public void onDone() {
                    MultiSelectListPreference pref = (MultiSelectListPreference) findPreference("channels");
                    Set<String> channels = prefs.getStringSet("available_channels", new HashSet<String>());
                    CharSequence[] achannels = channels.toArray(new CharSequence[channels.size()]);
                    pref.setEntries(achannels);
                    pref.setEntryValues(achannels);
                }
            });
            fmgr.refresh();
        }
        if (getActivity() instanceof MainFlashActivity) {
            ((MainFlashActivity)getActivity()).regenerateToolbarStatus();
        }
    }
}