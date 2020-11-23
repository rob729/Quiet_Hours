package com.rob729.quiethours

import android.app.Application
import com.rob729.quiethours.util.StoreSession
import com.rob729.quiethours.util.Utils
import com.rob729.quiethours.util.WorkManagerHelper

class QuietHoursApp : Application() {
    override fun onCreate() {
        super.onCreate()
        StoreSession.init(this)
        WorkManagerHelper.init(this)
        Utils.init(this)
    }
}