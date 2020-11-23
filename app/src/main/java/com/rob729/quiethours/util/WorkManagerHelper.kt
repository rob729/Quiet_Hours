package com.rob729.quiethours.util

import android.content.Context
import androidx.work.*
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
        val days: MutableList<Boolean> by lazy { Utils.daysList(profile.d) }
        for (i in 0..6) {
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
        }
    }

    fun setStartAlarm(dayOfWeek: Int, profile: Profile, startHour: Int, startMinute: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, startHour)
        c.set(Calendar.MINUTE, startMinute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == profile.profileId && StoreSession.readInt(
                AppConstants.BEGIN_STATUS
            ) > 0
        ) {
            StoreSession.writeInt(
                AppConstants.BEGIN_STATUS,
                StoreSession.readInt(AppConstants.BEGIN_STATUS) - 1
            )
        }
        timeCheck(c)
        var etime =
            "End Time: ${Utils.setTimeString(profile.ehr)}:${Utils.setTimeString(profile.emin)}"
        val profileData = workDataOf(
            Pair("ActiveProfileId", profile.profileId),
            Pair("Profile_Name", profile.name),
            (Pair("VibrateKey", profile.vibSwitch)),
            (Pair("EndTimeKey", etime))
        )
        setAlarmRequest(profile, profileData, c, StartAlarm::class.java)
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
        setAlarmRequest(profile, profileData, c, EndAlarm::class.java)
    }

    fun setAlarmRequest(
        profile: Profile,
        profileData: Data,
        c: Calendar,
        alarmClass: Class<out ListenableWorker>
    ) {
        val alarmRequest = if (profile.repeatWeekly) {
            PeriodicWorkRequest.Builder(alarmClass, 7, TimeUnit.DAYS)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(
                    c.timeInMillis - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()
        } else {
            OneTimeWorkRequest.Builder(alarmClass)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(
                    c.timeInMillis - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
        WorkManager.getInstance().enqueue(alarmRequest)
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