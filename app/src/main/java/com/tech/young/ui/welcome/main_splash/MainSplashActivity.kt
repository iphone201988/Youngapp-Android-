package com.tech.young.ui.welcome.main_splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    }


    private fun checkLocation() {
        Permissions.check(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            0,
            object : PermissionHandler() {
                override fun onGranted() {
                    createLocationHandler()
                    android.os.Handler().postDelayed({
                        val loginData = sharedPrefManager.getLoginData()
                        if (loginData == null) {
                            // Not logged in
                            val intent =
                                Intent(this@MainSplashActivity, MySplashActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent =
                                Intent(this@MainSplashActivity, HomeActivity::class.java)
                            startActivity(intent)

                        }
                    }, 1000)
                }

                override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                    super.onDenied(context, deniedPermissions)
                }
            })

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
}