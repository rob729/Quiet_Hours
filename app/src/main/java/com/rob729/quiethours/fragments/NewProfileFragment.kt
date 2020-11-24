package com.rob729.quiethours.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.rob729.quiethours.R
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.database.ProfileViewModel
import com.rob729.quiethours.databinding.FragmentNewProfileBinding
import com.rob729.quiethours.util.AppConstants
import com.rob729.quiethours.util.StoreSession
import com.rob729.quiethours.util.Utils
import com.rob729.quiethours.util.WorkManagerHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 *
 */
class NewProfileFragment : Fragment() {
    private lateinit var sTimePicker: TimePickerDialog
    private lateinit var eTimePicker: TimePickerDialog
    private var shr = 0
    private var smin = 0
    private var ehr = 0
    private var emin = 0
    private var days: MutableList<Boolean> = ArrayList()
    private var daysSelected: List<MaterialDayPicker.Weekday> = ArrayList()
    private lateinit var profileViewModel: ProfileViewModel
    val mcurrentTime = Calendar.getInstance()
    val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
    val minute = mcurrentTime.get(Calendar.MINUTE)
    val id = StoreSession.readLong(AppConstants.PROFILE_ID)
    private var _binding: FragmentNewProfileBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentNewProfileBinding.inflate(inflater, container, false)
        binding.toolBar.setNavigationOnClickListener { activity!!.onBackPressed() }
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

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        binding.dayPicker.clearSelection()
        val appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        binding.userToDoEditText.requestFocus()

        binding.StartTime.setOnClickListener {
            sTimePicker = TimePickerDialog(
                context,
                { _, i, i1 ->
                    binding.StartTime.setStringFormat(
                        Utils.setTimeString(i),
                        Utils.setTimeString(i1)
                    )
                    shr = Utils.setTimeString(i).toInt()
                    smin = Utils.setTimeString(i1).toInt()
                }, hour, minute, appSharedPrefs.getBoolean("time format", false)
            )
            sTimePicker.show()
        }

        binding.EndTime.setOnClickListener {
            eTimePicker = TimePickerDialog(
                context,
                { _, i, i1 ->
                    binding.EndTime.setStringFormat(Utils.setTimeString(i), Utils.setTimeString(i1))
                    ehr = Utils.setTimeString(i).toInt()
                    emin = Utils.setTimeString(i1).toInt()
                }, hour, minute, appSharedPrefs.getBoolean("time format", false)
            )
            eTimePicker.show()
        }

        binding.makeProfileFab.setOnClickListener {
            daysSelected = binding.dayPicker.selectedDays
            Days(daysSelected)
            if (binding.userToDoEditText.text.toString() == "") {
                Utils.showSnackBar(it, "Please enter the Profile name", Snackbar.LENGTH_LONG)
            } else if ((shr == ehr) && (smin == emin)) {
                Utils.showSnackBar(
                    it,
                    "Please enter different start and end time",
                    Snackbar.LENGTH_LONG
                )
            } else if (binding.dayPicker.selectedDays.size == 0) {
                Utils.showSnackBar(it, "Please select the day(s)", Snackbar.LENGTH_LONG)
            } else if ((shr > ehr) && (shr - ehr <= 12)) {
                Utils.showSnackBar(
                    it,
                    "Please enter a valid time.(Within 12 hour limit)",
                    Snackbar.LENGTH_LONG
                )
            } else {
                val daySelected = Gson()
                // Generating formated current time
                val currentTime = SimpleDateFormat("EEE, d MMM yyyy hh:mm", Locale.getDefault()).format(Date())
                val profile = Profile(
                    name = binding.userToDoEditText.text.toString(),
                    shr = shr,
                    smin = smin,
                    ehr = ehr,
                    emin = emin,
                    d = daySelected.toJson(days),
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
                    WorkManager.getInstance(profileViewModel.getApplication())
                        .cancelAllWorkByTag(id.toString())
                    profile.profileId = id
                    profileViewModel.update(profile)
                }
                Navigation.findNavController(it)
                    .navigate(NewProfileFragmentDirections.actionNewProfileFragmentToMainFragment())
                WorkManagerHelper.setAlarms(profile)
            }
        }
        val args = arguments?.getParcelable<Profile>("Profile")
        if (args != null) {
            days = Utils.daysList(args.d)
            Utils.selectedDays(days, binding.dayPicker)
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

    private fun Days(daysSelected: List<MaterialDayPicker.Weekday>) {
        for (day in MaterialDayPicker.Weekday.values()) {
            setDay(daysSelected, day, day.ordinal)
        }
    }

    private fun EditText.setStringFormat(hourText: String, minText: String) {
        setText(
            String.format(
                resources.getString(R.string.Time),
                hourText,
                minText
            )
        )
    }

    private fun setDay(
        daysSelected: List<MaterialDayPicker.Weekday>,
        day: MaterialDayPicker.Weekday,
        index: Int
    ) {
        days.add(index, daysSelected.contains(day))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
