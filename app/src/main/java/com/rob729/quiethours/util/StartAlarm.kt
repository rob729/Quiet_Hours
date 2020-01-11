package com.rob729.quiethours.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rob729.quiethours.Activity.SplashScreen
import com.rob729.quiethours.R

class StartAlarm(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val b = "422"
    var vibrate: Boolean = false

    override fun doWork(): Result {

        val appSharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
        if (appSharedPrefs.contains("vibrate")) {
            vibrate = appSharedPrefs.getBoolean("vibrate", false)
        }

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(b, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val profileName = inputData.getString("Profile_Name")

        val audioManager =
            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

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
            .setContentTitle("Profile Active")
            .setContentText("$profileName profile has started")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        notificationManager.notify(1112, notification)

        if (vibrate) {
            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
        } else {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        }

        return Result.success()
    }
}