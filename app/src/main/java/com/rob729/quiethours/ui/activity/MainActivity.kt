package com.rob729.quiethours.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.Navigation
import com.rob729.quiethours.databinding.ActivityMainBinding
import com.rob729.quiethours.util.StoreSession
import dagger.hilt.android.AndroidEntryPoint
import kotlin.or
import kotlin.text.compareTo
import androidx.navigation.findNavController

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (StoreSession.getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Enable edge-to-edge using WindowCompat (modern approach)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = !StoreSession.getNightMode()

        // Apply window insets to the nav host fragment
        ViewCompat.setOnApplyWindowInsetsListener(binding.myNavHostFragment) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }

        supportActionBar?.elevation = 0F
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = binding.myNavHostFragment.rootView.findNavController()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
