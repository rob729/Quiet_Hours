package com.rob729.quiethours

import android.app.Application
import com.rob729.quiethours.util.StoreSession
class QuietHoursApp : Application() {
    override fun onCreate() {
            super.onCreate()
            StoreSession.init(this)
    }
}