package com.rob729.quiethours.Fragments

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
import com.rob729.quiethours.Database.Profile
import com.rob729.quiethours.R
import com.rob729.quiethours.databinding.FragmentDetailsBinding
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.Utils
import com.rob729.quiethours.util.StoreSession
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class DetailsFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(args: Bundle) = DetailsFragment().apply {
            val fragment = DetailsFragment()
            fragment.arguments = args
            return fragment
        }
    }
    private var days: List<Boolean> = ArrayList()
    private var _binding: FragmentDetailsBinding? = null
    private val binding
    get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        val args = arguments?.getParcelable<Profile>("Profile")
        val daysSelected = Gson()
        val type = object : TypeToken<List<Boolean>>() {}.type

        if (args != null) {
            days = daysSelected.fromJson(args.d, type)
            Utils.selectedDays(days, binding.dayPicker)
            binding.profileTxt.text = args.name
            binding.str.text = "${setTimeString(args.shr)}:${setTimeString(args.smin)}"
            binding.end.text = "${setTimeString(args.ehr)}:${setTimeString(args.emin)}"
            if (args.vibSwitch) binding.audioMode.setImageResource(R.drawable.vibration)
            else binding.audioMode.setImageResource(R.drawable.mute)
            binding.repeatWeeklyIcon.visibility = if (args.repeatWeekly) VISIBLE
            else GONE
        }

        binding.editButton.setOnClickListener {
            val item: Profile = args!!
            val bundle = Bundle()
            bundle.putParcelable("Profile", item)
            StoreSession.writeLong(AppConstants.PROFILE_ID, item.profileId)
            val navOptions =
                NavOptions.Builder().setEnterAnim(R.anim.nav_default_enter_anim).setExitAnim(
                    R.anim.nav_default_exit_anim
                ).setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                    .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                    .build()
            findNavController(this).navigate(R.id.newProfileFragment, bundle, navOptions)
            dismiss()
        }
        return binding.root
    }
    private fun setTimeString(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            "$i"
        }
    }

    override fun onStart() {
        super.onStart()
        // this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
