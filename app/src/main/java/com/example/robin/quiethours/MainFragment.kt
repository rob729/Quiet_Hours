package com.example.robin.quiethours


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.databinding.FragmentMainBinding


/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentMainBinding>(inflater, R.layout.fragment_main, container, false);

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val profileListAdapter = ProfileListAdapter(profileViewModel)

        binding.rv.adapter = profileListAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)

        binding.lifecycleOwner = this


        profileViewModel.allProfiles.observe(this, Observer {
            if(it.isEmpty()){
                binding.emrl.visibility = View.VISIBLE
            } else {
                binding.emrl.visibility = View.GONE
            }

            profileListAdapter.submitList(it)

        })

        binding.floatingActionButton.setOnClickListener {
            Navigation.findNavController(it).navigate(MainFragmentDirections.actionMainFragmentToNewProfileFragment())
        }

        return  binding.root

    }


}
