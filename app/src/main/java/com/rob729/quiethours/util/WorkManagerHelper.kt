package com.rob729.quiethours.util

import android.content.Context
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rob729.quiethours.Database.Profile
import java.util.*
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    fun init(context: Context) {
        val configuration = Configuration.Builder()
            .build()
        WorkManager.initialize(context, configuration)
    }
        fun setAlarms(profile: Profile, startHour: Int = profile.shr, startMinute: Int = profile.smin) {
        val type by lazy { object : TypeToken<List<Boolean>>() {}.type }
        val selectedDays by lazy { Gson() }
        val days: MutableList<Boolean> = selectedDays.fromJson(profile.d, type)
        var i = 0
        while (i < 7) {
            if (days[i]) {
                setStartAlarm(i + 1, profile, startHour, startMinute)
                if (startHour > profile.ehr) {
                    if (i == 6) {
                        setEndAlarm(1, profile)
                    } else {
                        setEndAlarm(i + 2, profile)
                    }
                } else {
                    setEndAlarm(i + 1, profile)
                }
            }
            ++i
        }
    }

    fun setStartAlarm(dayOfWeek: Int, profile: Profile, startHour: Int, startMinute: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, startHour)
        c.set(Calendar.MINUTE, startMinute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == profile.profileId && StoreSession.readInt(AppConstants.BEGIN_STATUS) > 0) {
            StoreSession.writeInt(AppConstants.BEGIN_STATUS, StoreSession.readInt(AppConstants.BEGIN_STATUS) - 1)
        }

        if (c.timeInMillis < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 7)
        }

        var etime = "End Time: ${Utils.setTimeString(profile.ehr)}:${Utils.setTimeString(profile.emin)}"
        val profileData = workDataOf(
            Pair("ActiveProfileId", profile.profileId),
            Pair("Profile_Name", profile.name),
            (Pair("VibrateKey", profile.vibSwitch)),
            (Pair("EndTimeKey", etime))
        )

        val startAlarmRequest = if (profile.repeatWeekly) {
            PeriodicWorkRequest.Builder(StartAlarm::class.java, 7, TimeUnit.DAYS)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()
        } else {
            OneTimeWorkRequest.Builder(StartAlarm::class.java)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()
        }
        WorkManager.getInstance().enqueue(startAlarmRequest)
    }

    fun setEndAlarm(dayOfWeek: Int, profile: Profile) {

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, profile.ehr)
        c.set(Calendar.MINUTE, profile.emin)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        timeCheck(c)
        val profileData = workDataOf(Pair("Profile_Name", profile.name))
        val endAlarmRequest = if (profile.repeatWeekly) {
            PeriodicWorkRequest.Builder(EndAlarm::class.java, 7, TimeUnit.DAYS)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()
        } else {
            OneTimeWorkRequest.Builder(EndAlarm::class.java)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build()
        }
        WorkManager.getInstance().enqueue(endAlarmRequest)
    }
    fun cancelWork(tag: String) {
        WorkManager.getInstance().cancelAllWorkByTag(tag)
    }

    fun timeCheck(c: Calendar) {
        if (c.timeInMillis < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 7)
        }
    }
}