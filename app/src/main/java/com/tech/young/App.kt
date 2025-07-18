package com.tech.young

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.tech.young.base.AppLifecycleListener
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    private var isRestarting: Boolean = false

    fun onLogout() {
        restartApp()
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase first

        // FirebaseApp.initializeApp(this)
        //  Firebase.initialize(this)
        val firebaseApp = FirebaseApp.initializeApp(this)
        if (firebaseApp == null) {
            Log.e("FirebaseInit", "FirebaseApp FAILED to initialize!")
        } else {
            Log.d("FirebaseInit", "FirebaseApp initialized SUCCESSFULLY")
        }

        // Now it's safe to use Firebase
        // FirebaseApp.getInstance()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(this@App))
    }

    private fun restartApp() {
        if (!isRestarting) {
            isRestarting = true
            val intent =
                baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
