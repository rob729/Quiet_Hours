package com.example.robin.quiethours.Fragments


import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.robin.quiethours.Adapter.ProfileListAdapter
import com.example.robin.quiethours.Database.Profile
import com.example.robin.quiethours.Database.ProfileViewModel
import com.example.robin.quiethours.R
import com.example.robin.quiethours.databinding.FragmentMainBinding
import com.example.robin.quiethours.util.SwipeToDeleteCallback
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentMainBinding
    val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main, container, false
        )

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val profileListAdapter =
            ProfileListAdapter(profileViewModel, binding.rv.rootView)

        binding.rv.adapter = profileListAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)

        binding.lifecycleOwner = this

        enableSwipeToDeleteAndUndo(profileListAdapter)

        profileViewModel.allProfiles.observe(this, Observer {
            if (it.isEmpty()) {
                binding.emrl.visibility = View.VISIBLE
            } else {
                binding.emrl.visibility = View.GONE
            }

            profileListAdapter.submitList(it)
            profileListAdapter.profiles = it as ArrayList<Profile>

        })

        val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager!!.isNotificationPolicyAccessGranted) {
            permissionDialog()
        }

        binding.floatingActionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager!!.isNotificationPolicyAccessGranted) {
                permissionDialog()
            } else {
                Navigation.findNavController(it)
                    .navigate(MainFragmentDirections.actionMainFragmentToNewProfileFragment())
            }
        }

        setHasOptionsMenu(true)
        return binding.root

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return view?.let { Navigation.findNavController(it) }?.let {
            NavigationUI.onNavDestinationSelected(
                item,
                it
            )
        }!! || super.onOptionsItemSelected(item)

    }

    private fun enableSwipeToDeleteAndUndo(profileListAdapter: ProfileListAdapter) {
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                profileListAdapter.removeitem(position)
                val item = profileListAdapter.getList()[position]

                AlertDialog.Builder(context)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?")
                    .setPositiveButton("Yes") { _, dialogInterface ->
                        profileListAdapter.removeWork(item.profileId.toString())
                        Snackbar
                            .make(binding.coordLayout, "Profile is removed from the list.", Snackbar.LENGTH_LONG)
                            .show()
                    }
                    .setNegativeButton("No"){ _, dialogInterface ->
                        profileListAdapter.restoreItem(item, position)
                    }
                    .show()
            }
        }

        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(binding.rv)
    }

    private fun permissionDialog(){

        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage("Please give the necessary permissions for the app to work properly.")
            .setCancelable(false)
            .setPositiveButton("Ok") { i, dialogInterface ->
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent(
                        android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    )
                } else {
                    TODO("VERSION.SDK_INT < M")
                }

                startActivity(intent)
            }
            .show()
    }



}
