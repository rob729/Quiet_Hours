package com.rob729.quiethours.Database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(Application()) {

    private var parentJob = Job()
    private val scope = CoroutineScope(parentJob + Dispatchers.Main)

    private val repository: ProfileRepository
    val allProfiles: LiveData<List<Profile>>

    init {
        val profileDao = ProfileRoomDatabase.getDatabase(application).profileDao()
        repository = ProfileRepository(profileDao)
        allProfiles = repository.allProfiles
    }

    fun insert(profile: Profile) = scope.launch(Dispatchers.IO) {
        repository.insert(profile)
    }

    fun delete(profile: Profile) = scope.launch(Dispatchers.IO) {
        repository.delete(profile)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}