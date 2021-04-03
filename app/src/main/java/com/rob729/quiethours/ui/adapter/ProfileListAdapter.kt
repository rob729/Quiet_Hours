package com.rob729.quiethours.ui.adapter

import android.media.AudioManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.databinding.ItemRowBinding
import com.rob729.quiethours.util.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileListAdapter(
    private val adapterCallback: AdapterCallback,
    private val audioManager: AudioManager
) : ListAdapter<Profile, ProfileListAdapter.ViewHolder>(
    ProfileDiffCallbacks()
) {

    var profiles = ArrayList<Profile>()
    private val current: Calendar = Calendar.getInstance()
    private val currentHour = current.get(Calendar.HOUR_OF_DAY)
    private val currentMinute = current.get(Calendar.MINUTE)
    private val currentDay = current.get(Calendar.DAY_OF_WEEK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, adapterCallback)
    }

    inner class ViewHolder(private val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Profile, adapterCallback: AdapterCallback) {
            binding.ProfileName.text = item.name
            binding.TxtImg.setText(item.name[0].toString())
            binding.TxtImg.avatarBackgroundColor = AppConstants.bgColors[item.colorIndex]
            binding.timeStamp.text = item.timeInstance
            binding.pauseSwitch.isChecked = item.pauseSwitch

            val days: MutableList<Boolean> = Utils.daysList(item.d)

            binding.profileCard.setOnClickListener {
                adapterCallback.openProfileDetails(item)
            }

            if (item.ehr == currentHour && item.emin == currentMinute && StoreSession.readLong(
                    AppConstants.ACTIVE_PROFILE_ID
                ) == item.profileId
            ) {
                StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
                StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, 0)
            }
            if (days[currentDay - 1] && item.shr == currentHour && item.smin == currentMinute
            ) {
                StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, item.profileId)
            }

            binding.pauseSwitch.setOnClickListener {
                item.pauseSwitch = false
                adapterCallback.cancelWorkByTag(item.profileId.toString())
                if (!binding.pauseSwitch.isChecked) {
                    Utils.showSnackBar(binding.card, "${item.name} is Paused")
                    if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId)
                        audioManager.ringerMode =
                            StoreSession.readInt(AppConstants.RINGTONE_MODE)
                } else {
                    Utils.showSnackBar(binding.card, "${item.name} is Resumed")
                    if ((StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId) &&
                        ((item.shr == item.ehr && (currentHour == item.ehr && currentMinute > item.emin) || (currentHour > item.ehr)) ||
                                (item.shr < item.ehr && (currentHour >= item.ehr)) || (item.shr > item.ehr && (currentHour <= item.ehr)))
                    ) {
                        StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
                        StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, 0)
                    } else {
                        setNewAlarms(item)
                    }
                }
                adapterCallback.updateItem(item)
            }
        }
    }

    private fun setNewAlarms(item: Profile) {
        val smin: Int
        val shr: Int
        item.pauseSwitch = true
        if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId) {
            if (currentMinute == 59) {
                smin = 0
                shr = if (currentHour == 23)
                    0
                else
                    currentHour + 1
            } else {
                smin = currentMinute + 1
                shr = currentHour
            }
            if (shr != item.ehr && smin != item.emin)
                adapterCallback.setAlarms(item, shr, smin)
        } else {
            adapterCallback.setAlarms(item)
        }
    }

    class ProfileDiffCallbacks : DiffUtil.ItemCallback<Profile>() {
        override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
            return oldItem.profileId == newItem.profileId
        }

        override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
            return oldItem == newItem
        }
    }
}