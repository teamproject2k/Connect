package com.example.connect.ui.auth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connect.base.BaseActivity
import com.example.connect.ui.theme.ConnectTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.NestedNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : BaseActivity() {
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            ConnectTheme {
                val navHostEngine = rememberAnimatedNavHostEngine(
                    rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
                    defaultAnimationsForNestedNavGraph = mapOf(
                        NavGraphs.authentication to NestedNavGraphDefaultAnimations(
                            enterTransition = { fadeIn(animationSpec = tween(2000)) },
                            exitTransition = { fadeOut(animationSpec = tween(2000)) }
                        ),
                    ) // all other nav graphs not specified in this map, will get their animations from the `rootDefaultAnimations` above.
                )
                val navController = rememberAnimatedNavController()
                DestinationsNavHost(navGraph = NavGraphs.authentication, engine = rememberAnimatedNavHostEngine(), navController = navController)
            }
        }
    }
}