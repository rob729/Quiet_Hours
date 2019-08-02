package com.example.robin.quiethours.Fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.R
import com.example.robin.quiethours.databinding.FragmentDetailsBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class DetailsFragment : Fragment() {

    private var days: List<Boolean> = ArrayList()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentDetailsBinding>(
            inflater,
            R.layout.fragment_details, container, false
        )

        val args = arguments?.getParcelable<Profile>("Profile")
        val daysSelected = Gson()
        val type = object : TypeToken<List<Boolean>>() {}.type

        if (args != null) {
            days = daysSelected.fromJson(args.d, type)
            dayPicker(days, binding.dayPicker)
            binding.txt1.text = args.name
            binding.str.text = "${args.shr}:${args.smin}"
            binding.end.text = "${args.ehr}:${args.emin}"
        }


        return binding.root

    }

    private fun dayPicker(d: List<Boolean>, materialDayPicker: MaterialDayPicker) {
        if (d[0])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SUNDAY)
        if (d[1])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.MONDAY)
        if (d[2])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.TUESDAY)
        if (d[3])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.WEDNESDAY)
        if (d[4])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.THURSDAY)
        if (d[5])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.FRIDAY)
        if (d[6])
            materialDayPicker.selectDay(MaterialDayPicker.Weekday.SATURDAY)
    }


}
