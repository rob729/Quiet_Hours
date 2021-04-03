package com.rob729.quiethours.ui.fragments

import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentSender
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
import com.rob729.quiethours.R
import com.rob729.quiethours.ui.adapter.AdapterCallback
import com.rob729.quiethours.ui.adapter.ProfileListAdapter
import com.rob729.quiethours.database.Profile
import com.rob729.quiethours.database.ProfileViewModel
import com.rob729.quiethours.databinding.FragmentMainBinding
import com.rob729.quiethours.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
@AndroidEntryPoint
class MainFragment : Fragment() {

    @Inject lateinit var audioManager: AudioManager
    @Inject lateinit var notificationManager: NotificationManager

    private val profilesListData: ArrayList<Profile> = ArrayList()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var profileListAdapter: ProfileListAdapter
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(
            requireContext()
        )
    }
    private val appUpdateInfoTask: Task<AppUpdateInfo> by lazy { appUpdateManager.appUpdateInfo }
    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = _binding!!
    private val swipeToDeleteCallback by lazy {
        object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                removeItem(position)
                val item = profilesListData[position]
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?")
                    .setPositiveButton("Yes") { _, dialogInterface ->
                        deleteItem(item)
                    }
                    .setNegativeButton("No") { _, dialogInterface ->
                        restoreItem(item, position)
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    private val adapterCallback = object : AdapterCallback {
        override fun updateItem(profile: Profile) {
            profileViewModel.update(profile)
        }

        override fun openProfileDetails(profile: Profile) {
            val args = Bundle()
            args.putParcelable("Profile", profile)
            val dialog = DetailsFragment.newInstance(args)
            dialog.show(
                requireActivity().supportFragmentManager,
                "DialogFragment"
            )
        }

        override fun setAlarms(profile: Profile, startHour: Int, startMinute: Int) {
            profileViewModel.setAlarms(profile, startHour, startMinute)
        }

        override fun cancelWorkByTag(tag: String) {
            profileViewModel.cancelAllWorkByTag(tag)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        checkForUpdates()

        profileListAdapter = ProfileListAdapter(adapterCallback, audioManager)

        binding.rv.adapter = profileListAdapter
        binding.rv.layoutManager = LinearLayoutManager(context)

        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.rv)

        profileViewModel.allProfiles.observe(viewLifecycleOwner, Observer { profilesList ->
            profilesListData.clear()
            profilesListData.addAll(profilesList)
            if (profilesList.isEmpty()) {
                binding.emrl.visibility = View.VISIBLE
            } else {
                binding.emrl.visibility = View.GONE
            }
            for (i in profilesList.indices) {
                if (StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID) == profilesList[i].profileId) {
                    binding.activeCard.visibility = if (profilesList[i].pauseSwitch)
                        View.VISIBLE
                    else
                        View.GONE
                }
            }
            profileListAdapter.submitList(profilesList)
            profileListAdapter.profiles = profilesList as ArrayList<Profile>
        })

        binding.floatingActionButton.setOnClickListener {
            if (doNotDisturbPermissionCheck()) {
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

    private fun doNotDisturbPermissionCheck(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted
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
        if (requestCode == AppConstants.REQUEST_CODE && resultCode != RESULT_OK) {
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
                                audioManager.ringerMode =
                                    StoreSession.readInt(AppConstants.RINGTONE_MODE)
                            deleteAllProfiles()
                            binding.activeProfile.visibility = View.GONE
                        }
                        .setNegativeButton("No") { _, dialogInterface ->
                        }
                        .setCancelable(true)
                        .show()
                } else {
                    Utils.showSnackBar(
                        binding.coordLayout,
                        "No profile is present to delete",
                        Snackbar.LENGTH_LONG
                    )
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun permissionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Please give the Do Not Disturb access permission for the app to work` properly. Click OK to continue.")
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
                    requireActivity(),
                    AppConstants.REQUEST_CODE
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

    private fun removeItem(position: Int) {
        profileViewModel.delete(profilesListData[position])
        profileListAdapter.notifyItemRemoved(position)
    }

    private fun restoreItem(profile: Profile, position: Int) {
        profilesListData.add(position, profile)
        profileListAdapter.notifyItemChanged(position)
        profileViewModel.insert(profile)
    }

    private fun deleteItem(profile: Profile) {
        if (profile.profileId == StoreSession.readLong(AppConstants.ACTIVE_PROFILE_ID)) {
            StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
            binding.activeProfile.visibility = View.GONE
            audioManager.ringerMode =
                StoreSession.readInt(AppConstants.RINGTONE_MODE)
        }
        profileViewModel.cancelAllWorkByTag(profile.profileId.toString())
        Utils.showSnackBar(
            binding.coordLayout,
            "Profile is removed from the list.",
            Snackbar.LENGTH_LONG
        )
    }

    private fun deleteAllProfiles() {
        val size: Int = profilesListData.size
        for (i in 0 until size) {
            removeItem(i)
            profileViewModel.cancelAllWorkByTag(profilesListData[i].profileId.toString())
        }
        StoreSession.writeInt(AppConstants.BEGIN_STATUS, 0)
    }
}
