package com.example.robin.quiethours.Database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey(autoGenerate = true) var profileId: Long = 0L, var name: String,
    var shr: Int,
    var smin: Int,
    var ehr: Int,
    var emin: Int,
    var d: String
) :
    Parcelable
