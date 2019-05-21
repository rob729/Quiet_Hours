package com.example.robin.quiethours


import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.databinding.FragmentNewProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.util.*
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
    private var daysSelected: List<MaterialDayPicker.Weekday> = ArrayList<MaterialDayPicker.Weekday>()
    private lateinit var snackbar: Snackbar
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<FragmentNewProfileBinding>(inflater, R.layout.fragment_new_profile, container, false)

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        binding.dayPicker.clearSelection()

        binding.StartTime.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mcurrentTime.get(Calendar.MINUTE)
            sTimePicker = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
                    hourText = if(i<10){
                        "0$i"
                    }else{
                        "$i"
                    }

                    minText = if(i1<10){
                        "0$i1"
                    }else{
                        "$i1"
                    }

                    binding.StartTime.setText("$hourText:$minText")
                    shr = i
                    smin = i1
                }, hour, minute, false
            )
            sTimePicker.show()
        }

        binding.EndTime.setOnClickListener {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute = mcurrentTime.get(Calendar.MINUTE)
            eTimePicker = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
                    hourText = if(i<10){
                        "0$i"
                    }else{
                        "$i"
                    }

                    minText = if(i1<10){
                        "0$i1"
                    }else{
                        "$i1"
                    }

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
            }
        }

        return  binding.root
    }

    private fun Days(daysSelected: List<MaterialDayPicker.Weekday>) {
        if (daysSelected.contains(MaterialDayPicker.Weekday.SUNDAY))
            days.add(0, true)
        else
            days.add(0, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.MONDAY))
            days.add(1, true)
        else
            days.add(1, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.TUESDAY))
            days.add(2, true)
        else
            days.add(2, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.WEDNESDAY))
            days.add(3, true)
        else
            days.add(3, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.THURSDAY))
            days.add(4, true)
        else
            days.add(4, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.FRIDAY))
            days.add(5, true)
        else
            days.add(5, false)
        if (daysSelected.contains(MaterialDayPicker.Weekday.SATURDAY))
            days.add(6, true)
        else
            days.add(6, false)
    }


}
