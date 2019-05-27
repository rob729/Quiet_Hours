package com.example.robin.quiethours.Fragments


import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.EndAlarm
import com.example.robin.quiethours.R
import com.example.robin.quiethours.StartAlarm
import com.example.robin.quiethours.databinding.FragmentNewProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


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
    private val noDaySelected = listOf(false, false, false, false, false, false, false )
    private var daysSelected: List<MaterialDayPicker.Weekday> = ArrayList()
    private lateinit var snackbar: Snackbar
    private lateinit var profileViewModel: ProfileViewModel
    val mcurrentTime = Calendar.getInstance()
    val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
    val minute = mcurrentTime.get(Calendar.MINUTE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentNewProfileBinding>(inflater,
            R.layout.fragment_new_profile, container, false)

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        binding.dayPicker.clearSelection()

        binding.StartTime.setOnClickListener {
            sTimePicker = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { _, i, i1 ->
                    hourText = setTimeString(i)

                    minText = setTimeString(i1)

                    binding.StartTime.setText("$hourText:$minText")
                    shr = i
                    smin = i1
                }, hour, minute, false
            )
            sTimePicker.show()
        }

        binding.EndTime.setOnClickListener {
            eTimePicker = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { _, i, i1 ->
                    hourText = setTimeString(i)

                    minText = setTimeString(i1)

                    binding.EndTime.setText("$hourText:$minText")
                    ehr = i
                    emin = i1
                }, hour, minute, false
            )
            eTimePicker.show()
        }

        binding.makeProfileFab.setOnClickListener {
            daysSelected = binding.dayPicker.selectedDays
            Days(daysSelected)
            if(binding.userToDoEditText.text.toString() == ""){
                snackbar = Snackbar.make(it, "Please enter the Profile name",
                    Snackbar.LENGTH_LONG).setAction("Action", null)
                snackbar.show()
            } else if ((shr==ehr)&&(smin==emin)){
                snackbar = Snackbar.make(it, "Please enter different start and end time",
                    Snackbar.LENGTH_LONG).setAction("Action", null)
                snackbar.show()
            } else if (binding.dayPicker.selectedDays.size == 0) {
                snackbar = Snackbar.make(it, "Please select the day(s)",
                    Snackbar.LENGTH_LONG).setAction("Action", null)
                snackbar.show()
            } else {
                val daySelected = Gson()
                val profile = Profile(name = binding.userToDoEditText.text.toString(), shr = shr, smin = smin, ehr = ehr, emin = emin, d = daySelected.toJson(days))
                profileViewModel.insert(profile)
                Navigation.findNavController(it).navigate(NewProfileFragmentDirections.actionNewProfileFragmentToMainFragment())
                var i=0
                while(i<7){
                    if(days[i]){
                        sAlarm(i+1, profile)
                        eAlarm(i+1, profile.profileId.toString())
                    }
                    ++i
                }
            }
        }

        return  binding.root
    }

    private fun setTimeString(i: Int): String {
        return if (i < 10) {
            "0$i"
        } else {
            "$i"
        }
    }

    private fun Days(daysSelected: List<MaterialDayPicker.Weekday>) {
        setDay(daysSelected, MaterialDayPicker.Weekday.SUNDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.MONDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.TUESDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.WEDNESDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.THURSDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.FRIDAY)
        setDay(daysSelected, MaterialDayPicker.Weekday.SATURDAY)
    }

    private fun setDay(daysSelected: List<MaterialDayPicker.Weekday>, day: MaterialDayPicker.Weekday) {
        if (daysSelected.contains(day))
            days.add(0, true)
        else
            days.add(0, false)
    }

    private fun sAlarm(dayOfWeek: Int, profile: Profile) {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, shr)
        c.set(Calendar.MINUTE, smin)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)


        if (c.timeInMillis < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 7)
        }

        val profileData = workDataOf(Pair("Profile_Name", profile.name))

        val startAlarmRequest = OneTimeWorkRequest.Builder(StartAlarm::class.java)
            .addTag(profile.profileId.toString())
            .setInputData(profileData)
            .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance().enqueue(startAlarmRequest)

    }

    private fun eAlarm(dayOfWeek: Int, tag: String) {

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        c.set(Calendar.HOUR_OF_DAY, ehr)
        c.set(Calendar.MINUTE, emin)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        if (c.timeInMillis < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 7)
        }

        val endAlarmRequest = OneTimeWorkRequest.Builder(EndAlarm::class.java)
            .addTag(tag)
            .setInitialDelay(c.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance().enqueue(endAlarmRequest)
    }


}
