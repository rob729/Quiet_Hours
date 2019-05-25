package com.example.robin.quiethours.Activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.example.robin.quiethours.R
import com.example.robin.quiethours.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,
            R.layout.activity_main
        )
        supportActionBar?.elevation = 0F

        val appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if(appSharedPrefs.getBoolean("nightMode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
        NavigationUI.setupActionBarWithNavController(this, navController!!)


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}
