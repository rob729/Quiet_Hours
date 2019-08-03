package com.rob729.quiethours

import android.content.Context
import android.media.AudioManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class EndAlarm(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        val audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL

        return Result.success()
    }

}
