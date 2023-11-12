package com.example.connect.presentation.ui.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.home.user_profile.UserProfileScreen
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.example.connect.presentation.utils.LocalActivity

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                ConnectTheme {
                    UserProfileScreen()
                }
            }
        }
    }
}