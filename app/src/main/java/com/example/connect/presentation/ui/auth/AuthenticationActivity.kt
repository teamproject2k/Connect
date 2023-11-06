package com.example.connect.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.auth.destinations.UserDetailsScreenDestination
import com.example.connect.presentation.ui.home.HomeActivity
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.example.connect.presentation.utils.ConstantsHelper
import com.example.connect.presentation.utils.LocalActivity
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine

class AuthenticationActivity : BaseActivity() {
    companion object {
        var Instance: AuthenticationActivity? = null
    }

    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (firebaseAuth.currentUser != null && sharedPreferences.isUserDetailsEntered) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Instance = this
            setContent {
                CompositionLocalProvider(LocalActivity provides this) {
                    ConnectTheme {
                        val navHostEngine = rememberAnimatedNavHostEngine(
                            rootDefaultAnimations = RootNavGraphDefaultAnimations(
                                enterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(ConstantsHelper.NavigationAnimationDuration)
                                    )
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(ConstantsHelper.NavigationAnimationDuration)
                                    )
                                },
                                popEnterTransition = {
                                    slideIntoContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(ConstantsHelper.NavigationAnimationDuration)
                                    )
                                },
                                popExitTransition = {
                                    slideOutOfContainer(
                                        AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(ConstantsHelper.NavigationAnimationDuration)
                                    )
                                }
                            ),
                        )
                        DestinationsNavHost(
                            navGraph = NavGraphs.authentication,
                            engine = navHostEngine
                        )

                        if (firebaseAuth.currentUser != null) {
                            DestinationsNavHost(
                                navGraph = NavGraphs.authentication,
                                engine = navHostEngine,
                                startRoute = UserDetailsScreenDestination
                            )
                        } else {
                            DestinationsNavHost(
                                navGraph = NavGraphs.authentication,
                                engine = navHostEngine
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Instance = null
    }
}