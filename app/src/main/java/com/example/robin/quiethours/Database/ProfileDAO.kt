package com.example.robin.quiethours.Database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProfileDAO {
    @Query("SELECT * FROM profile_table")
    fun getAllProfile(): LiveData<List<Profile>>

    @Insert
    fun insertProfile(vararg profiles: Profile)

    @Delete
    fun deleteProfile(vararg profiles: Profile)
}