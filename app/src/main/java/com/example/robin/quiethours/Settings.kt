package com.example.robin.quiethours

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.robin.quiethours.Activity.MainActivity

class Settings : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    var pref: SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_quiet_hours)
        val pmanager = preferenceManager
        pref = findPreference("nightMode")

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onStop() {
        super.onStop()
        // unregister the preference change listener
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        // register the preference change listener
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
    }


}