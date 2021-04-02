package com.rob729.quiethours.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rob729.quiethours.R
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.databinding.FragmentDetailsBinding
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.StoreSession
import com.rob729.quiethours.util.Utils
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailsFragment : BottomSheetDialogFragment() {

    private lateinit var profile: Profile
    private var days: List<Boolean> = ArrayList()
    private var _binding: FragmentDetailsBinding? = null
    private val binding
        get() = _binding!!
    private val gson = Gson()
    private val type = object : TypeToken<List<Boolean>>() {}.type
    private val navOptions by lazy {
        NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build()
    }
    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(requireView().parent as View) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        profile = requireArguments().getParcelable("Profile")!!
        setupUI(profile)
        binding.editButton.setOnClickListener {
            editProfile()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // this forces the sheet to appear at max height even on landscape
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupUI(profile: Profile) {
        days = gson.fromJson(profile.d, type)
        Utils.selectedDays(days, binding.dayPicker)
        binding.profileTxt.text = profile.name
        binding.str.text = "${setTimeString(profile.shr)}:${setTimeString(profile.smin)}"
        binding.end.text = "${setTimeString(profile.ehr)}:${setTimeString(profile.emin)}"
        binding.profileNote.text = profile.notes
        binding.profileNote.visibility = if (binding.profileNote.text.isNotBlank()) VISIBLE else GONE
        if (profile.vibSwitch) binding.audioMode.setImageResource(R.drawable.vibration)
        else binding.audioMode.setImageResource(R.drawable.mute)
        binding.repeatWeeklyIcon.visibility = if (profile.repeatWeekly) VISIBLE else GONE
    }

    private fun setTimeString(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            "$i"
        }
    }

    private fun editProfile() {
        val bundle = Bundle().apply { putParcelable("Profile", profile) }
        StoreSession.writeLong(AppConstants.PROFILE_ID, profile.profileId)
        findNavController(this).navigate(R.id.newProfileFragment, bundle, navOptions)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(args: Bundle) = DetailsFragment().apply {
            arguments = args
        }
    }
}
