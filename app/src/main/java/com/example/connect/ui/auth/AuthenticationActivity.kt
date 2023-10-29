package com.example.connect.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connect.ui.auth.mobile_input.MobileNumberInputScreen
import com.example.connect.ui.theme.ConnectTheme

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            ConnectTheme {
                Surface {
                    MobileNumberInputScreen()
                }
            }
        }
    }
}