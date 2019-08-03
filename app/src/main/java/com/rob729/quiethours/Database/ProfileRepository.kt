package com.rob729.quiethours.Database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class ProfileRepository(private val profileDAO: ProfileDAO) {

    val allProfiles: LiveData<List<Profile>> = profileDAO.getAllProfile()

    @WorkerThread
    fun insert(profile: Profile) {
        profileDAO.insertProfile(profile)
    }

    @WorkerThread
    fun delete(profile: Profile) {
        profileDAO.deleteProfile(profile)
    }

}