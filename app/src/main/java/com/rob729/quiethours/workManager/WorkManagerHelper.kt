package com.rob729.quiethours.workManager

import androidx.work.*
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.util.*
import com.rob729.quiethours.util.Utils.timeCheck
import java.util.*
import java.util.concurrent.TimeUnit

object WorkManagerHelper {

    private val calender: Calendar by lazy {
        Calendar.getInstance()
    }

    fun setAlarms(
        workManager: WorkManager,
        profile: Profile,
        startHour: Int,
        startMinute: Int
    ) {
        val days: MutableList<Boolean> by lazy { Utils.daysList(profile.d) }
        for (i in 0..6) {
            if (days[i]) {
                setStartAlarm(workManager, i + 1, profile, startHour, startMinute)
                if (startHour > profile.ehr) {
                    if (i == 6) {
                        setEndAlarm(workManager, 1, profile)
                    } else {
                        setEndAlarm(workManager, i + 2, profile)
                    }
                } else {
                    setEndAlarm(workManager, i + 1, profile)
                }
            }
        }
    }

    private fun setStartAlarm(
        workManager: WorkManager,
        dayOfWeek: Int,
        profile: Profile,
        startHour: Int,
        startMinute: Int
    ) {
        calender.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calender.set(Calendar.HOUR_OF_DAY, startHour)
        calender.set(Calendar.MINUTE, startMinute)
        calender.set(Calendar.SECOND, 0)
        calender.set(Calendar.MILLISECOND, 0)
        if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == profile.profileId && StoreSession.readInt(
                AppConstants.BEGIN_STATUS
            ) > 0
        ) {
            StoreSession.writeInt(
                AppConstants.BEGIN_STATUS,
                StoreSession.readInt(AppConstants.BEGIN_STATUS) - 1
            )
        }
        timeCheck(calender)
        val etime =
            "End Time: ${Utils.setTimeString(profile.ehr)}:${Utils.setTimeString(profile.emin)}"
        val profileData = workDataOf(
            Pair("ActiveProfileId", profile.profileId),
            Pair("Profile_Name", profile.name),
            (Pair("VibrateKey", profile.vibSwitch)),
            (Pair("EndTimeKey", etime))
        )
        setAlarmRequest<StartAlarm>(workManager, profile, profileData, calender, StartAlarm::class.java)
    }

    private fun setEndAlarm(workManager: WorkManager, dayOfWeek: Int, profile: Profile) {
        calender.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calender.set(Calendar.HOUR_OF_DAY, profile.ehr)
        calender.set(Calendar.MINUTE, profile.emin)
        calender.set(Calendar.SECOND, 0)
        calender.set(Calendar.MILLISECOND, 0)

        timeCheck(calender)
        val profileData = workDataOf(Pair("Profile_Name", profile.name))
        setAlarmRequest<EndAlarm>(workManager, profile, profileData, calender, EndAlarm::class.java)
    }

    private inline fun <reified T : ListenableWorker> setAlarmRequest(
        workManager: WorkManager,
        profile: Profile,
        profileData: Data,
        c: Calendar,
        alarmClass: Class<out ListenableWorker>
    ) {
        val alarmRequest = if (profile.repeatWeekly) {
            PeriodicWorkRequestBuilder<T>(7, TimeUnit.DAYS)
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(
                    c.timeInMillis - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()
//            PeriodicWorkRequest.Builder(alarmClass, 7, TimeUnit.DAYS)
//                .addTag(profile.profileId.toString())
//                .setInputData(profileData)
//                .setInitialDelay(
//                    c.timeInMillis - System.currentTimeMillis(),
//                    TimeUnit.MILLISECONDS
//                )
//                .build()
        } else {
            OneTimeWorkRequestBuilder<T>()
                .addTag(profile.profileId.toString())
                .setInputData(profileData)
                .setInitialDelay(
                    c.timeInMillis - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()
//            OneTimeWorkRequest.Builder(alarmClass)
//                .addTag(profile.profileId.toString())
//                .setInputData(profileData)
//                .setInitialDelay(
//                    c.timeInMillis - System.currentTimeMillis(),
//                    TimeUnit.MILLISECONDS
//                )
//                .build()
        }
        workManager.enqueue(alarmRequest)
    }

    fun cancelWork(workManager: WorkManager, tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }
}