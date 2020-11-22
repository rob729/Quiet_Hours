package com.rob729.quiethours.Adapter

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.rob729.quiethours.Database.Profile
import com.rob729.quiethours.Database.ProfileViewModel
import com.rob729.quiethours.Fragments.DetailsFragment
import com.rob729.quiethours.databinding.ItemRowBinding
import com.rob729.quiethours.util.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileListAdapter(
    val profileViewModel: ProfileViewModel,
    val parentView: View,
    val activity: FragmentActivity?
) :
    ListAdapter<Profile, ProfileListAdapter.ViewHolder>(
        ProfileDiffCallbacks()
    ) {

    var profiles = ArrayList<Profile>()
    lateinit var firstView: View
    val audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding, audioManager)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, profileViewModel, parentView)
        if (position == 0) {
            introDetail(holder.binding.profileCard)
        }
    }

    class ViewHolder(val binding: ItemRowBinding, val audioManager: AudioManager) :
        RecyclerView.ViewHolder(binding.root) {

        private val bgColors: IntArray = intArrayOf(
            Color.rgb(244, 81, 30),
            Color.rgb(17, 94, 231),
            Color.rgb(9, 187, 69),
            Color.rgb(123, 31, 162),
            Color.rgb(191, 27, 19),
            Color.rgb(0, 121, 107),
            Color.rgb(255, 143, 0),
            Color.rgb(216, 27, 96)
        )

        fun bind(item: Profile, profileViewModel: ProfileViewModel, parentView: View) {
            binding.ProfileName.text = item.name
            binding.TxtImg.setText(item.name[0].toString())
            binding.TxtImg.avatarBackgroundColor = bgColors[item.colorIndex]
            binding.timeStamp.text = item.timeInstance
            binding.pauseSwitch.isChecked = item.pauseSwitch
            val current: Calendar = Calendar.getInstance()
            val currentHour = current.get(Calendar.HOUR_OF_DAY)
            val currentMinute = current.get(Calendar.MINUTE)
            val currentDay = current.get(Calendar.DAY_OF_WEEK)
            val days: MutableList<Boolean> = Utils.daysList(item.d)

            binding.profileCard.setOnClickListener {
                val args = Bundle()
                args.putParcelable("Profile", item)
                val dialog = DetailsFragment.newInstance(args)
                dialog.show(
                    (parentView.context as FragmentActivity).supportFragmentManager,
                    "DialogFragment"
                )
            }
            if (item.ehr == currentHour && item.emin == currentMinute && StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId
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
                WorkManagerHelper.cancelWork(item.profileId.toString())
                if (!binding.pauseSwitch.isChecked) {
                    if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId)
                        audioManager.ringerMode = StoreSession.readInt(AppConstants.RINGTONE_MODE)
                } else {
                    if ((StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId) &&
                        ((item.shr == item.ehr && (currentHour == item.ehr && currentMinute > item.emin) || (currentHour > item.ehr)) ||
                                (item.shr < item.ehr && (currentHour >= item.ehr)) || (item.shr > item.ehr && (currentHour <= item.ehr)))
                    ) {
                        StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
                        StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, 0)
                    } else {
                        var smin: Int
                        var shr: Int
                        item.pauseSwitch = true
                        if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == item.profileId) {
                            if (currentMinute == 59) {
                                smin = 0
                                if (currentHour == 23)
                                    shr = 0
                                else
                                    shr = currentHour + 1
                            } else {
                                smin = currentMinute + 1
                                shr = currentHour
                            }
                            if (shr != item.ehr && smin != item.emin)
                            WorkManagerHelper.setAlarms(item, shr, smin)
                        } else {
                            WorkManagerHelper.setAlarms(item)
                        }
                    }
                }
                profileViewModel.update(item)
            }
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

    fun removeitem(position: Int) {
        profileViewModel.delete(profiles[position])
        notifyItemRemoved(position)
    }

    fun restoreItem(profile: Profile, position: Int) {
        profiles.add(position, profile)
        notifyItemChanged(position)
        profileViewModel.insert(profile)
    }

    fun getList() = profiles

    private fun introDelete(view: View) {
        MaterialIntroView.Builder(activity)
            .enableDotAnimation(false)
            .enableIcon(true)
            .setFocusGravity(FocusGravity.RIGHT)
            .setFocusType(Focus.MINIMUM)
            .setDelayMillis(400)
            .enableFadeAnimation(true)
            .performClick(false)
            .dismissOnTouch(true)
            .setInfoText("Slide the profile towards right to delete it")
            .setShape(ShapeType.CIRCLE)
            .setTarget(view)
            .setUsageId("intro_card") // THIS SHOULD BE UNIQUE ID
            .show()
    }

    private fun introDetail(view: View) {
        MaterialIntroView.Builder(activity)
            .enableDotAnimation(true)
            .enableIcon(true)
            .setFocusGravity(FocusGravity.CENTER)
            .setFocusType(Focus.NORMAL)
            .setDelayMillis(400)
            .enableFadeAnimation(true)
            .performClick(false)
            .dismissOnTouch(true)
            .setInfoText("Click on the profile to get profile details")
            .setShape(ShapeType.CIRCLE)
            .setTarget(view)
            .setUsageId("intro_card_2") // THIS SHOULD BE UNIQUE ID
            .setListener {
                introDelete(view)
            }
            .show()
    }

    fun deleteAll() {
        val size: Int = profiles.size
        for (i in 0 until size) {
            removeitem(i)
            WorkManagerHelper.cancelWork(profiles[i].profileId.toString())
        }
        StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
    }
}