package com.rob729.quiethours.Fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentSender
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
import co.mobiwise.materialintro.shape.Focus
import co.mobiwise.materialintro.shape.FocusGravity
import co.mobiwise.materialintro.shape.ShapeType
import co.mobiwise.materialintro.view.MaterialIntroView
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
import com.rob729.quiethours.util.SwipeToDeleteCallback

/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentMainBinding
    lateinit var profileListAdapter: ProfileListAdapter
    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private val appUpdateInfoTask: Task<AppUpdateInfo> by lazy { appUpdateManager.appUpdateInfo }
    private val MY_REQUEST_CODE = 111
    val notificationManager =
        context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main, container, false
        )

        checkForUpdates()

        introFab()

        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        profileListAdapter =
            ProfileListAdapter(profileViewModel, binding.rv.rootView, activity)

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

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { result: AppUpdateInfo? ->
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
                            .make(
                                binding.coordLayout,
                                "Profile is removed from the list.",
                                Snackbar.LENGTH_LONG
                            )
                            .show()
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

    private fun introFab() {
        MaterialIntroView.Builder(activity)
            .enableDotAnimation(false)
            .enableIcon(true)
            .setFocusGravity(FocusGravity.CENTER)
            .setFocusType(Focus.ALL)
            .setDelayMillis(500)
            .enableFadeAnimation(true)
            .performClick(false)
            .dismissOnTouch(true)
            .setInfoText("Click the + sign to add new profile")
            .setShape(ShapeType.CIRCLE)
            .setTarget(binding.floatingActionButton)
            .setUsageId("intro_card_1") // THIS SHOULD BE UNIQUE ID
            .show()
    }

    private fun checkForUpdates() {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                // For a flexible update, use AppUpdateType.FLEXIBLE
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateInfoTask.addOnSuccessListener { result: AppUpdateInfo? ->
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
            }
        }
    }
}
