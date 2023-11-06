package com.example.connect.presentation.utils

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@NavGraph
annotation class AuthenticationNavGraph(
    val start: Boolean = false
)