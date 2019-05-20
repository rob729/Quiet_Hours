package com.example.robin.quiethours.Database

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileDAO

class ProfileRepository(private val profileDAO: ProfileDAO){

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