package com.example.robin.quiethours.Adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.R
import com.example.robin.quiethours.databinding.ItemRowBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class ProfileListAdapter( val profileViewModel: ProfileViewModel, val parentView: View): ListAdapter<Profile, ProfileListAdapter.ViewHolder>(
    ProfileDiffCallbacks()
){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, profileViewModel, parentView)
    }

    class ViewHolder(val binding: ItemRowBinding): RecyclerView.ViewHolder(binding.root) {

        private val bgColors: IntArray = intArrayOf( Color.rgb(244, 81, 30), Color.rgb(17,94,231), Color.rgb(9,187,69), Color.rgb( 123, 31, 162 ), Color.rgb(191, 27, 19), Color.rgb( 0, 121, 107 ), Color.rgb(255, 143, 0), Color.rgb( 216, 27, 96 ))

        fun bind(item: Profile, profileViewModel: ProfileViewModel, parentView: View){
            binding.ProfileName.text = item.name
            binding.TxtImg.setText(item.name[0].toString())
            binding.TxtImg.avatarBackgroundColor = bgColors[Random.nextInt(0, 8)]
            binding.profileCard.setOnLongClickListener {
                profileViewModel.delete(item)
                val snackbar = Snackbar.make(parentView, "Profile Deleted",
                    Snackbar.LENGTH_LONG).setAction("Action", null)
                snackbar.show()
                WorkManager.getInstance().cancelAllWorkByTag(item.profileId.toString())
                return@setOnLongClickListener true
            }

            binding.profileCard.setOnClickListener {
                val args = Bundle()
                args.putParcelable("Profile", item)
                val navOptions = NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim).setExitAnim(
                    R.anim.nav_default_exit_anim
                ).setPopEnterAnim(R.anim.nav_default_pop_enter_anim).setPopExitAnim(R.anim.nav_default_pop_exit_anim).build()
                Navigation.findNavController(it).navigate(R.id.detailsFragment, args, navOptions)

            }
        }
    }

    class ProfileDiffCallbacks: DiffUtil.ItemCallback<Profile>(){
        override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
            return oldItem.profileId == newItem.profileId
        }


        override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
            return oldItem == newItem
        }

    }

}