package com.example.robin.quiethours

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.robin.quiethours.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        supportActionBar?.elevation = 0F
        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
        NavigationUI.setupActionBarWithNavController(this, navController!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = myNavHostFragment.view?.let { Navigation.findNavController(it) }
        return navController!!.navigateUp() || super.onSupportNavigateUp()
    }
}
