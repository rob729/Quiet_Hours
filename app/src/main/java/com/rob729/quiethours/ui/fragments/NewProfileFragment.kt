package com.rob729.quiethours.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.database.ProfileViewModel
import com.rob729.quiethours.databinding.FragmentNewProfileBinding
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.StoreSession
import com.rob729.quiethours.util.Utils
import com.rob729.quiethours.util.Utils.setStringFormat
import com.rob729.quiethours.util.Utils.showTimePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 *
 */

@AndroidEntryPoint
class NewProfileFragment : Fragment() {

    @Inject
    lateinit var gson: Gson

    private var shr = 0
    private var smin = 0
    private var ehr = 0
    private var emin = 0
    private var daysSelected: MutableList<Boolean> = ArrayList()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val appSharedPrefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            requireContext()
        )
    }
    private val currentTime = Calendar.getInstance()
    private val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    private val minute = currentTime.get(Calendar.MINUTE)
    val id = StoreSession.readLong(AppConstants.PROFILE_ID)
    private var _binding: FragmentNewProfileBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentNewProfileBinding.inflate(inflater, container, false)
        binding.toolBar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.vibSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.vibSwitch.text = if (isChecked) {
                "Vibrate"
            } else {
                "Silent"
            }
        }
        binding.noteCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.noteTextInput.visibility = VISIBLE
            } else {
                binding.noteEditText.setText("")
                binding.noteTextInput.visibility = GONE
            }
        }

        binding.dayPicker.clearSelection()

        binding.userToDoEditText.requestFocus()

        binding.StartTime.setOnClickListener {
            requireContext().showTimePicker(
                onTimeSelected = { hour, min ->
                    binding.StartTime.setStringFormat(
                        Utils.setTimeString(hour),
                        Utils.setTimeString(min)
                    )
                    shr = Utils.setTimeString(hour).toInt()
                    smin = Utils.setTimeString(min).toInt()
                }, hour, minute, appSharedPrefs.getBoolean("time format", false)
            )
        }

        binding.EndTime.setOnClickListener {
            requireContext().showTimePicker(
                onTimeSelected = { hour, min ->
                    binding.EndTime.setStringFormat(
                        Utils.setTimeString(hour),
                        Utils.setTimeString(min)
                    )
                    ehr = Utils.setTimeString(hour).toInt()
                    emin = Utils.setTimeString(min).toInt()
                }, hour, minute, appSharedPrefs.getBoolean("time format", false)

            )
        }

        binding.makeProfileFab.setOnClickListener {
            validateProfileData(it)
        }

        val args = arguments?.getParcelable<Profile>("Profile")
        if (args != null) {
            daysSelected = Utils.daysList(args.d)
            Utils.selectedDays(daysSelected, binding.dayPicker)
            binding.userToDoEditText.setText(args.name)
            if (!args.notes.isBlank()) {
                binding.noteEditText.setText(args.notes)
                binding.noteTextInput.visibility = VISIBLE
                binding.noteCheckBox.isChecked = true
            }
            binding.StartTime.setStringFormat(
                Utils.setTimeString(args.shr),
                Utils.setTimeString(args.smin)
            )
            shr = args.shr
            smin = args.smin
            ehr = args.ehr
            emin = args.emin
            binding.EndTime.setStringFormat(
                Utils.setTimeString(args.ehr),
                Utils.setTimeString(args.emin)
            )
            binding.vibSwitch.isChecked = args.vibSwitch
            binding.repeatWeeklySwitch.isChecked = args.repeatWeekly
            binding.makeProfileFab.text = "UPDATE"
        }
        return binding.root
    }

    private fun setDaysSelectedValue(selectedDays: List<MaterialDayPicker.Weekday>) {
        for (day in MaterialDayPicker.Weekday.values()) {
            daysSelected.add(day.ordinal, selectedDays.contains(day))
        }
    }

    private fun validateProfileData(view: View) {
        setDaysSelectedValue(binding.dayPicker.selectedDays)
        if (binding.userToDoEditText.text?.isEmpty() == true) {
            Utils.showSnackBar(view, "Please enter the Profile name", Snackbar.LENGTH_LONG)
        } else if ((shr == ehr) && (smin == emin)) {
            Utils.showSnackBar(
                view,
                "Please enter different start and end time",
                Snackbar.LENGTH_LONG
            )
        } else if (binding.dayPicker.selectedDays.isEmpty()) {
            Utils.showSnackBar(view, "Please select the day(s)", Snackbar.LENGTH_LONG)
        } else if ((shr > ehr) && (shr - ehr <= 12)) {
            Utils.showSnackBar(
                view,
                "Please enter a valid time.(Within 12 hour limit)",
                Snackbar.LENGTH_LONG
            )
        } else {
            addProfile()
        }
    }

    private fun addProfile() {
        val currentTime =
            SimpleDateFormat("EEE, d MMM yyyy hh:mm", Locale.getDefault()).format(Date())
        val profile = Profile(
            name = binding.userToDoEditText.text.toString(),
            shr = shr,
            smin = smin,
            ehr = ehr,
            emin = emin,
            d = gson.toJson(daysSelected),
            colorIndex = Random.nextInt(0, 8),
            vibSwitch = binding.vibSwitch.isChecked,
            timeInstance = currentTime,
            repeatWeekly = binding.repeatWeeklySwitch.isChecked,
            pauseSwitch = true,
            notes = binding.noteEditText.text.toString()
        )
        if (binding.makeProfileFab.text == "Submit") {
            profile.profileId = System.currentTimeMillis()
            profileViewModel.insert(profile)
        } else {
            profileViewModel.cancelAllWorkByTag(id.toString())
            profile.profileId = id
            profileViewModel.update(profile)
        }
        profileViewModel.setAlarms(profile)
        Navigation.findNavController(binding.root)
            .navigate(NewProfileFragmentDirections.actionNewProfileFragmentToMainFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
