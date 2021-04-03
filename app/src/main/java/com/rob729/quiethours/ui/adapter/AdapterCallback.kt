package com.rob729.quiethours.ui.adapter

import com.rob729.quiethours.database.Profile

interface AdapterCallback {
    fun updateItem(profile: Profile)
    fun openProfileDetails(profile: Profile)
    fun setAlarms(profile: Profile, startHour: Int = profile.shr, startMinute: Int = profile.smin)
    fun cancelWorkByTag(tag: String)
}