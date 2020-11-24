package com.rob729.quiethours.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProfileDAO {
    @Query("SELECT * FROM profile_table")
    fun getAllProfile(): LiveData<List<Profile>>

    @Insert
    fun insertProfile(vararg profiles: Profile)

    @Delete
    fun deleteProfile(vararg profiles: Profile)

    @Update
    fun updateProfile(vararg profiles: Profile)
}