package com.teamproject2k.connect.presentation.ui.chat.base_screen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import com.ramcosta.composedestinations.DestinationsNavHost
import com.teamproject2k.connect.presentation.base.BaseActivity
import com.teamproject2k.connect.presentation.base.BaseApp
import com.teamproject2k.connect.presentation.ui.NavGraphs
import com.teamproject2k.connect.presentation.ui.common.LocalActivity
import com.teamproject2k.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.teamproject2k.connect.presentation.ui.destinations.ChatListScreenDestination
import com.teamproject2k.connect.presentation.ui.theme.ConnectTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaseApp.INSTANCE?.isChatScreenOpen = true
        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                Surface {
                    ConnectTheme {
                        DestinationsNavHost(
                            navGraph = NavGraphs.chat,
                            engine = getAnimatedNavHostEngine(),
                            startRoute = ChatListScreenDestination
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseApp.INSTANCE?.isChatScreenOpen = false
    }
}