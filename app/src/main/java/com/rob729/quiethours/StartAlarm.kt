package com.rob729.quiethours

import android.app.NotificationChannel
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

class StartAlarm(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    private val b = "422"

    override fun doWork(): Result {

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(b, "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val profileName = inputData.getString("Profile_Name")

        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE

        val intent = Intent(applicationContext, SplashScreen::class.java)
        val pi = PendingIntent.getActivity(applicationContext, 333, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, b)
            .setSmallIcon(R.drawable.ic_notifications_off)
            .setColor(Color.rgb(30, 136, 229))
            .setContentTitle("Profile Active")
            .setContentText("$profileName profile has started")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        notificationManager.notify(1112, notification)

        return Result.success()
    }
}