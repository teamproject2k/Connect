package com.example.connect.utils

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@NavGraph
annotation class AuthenticationNavGraph(
    val start: Boolean = false
)