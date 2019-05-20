package com.example.robin.quiethours

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.databinding.ItemRowBinding
import kotlin.random.Random

class ProfileListAdapter(val profileViewModel: ProfileViewModel): ListAdapter<Profile, ProfileListAdapter.ViewHolder>(ProfileDiffCallbacks()){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, profileViewModel)
    }

    class ViewHolder(val binding: ItemRowBinding): RecyclerView.ViewHolder(binding.root) {

        private val bgColors: IntArray = intArrayOf( Color.rgb(220,85,31), Color.rgb(17,94,231), Color.rgb(9,187,69), Color.rgb(105,19, 191), Color.rgb(191, 27, 19))

        fun bind(item: Profile, profileViewModel: ProfileViewModel){
            binding.ProfileName.text = item.name
            binding.TxtImg.setText(item.name[0].toString())
            binding.TxtImg.avatarBackgroundColor = bgColors[Random.nextInt(0, 5)]
            binding.profileCard.setOnLongClickListener {
                profileViewModel.delete(item)
                return@setOnLongClickListener true
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