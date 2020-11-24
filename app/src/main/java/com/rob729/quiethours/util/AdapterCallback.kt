package com.rob729.quiethours.util

import com.rob729.quiethours.database.Profile

interface AdapterCallback {
    fun updateItem(profile: Profile)
    fun openProfileDetails(profile: Profile)
}