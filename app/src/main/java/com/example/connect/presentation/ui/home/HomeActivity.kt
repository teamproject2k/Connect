package com.example.connect.presentation.ui.home

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.connect.presentation.base.BaseActivity
import com.example.connect.presentation.ui.NavGraphs
import com.example.connect.presentation.ui.common.LocalActivity
import com.example.connect.presentation.ui.common.getAnimatedNavHostEngine
import com.example.connect.presentation.ui.models.BottomAppBarItemData
import com.example.connect.presentation.ui.theme.ConnectTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                ConnectTheme {
                    var selectedIndex by rememberSaveable {
                        mutableIntStateOf(0)
                    }
//                    Scaffold(bottomBar = {
//                        NavigationBar {
//                            getBottomNavBarItemList().forEachIndexed { index, data ->
//                                NavigationBarItem(
//                                    selected = selectedIndex == index,
//                                    onClick = {
//                                        selectedIndex = index
//                                    },
//                                    icon = {
//                                        Image(
//                                            imageVector = if (selectedIndex == index) data.selectedIcon else data.unSelectedIcon,
//                                            contentDescription = data.text
//                                        )
//                                    },
////                                    label = { Text(text = data.text) },
//                                )
//                            }
//                        }
//                    }) {
//
//                    }
                    DestinationsNavHost(
                        navGraph = NavGraphs.home,
                        engine = getAnimatedNavHostEngine()
                    )
                }
            }
        }
    }


    private fun getBottomNavBarItemList(): ArrayList<BottomAppBarItemData> {
        val bottomNavList = arrayListOf<BottomAppBarItemData>()
        bottomNavList.add(BottomAppBarItemData("Home", Icons.Filled.Home, Icons.Outlined.Home))
        bottomNavList.add(
            BottomAppBarItemData(
                "Add Post",
                Icons.Filled.AddCircle,
                Icons.Outlined.AddCircle
            )
        )
        bottomNavList.add(
            BottomAppBarItemData(
                "Profile",
                Icons.Filled.Person,
                Icons.Outlined.Person
            )
        )
        return bottomNavList
    }
}