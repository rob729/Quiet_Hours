package com.rob729.quiethours.Fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rob729.quiethours.Database.Profile
import com.rob729.quiethours.Database.ProfileViewModel
import com.rob729.quiethours.R
import com.rob729.quiethours.databinding.FragmentNewProfileBinding
import com.rob729.quiethours.util.*
import kotlinx.android.synthetic.main.fragment_new_profile.*
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
    private var minText = ""
    private var hourText = ""
    private var days: MutableList<Boolean> = ArrayList<Boolean>()
    private val noDaySelected = listOf(false, false, false, false, false, false, false)
    private var daysSelected: List<MaterialDayPicker.Weekday> = ArrayList()
    private lateinit var snackbar: Snackbar
    private lateinit var profileViewModel: ProfileViewModel
    val mcurrentTime = Calendar.getInstance()
    val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
    val minute = mcurrentTime.get(Calendar.MINUTE)
    val selectedDays by lazy { Gson() }
    val type by lazy { object : TypeToken<List<Boolean>>() {}.type }
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
            if (isChecked) {
                binding.vibSwitch.text = "Vibrate"
            } else {
                binding.vibSwitch.text = "Silent"
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
                TimePickerDialog.OnTimeSetListener { _, i, i1 ->
                    hourText = Utils.setTimeString(i)
                    minText = Utils.setTimeString(i1)
                    Log.e("TAG", "$hourText : $minText")
                    binding.StartTime.setText(
                        String.format(
                            resources.getString(R.string.Time),
                            hourText,
                            minText
                        )
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
                TimePickerDialog.OnTimeSetListener { _, i, i1 ->
                    hourText = Utils.setTimeString(i)
                    minText = Utils.setTimeString(i1)
                    binding.EndTime.setText(
                        String.format(
                            resources.getString(R.string.Time),
                            hourText,
                            minText
                        )
                    )
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
                viewSnackBar(it, "Please enter the Profile name")
                snackbar.show()
            } else if ((shr == ehr) && (smin == emin)) {
                viewSnackBar(it, "Please enter different start and end time")
            } else if (binding.dayPicker.selectedDays.size == 0) {
                viewSnackBar(it, "Please select the day(s)")
            } else if ((shr > ehr) && (shr - ehr <= 12)) {
                viewSnackBar(it, "Please enter a valid time.(Within 12 hour limit)")
            } else {
                val daySelected = Gson()
                // Generating formated current time
                var currentTime = SimpleDateFormat("EEE, d MMM yyyy hh:mm").format(Date())
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
                    WorkManager.getInstance(profileViewModel.getApplication()).cancelAllWorkByTag(id.toString())
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
            days = selectedDays.fromJson(args.d, type)
            Utils.selectedDays(days, binding.dayPicker)
            binding.userToDoEditText.setText(args.name)
            if (!args.notes.isBlank()) {
                binding.noteEditText.setText(args.notes)
                binding.noteTextInput.visibility = VISIBLE
                binding.noteCheckBox.isChecked = true
            }
            binding.StartTime.setText("${Utils.setTimeString(args.shr)}:${Utils.setTimeString(args.smin)}")
            shr = args.shr
            smin = args.smin
            ehr = args.ehr
            emin = args.emin
            binding.EndTime.setText("${Utils.setTimeString(args.ehr)}:${Utils.setTimeString(args.emin)}")
            binding.vibSwitch.isChecked = args.vibSwitch
            binding.repeatWeeklySwitch.isChecked = args.repeatWeekly
            binding.makeProfileFab.text = "UPDATE"
        }
        return binding.root
    }

    private fun viewSnackBar(it: View, message: String) {
        snackbar = Snackbar.make(
            it, message,
            Snackbar.LENGTH_LONG
        ).setAction("Action", null)
        snackbar.show()
    }

    private fun Days(daysSelected: List<MaterialDayPicker.Weekday>) {
        setDay(daysSelected, MaterialDayPicker.Weekday.SUNDAY, 0)
        setDay(daysSelected, MaterialDayPicker.Weekday.MONDAY, 1)
        setDay(daysSelected, MaterialDayPicker.Weekday.TUESDAY, 2)
        setDay(daysSelected, MaterialDayPicker.Weekday.WEDNESDAY, 3)
        setDay(daysSelected, MaterialDayPicker.Weekday.THURSDAY, 4)
        setDay(daysSelected, MaterialDayPicker.Weekday.FRIDAY, 5)
        setDay(daysSelected, MaterialDayPicker.Weekday.SATURDAY, 6)
    }

    private fun setDay(
        daysSelected: List<MaterialDayPicker.Weekday>,
        day: MaterialDayPicker.Weekday,
        index: Int
    ) {
        if (daysSelected.contains(day))
            days.add(index, true)
        else
            days.add(index, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
