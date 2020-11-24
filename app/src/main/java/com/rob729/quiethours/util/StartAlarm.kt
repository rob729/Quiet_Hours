package com.rob729.quiethours.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rob729.quiethours.activity.SplashScreen

class StartAlarm(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    AppConstants.CHANNEL_ID,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            Utils.notificationManager.createNotificationChannel(notificationChannel)
        }
        val profileName = "Currently Active Profile: ${inputData.getString("Profile_Name")}"
        val vibrate = inputData.getBoolean("VibrateKey", true)
        val profileEndTime = inputData.getString("EndTimeKey")
        val activeProfileId = inputData.getLong("ActiveProfileId", 0)

        val intent = Intent(applicationContext, SplashScreen::class.java)
        val pi = PendingIntent.getActivity(
            applicationContext,
            333,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (StoreSession.readInt(AppConstants.BEGIN_STATUS) == 0)
            StoreSession.writeInt(
                AppConstants.RINGTONE_MODE,
                Utils.audioManager.ringerMode
            )
        Utils.sendNotification(applicationContext, profileName, "started", pi)
        StoreSession.writeInt(
            AppConstants.BEGIN_STATUS,
            StoreSession.readInt(AppConstants.BEGIN_STATUS) + 1
        )
        StoreSession.writeString(AppConstants.ACTIVE_PROFILE_NAME, profileName)
        StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, activeProfileId)

        if (vibrate) {
            StoreSession.writeInt(AppConstants.VIBRATE_STATE_ICON, 1)
        } else {
            StoreSession.writeInt(AppConstants.VIBRATE_STATE_ICON, 0)
        }
        StoreSession.writeString(AppConstants.END_TIME, profileEndTime!!)
        Utils.audioManager.ringerMode = if (vibrate) {
            AudioManager.RINGER_MODE_VIBRATE
        } else {
            AudioManager.RINGER_MODE_SILENT
        }

        return Result.success()
    }
}