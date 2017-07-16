package app.librenews.io.librenews.views;

import android.preference.PreferenceFragment;
import android.os.Bundle;

import app.librenews.io.librenews.R;

public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}