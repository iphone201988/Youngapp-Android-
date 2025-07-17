package com.tech.young.ui

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tech.young.R
import com.tech.young.base.BaseActivity
import com.tech.young.base.BaseViewModel
import com.tech.young.base.utils.BindingUtils
import com.tech.young.databinding.ActivityMySplashBinding
import com.tech.young.ui.auth.AuthCommonVM
import com.tech.young.ui.home.HomeActivity
import com.tech.young.ui.welcome.WelcomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySplashActivity : BaseActivity<ActivityMySplashBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_my_splash
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initView()
        initOnClick()

    }

    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.tvRegisterNow -> {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("type", "register")
                    startActivity(intent)
                }

                R.id.tvGetStart -> {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.putExtra("type", "login")
                    startActivity(intent)

                }
            }
        })

    }

    private fun initView() {
        BindingUtils.statusBarStyleWhite(this)
        BindingUtils.styleSystemBars(this, getColor(R.color.white))


//        val loginData = sharedPrefManager.getLoginData()
//        Handler(Looper.getMainLooper()).postDelayed({
//        if (loginData != null) {
//            // Not logged in
//            val intent = Intent(this , HomeActivity::class.java)
//            startActivity(intent)
//        }
//        }, 2000)

    }

}