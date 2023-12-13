package com.example.connect.presentation.ui.home.base_screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.theme.ConnectTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                ConnectTheme {
                    HomeActivityScreen()
                }
            }
        }
    }

}