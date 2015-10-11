package com.example.utente.simpleledtorch;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.support.v7.app.ActionBar;

import com.example.utente.simpleledtorch.R;

/**
 * Created by utente on 04/10/2015.
 */
public class MySettingsActivity extends AppCompatPreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.application_preference);
        bindPreferenceSummaryToValue(findPreference(getApplicationContext().getString(R.string.pref_number_shakes)));
        bindPreferenceSummaryToValue(findPreference(getApplicationContext().getString(R.string.pref_shake_window)));
        bindPreferenceSummaryToValue(findPreference(getApplicationContext().getString(R.string.pref_shake_debounce)));

        setupActionBar();
    }


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            }  else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Override per gestire correttamente il back (pulsante up in alto a sx) alla prima finestra
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

        private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("***MySettingsActivity", "onPause");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("***MySettingsActivity", "onResume");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d("***MySettingsActivity", "onDestroy");
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.d("***MySettingsActivity", "onStop");
    }


}
