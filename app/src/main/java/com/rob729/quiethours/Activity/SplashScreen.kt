package com.rob729.quiethours.Activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rob729.quiethours.R
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.StoreSession

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        checkFirstRun()
        finish()
    }
    fun checkFirstRun() {
        if (!StoreSession.readBoolean(AppConstants.FIRST_BOOT)) {
            StoreSession.setNightMode(resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
        }
        StoreSession.writeBoolean(AppConstants.FIRST_BOOT, true)
    }
}
