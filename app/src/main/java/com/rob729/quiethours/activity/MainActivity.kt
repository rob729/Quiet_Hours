package com.rob729.quiethours.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.Navigation
import com.rob729.quiethours.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import com.rob729.quiethours.util.StoreSession

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.elevation = 0F
        if (StoreSession.getNightMode()) {
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
