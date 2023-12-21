package com.example.connect.presentation.ui.chat.base_screen

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.connect.domain.models.UsersBean
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.NavGraphs
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.example.connect.presentation.ui.destinations.ChatListScreenDestination
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ChatActivity : BaseActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val chatSharedViewModel: ChatSharedViewModel = hiltViewModel()
            intent.getParcelableExtra("userDetails", UsersBean::class.java)
                ?.let { chatSharedViewModel.setCurrentUser(it) }
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
}