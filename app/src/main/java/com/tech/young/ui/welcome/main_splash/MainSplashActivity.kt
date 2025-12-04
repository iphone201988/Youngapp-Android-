package com.tech.young.ui.welcome.main_splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tech.young.R
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.location.LocationHandler
import com.tech.young.base.location.LocationResultListener
import com.tech.young.base.permission.PermissionHandler
import com.tech.young.base.permission.Permissions
import com.tech.young.base.utils.BindingUtils
import com.tech.young.base.utils.BindingUtils.hasPermissions
import com.tech.young.base.utils.BindingUtils.permissions
import com.tech.young.databinding.ActivityMainSplashBinding
import com.tech.young.ui.MySplashActivity
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.auth.login_flow.LoginFragment
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.signup_process.SetupPasswordFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainSplashActivity : BaseActivity<ActivityMainSplashBinding>(), LocationResultListener {

    private val viewModel: AuthCommonVM by viewModels()
    private var locationHandler: LocationHandler? = null
    private var mCurrentLocation: Location? = null

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun getLayoutResource(): Int = R.layout.activity_main_splash

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView() {
        BindingUtils.statusBarStyleWhite(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.white))

        // 👉 FIX: Request permissions sequentially
        requestLocationPermission()
    }

    // -------------------------------------------------------
    // 1️⃣ LOCATION PERMISSION
    // -------------------------------------------------------
    private fun requestLocationPermission() {
        Permissions.check(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            0,
            object : PermissionHandler() {
                override fun onGranted() {
                    createLocationHandler()
                    requestNotificationPermission() // NEXT
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    requestNotificationPermission() // STILL NEXT
                }
            }
        )
    }

    private fun createLocationHandler() {
        locationHandler = LocationHandler(this, this)
        locationHandler?.getUserLocation()
        locationHandler?.removeLocationUpdates()
    }

    override fun getLocation(location: Location) {
        mCurrentLocation = location
        Log.i("MainSplash", "Location: ${location.latitude}, ${location.longitude}")

        LoginFragment.lat = location.latitude
        LoginFragment.long = location.longitude

        SetupPasswordFragment.lat = location.latitude
        SetupPasswordFragment.long = location.longitude
    }

    // -------------------------------------------------------
    // 2️⃣ NOTIFICATION PERMISSION
    // -------------------------------------------------------
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )

                return
            }
        }

        // Continue to next
        requestAudioPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            requestAudioPermission() // continue chain
        }
    }

    // -------------------------------------------------------
    // 3️⃣ AUDIO PERMISSION
    // -------------------------------------------------------
    private fun requestAudioPermission() {
        Permissions.check(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            "Microphone permission is required.",
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    requestOtherPermissions() // NEXT
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    requestOtherPermissions() // STILL NEXT
                }
            }
        )
    }

    // -------------------------------------------------------
    // 4️⃣ OTHER PERMISSIONS FROM BindingUtils.permissions
    // -------------------------------------------------------
    private fun requestOtherPermissions() {
        if (!hasPermissions(this, permissions)) {
            permissionResultLauncher.launch(permissions)
        } else {
            proceedToNextScreen()
        }
    }

    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            proceedToNextScreen()
        }

    // -------------------------------------------------------
    // 5️⃣ MOVE TO NEXT SCREEN
    // -------------------------------------------------------
    private fun proceedToNextScreen() {
        android.os.Handler().postDelayed({
            val loginData = sharedPrefManager.getLoginData()

            if (loginData == null) {
                startActivity(Intent(this, MySplashActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }

            finish()
        }, 1000)
    }
}
