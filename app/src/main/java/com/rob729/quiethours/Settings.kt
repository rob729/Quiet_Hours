package com.rob729.quiethours

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.rob729.quiethours.Activity.MainActivity

class Settings : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    var pref: SwitchPreference? = null
    lateinit var rate: Preference
    lateinit var share: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_quiet_hours)
        pref = findPreference("nightMode")
        rate = findPreference("rate")!!
        share = findPreference("share")!!

        rate.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context?.packageName)))
            return@setOnPreferenceClickListener true
        }

        share.setOnPreferenceClickListener {
            try {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                    String.format(getString(R.string.promo_msg_template),
                        String.format(getString(R.string.app_share_url), activity?.packageName)))
                startActivity(shareIntent)
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.error_msg_retry), Toast.LENGTH_SHORT).show()
            }
            true
        }

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