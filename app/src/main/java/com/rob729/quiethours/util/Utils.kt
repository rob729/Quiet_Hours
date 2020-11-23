package com.rob729.quiethours.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.view.View
import androidx.core.app.NotificationCompat
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rob729.quiethours.R

object Utils {
    lateinit var audioManager: AudioManager
    lateinit var notificationManager: NotificationManager

    fun init(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun selectedDays(d: List<Boolean>, materialDayPicker: MaterialDayPicker) {
        if (d[0])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SUNDAY)
        if (d[1])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.MONDAY)
        if (d[2])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.TUESDAY)
        if (d[3])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.WEDNESDAY)
        if (d[4])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.THURSDAY)
        if (d[5])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.FRIDAY)
        if (d[6])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SATURDAY)
    }

    fun setTimeString(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            "$i"
        }
    }

    fun daysList(profileDays: String): MutableList<Boolean> {
        val type by lazy { object : TypeToken<List<Boolean>>() {}.type }
        val selectedDays by lazy { Gson() }
        return selectedDays.fromJson(profileDays, type)
    }

    fun showSnackBar(it: View, message: String, length: Int = Snackbar.LENGTH_SHORT) {
        Snackbar
            .make(
                it,
                message,
                length
            )
            .show()
    }

    fun setNotification(
        applicationContext: Context,
        profileName: String,
        state: String,
        pi: PendingIntent
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, AppConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_off)
            .setColor(Color.rgb(30, 136, 229))
            .setContentTitle("Profile $state")
            .setContentText("$profileName profile has $state")
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()
        notificationManager.notify(1112, notification)
    }
}