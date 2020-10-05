package com.rob729.quiethours.Adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
import com.rob729.quiethours.Database.Profile
import com.rob729.quiethours.Database.ProfileViewModel
import com.rob729.quiethours.R
import com.rob729.quiethours.databinding.ItemRowBinding
import kotlin.random.Random

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, profileViewModel, parentView)
        if (position == 0) {
            introDetail(holder.binding.profileCard)
        }
    }

    class ViewHolder(val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {

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
            binding.TxtImg.avatarBackgroundColor = bgColors[Random.nextInt(0, 8)]
            // setting value of timeInstance
            binding.Date.text = item.timeInstance

            binding.profileCard.setOnClickListener {
                val args = Bundle()
                args.putParcelable("Profile", item)
                val navOptions = NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim).setExitAnim(
                    R.anim.nav_default_exit_anim
                ).setPopEnterAnim(R.anim.nav_default_pop_enter_anim).setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                    .build()
                Navigation.findNavController(it).navigate(R.id.detailsFragment, args, navOptions)
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

    fun removeWork(tag: String) {
        WorkManager.getInstance(profileViewModel.getApplication()).cancelAllWorkByTag(tag)
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
}