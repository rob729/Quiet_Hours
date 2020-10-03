package com.rob729.quiethours.Activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.rob729.quiethours.R
import com.rob729.quiethours.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        supportActionBar?.elevation = 0F
        val appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (appSharedPrefs.getBoolean("nightMode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}
