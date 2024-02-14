package com.teamproject2k.connect.presentation.ui.home.base_screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.teamproject2k.connect.presentation.base.BaseActivity
import com.teamproject2k.connect.presentation.services.fcm.NotificationTypesEnum
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.theme.ConnectTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val screenToNavigate = intent.getStringExtra(NotificationTypesEnum::class.simpleName)
        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                ConnectTheme {
                    HomeActivityScreen(screenToNavigate ?: "")
                }
            }
        }
    }
}