package com.example.connect.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.NavGraphs
import com.example.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.example.connect.presentation.ui.destinations.MobileNumberInputScreenDestination
import com.example.connect.presentation.ui.destinations.UserDetailsScreenDestination
import com.example.connect.presentation.ui.home.HomeActivity
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.example.connect.presentation.ui.common.LocalActivity
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID


@AndroidEntryPoint
class AuthenticationActivity : BaseActivity() {
    companion object {
        var Instance: AuthenticationActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setDeviceInfo()
        if (firebaseAuth.currentUser != null && sharedPreferences.isUserDetailsEntered) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Instance = this
            setContent {
                CompositionLocalProvider(LocalActivity provides this) {
                    ConnectTheme {
                        DestinationsNavHost(
                            navGraph = NavGraphs.authentication,
                            engine = getAnimatedNavHostEngine(),
                            startRoute = if (firebaseAuth.currentUser != null) UserDetailsScreenDestination else MobileNumberInputScreenDestination
                        )
                    }
                }
            }
        }
    }

    private fun setDeviceInfo() {
        if (sharedPreferences.deviceId.isNullOrBlank()) {
            sharedPreferences.deviceId = UUID.randomUUID().toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Instance = null
    }
}