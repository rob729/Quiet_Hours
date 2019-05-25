package com.example.robin.quiethours.Database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "profile_table")
data class Profile(@PrimaryKey(autoGenerate = true) var profileId: Long = 0L, var name: String, var shr: Int, var smin: Int, var ehr: Int, var emin: Int, var d: String) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(profileId)
        parcel.writeString(name)
        parcel.writeInt(shr)
        parcel.writeInt(smin)
        parcel.writeInt(ehr)
        parcel.writeInt(emin)
        parcel.writeString(d)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }
}
