package com.rob729.quiethours.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rob729.quiethours.Activity.SplashScreen

class EndAlarm(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val profileName = inputData.getString("Profile_Name")
        if (StoreSession.readInt(AppConstants.BEGIN_STATUS) > 0)
            StoreSession.writeInt(
                AppConstants.BEGIN_STATUS,
                StoreSession.readInt(AppConstants.BEGIN_STATUS) - 1
            )
        Utils.audioManager.ringerMode =
            if (StoreSession.readInt(AppConstants.BEGIN_STATUS) > 0) {
                if (StoreSession.readInt(AppConstants.VIBRATE_STATE_ICON) == 1)
                    AudioManager.RINGER_MODE_VIBRATE
                else
                    AudioManager.RINGER_MODE_SILENT
            } else AudioManager.RINGER_MODE_NORMAL
        val intent = Intent(applicationContext, SplashScreen::class.java)
        val pi = PendingIntent.getActivity(
            applicationContext,
            333,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Utils.setNotification(applicationContext, profileName!!, "ended", pi)

        return Result.success()
    }
}
