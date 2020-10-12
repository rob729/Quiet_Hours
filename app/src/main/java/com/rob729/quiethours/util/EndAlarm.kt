package com.rob729.quiethours.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rob729.quiethours.Activity.SplashScreen
import com.rob729.quiethours.R

class EndAlarm(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val b = "422"

    override fun doWork(): Result {

        val profileName = inputData.getString("Profile_Name")
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val audioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        StoreSession.writeInt(AppConstants.BEGIN_STATUS, StoreSession.readInt(AppConstants.BEGIN_STATUS) - 1)

        val intent = Intent(applicationContext, SplashScreen::class.java)
        val pi = PendingIntent.getActivity(
            applicationContext,
            333,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(applicationContext, b)
            .setSmallIcon(R.drawable.ic_notifications_off)
            .setColor(Color.rgb(30, 136, 229))
            .setContentTitle("Profile Ended")
            .setContentText("$profileName profile has ended")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        notificationManager.notify(1112, notification)

        return Result.success()
    }
}
