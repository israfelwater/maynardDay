package com.example.betarun.settings;

import com.example.betarun.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsActivity extends Activity {

	private SettingsFragment mSettingsFragment;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_settings);
		
		// Display the fragment as the main content.
		mSettingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsFragmentView, mSettingsFragment)
                .commit();	
        //mSettingsFragment.addPreferencesFromResource(R.xml.settings);
        this.setTitle("Settings");
	}
}
