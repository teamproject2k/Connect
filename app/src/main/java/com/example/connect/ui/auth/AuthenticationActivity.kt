package com.example.connect.ui.auth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connect.base.BaseActivity
import com.example.connect.ui.auth.userDetails.UserDetailsScreen
import com.example.connect.ui.theme.ConnectTheme

class AuthenticationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            ConnectTheme {
                Surface {
//                     MobileNumberInputScreen()
//                    OTPScreen()
                    UserDetailsScreen()
                }
            }
        }
    }
}