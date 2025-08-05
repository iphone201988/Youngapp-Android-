package com.tech.young.ui.welcome.main_splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200


    override fun getLayoutResource(): Int {
        return R.layout.activity_main_splash
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView() {
        BindingUtils.statusBarStyleWhite(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.white))
        checkLocation()
        checkPermissions()
        checkAudioPermission()
    }


    private fun checkLocation() {
        Permissions.check(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            0,
            object : PermissionHandler() {
                override fun onGranted() {
                    createLocationHandler()
                    proceedToNextScreen()
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    // Proceed even if location is denied
                    proceedToNextScreen()
                }
            }
        )
    }

    private fun proceedToNextScreen() {
        android.os.Handler().postDelayed({
            val loginData = sharedPrefManager.getLoginData()
            if (loginData == null) {
                // Not logged in
                startActivity(Intent(this@MainSplashActivity, MySplashActivity::class.java))
            } else {
                startActivity(Intent(this@MainSplashActivity, HomeActivity::class.java))
            }
            finish() // optional: close splash screen
        }, 1000)
    }



    private fun createLocationHandler() {
        locationHandler = LocationHandler(this, this)
        locationHandler?.getUserLocation()
        locationHandler?.removeLocationUpdates()
    }

    override fun getLocation(location: Location) {
        this.mCurrentLocation = location
        Log.i("fdsf", "getLocation: ${mCurrentLocation!!.latitude}  , ${mCurrentLocation!!.longitude}")
        val latitude = location.latitude
        val longitude = location.longitude


        LoginFragment.lat = latitude
        LoginFragment.long = longitude

        SetupPasswordFragment.lat = latitude
        SetupPasswordFragment.long = longitude


    }

    private fun checkPermissions() {
        if (!hasPermissions(this, permissions)) {
            permissionResultLauncher.launch(permissions)
        } else {
            // All permissions already granted
        }
    }


    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.all { it.value }) {
                // All permissions granted
            } else {
                // One or more permissions denied
            }
        }
    /** check audio permission **/
    private fun checkAudioPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this@MainSplashActivity, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainSplashActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            return false
        } else {
            return true
        }
    }

    }