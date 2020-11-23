package com.rob729.quiethours.Fragments

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.rob729.quiethours.Adapter.ProfileListAdapter
import com.rob729.quiethours.Database.Profile
import com.rob729.quiethours.Database.ProfileViewModel
import com.rob729.quiethours.R
import com.rob729.quiethours.databinding.FragmentMainBinding
import com.rob729.quiethours.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment() {
    private lateinit var profileViewModel: ProfileViewModel
    lateinit var profileListAdapter: ProfileListAdapter
    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private val appUpdateInfoTask: Task<AppUpdateInfo> by lazy { appUpdateManager.appUpdateInfo }
    private val MY_REQUEST_CODE = 111
    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        checkForUpdates()

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        profileListAdapter =
            ProfileListAdapter(profileViewModel, binding.rv.rootView, activity)

        binding.rv.adapter = profileListAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)

        enableSwipeToDeleteAndUndo(profileListAdapter)

        profileViewModel.allProfiles.observe(this, Observer { profilesList ->
            if (profilesList.isEmpty()) {
                binding.emrl.visibility = View.VISIBLE
            } else {
                binding.emrl.visibility = View.GONE
            }
            for (i in profilesList.indices) {
                if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == profilesList[i].profileId) {
                    if (!profilesList[i].pauseSwitch)
                        binding.activeCard.visibility = View.GONE
                    else
                        binding.activeCard.visibility = View.VISIBLE
                }
            }
            profileListAdapter.submitList(profilesList)
            profileListAdapter.profiles = profilesList as ArrayList<Profile>
        })

        val notificationManager =
            context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
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
        currentlyActiveProfile()
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun currentlyActiveProfile() {
        if (StoreSession.readInt(AppConstants.BEGIN_STATUS) > 0) {
            binding.activeProfile.visibility = View.VISIBLE
            binding.activeName.text = StoreSession.readString(AppConstants.ACTIVE_PROFILE_NAME)
            binding.endTimeTxt.text = StoreSession.readString(AppConstants.END_TIME)

            if (StoreSession.readInt(AppConstants.VIBRATE_STATE_ICON) == 1) {
                binding.vibIconImg.setImageResource(R.drawable.vibration)
            } else {
                binding.vibIconImg.setImageResource(R.drawable.mute)
            }
        } else {
            binding.activeProfile.visibility = View.GONE
            StoreSession.writeLong(AppConstants.ACTIVE_PROFILE_ID, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { result: AppUpdateInfo? ->
            applicationUpdateManager(result)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.settings, menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE && resultCode != RESULT_OK) {
            checkForUpdates()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                return view?.let { Navigation.findNavController(it) }?.let {
                    NavigationUI.onNavDestinationSelected(
                        item,
                        it
                    )
                }!! || super.onOptionsItemSelected(item)
            }
            R.id.action_delete -> {
                if (binding.emrl.visibility == View.GONE) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete All Profiles")
                        .setMessage("Are you sure you want to delete all the profiles?")
                        .setPositiveButton("Yes") { _, dialogInterface ->
                            if (StoreSession.readInt(AppConstants.BEGIN_STATUS) != 0)
                                Utils.audioManager.ringerMode =
                                    StoreSession.readInt(AppConstants.RINGTONE_MODE)
                            profileListAdapter.deleteAll()
                            binding.activeProfile.visibility = View.GONE
                        }
                        .setNegativeButton("No") { _, dialogInterface ->
                        }
                        .setCancelable(true)
                        .show()
                } else {
                    Utils.showSnackBar(
                        binding.coordLayout,
                        "No profile is present to be deleted",
                        Snackbar.LENGTH_LONG
                    )
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun enableSwipeToDeleteAndUndo(profileListAdapter: ProfileListAdapter) {
        val swipeToDeleteCallback = object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                profileListAdapter.removeitem(position)
                val item = profileListAdapter.getList()[position]
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?")
                    .setPositiveButton("Yes") { _, dialogInterface ->
                        if (item.profileId == StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID)) {
                            StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
                            binding.activeProfile.visibility = View.GONE
                            Utils.audioManager.ringerMode =
                                StoreSession.readInt(AppConstants.RINGTONE_MODE)
                        }
                        WorkManagerHelper.cancelWork(item.profileId.toString())
                        Utils.showSnackBar(
                            binding.coordLayout,
                            "Profile is removed from the list.",
                            Snackbar.LENGTH_LONG
                        )
                    }
                    .setNegativeButton("No") { _, dialogInterface ->
                        profileListAdapter.restoreItem(item, position)
                    }
                    .setCancelable(false)
                    .show()
            }
        }

        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(binding.rv)
    }

    private fun permissionDialog() {

        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Please give the Do Not Disturb access permission for the app to work properly. Click OK to continue.")
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
    private fun applicationUpdateManager(result: AppUpdateInfo?) {
        if (result?.updateAvailability()
            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        ) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                    result,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    MY_REQUEST_CODE
                )
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        }
    }

    private fun checkForUpdates() {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                // For a flexible update, use AppUpdateType.FLEXIBLE
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo? ->
                    applicationUpdateManager(result)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
