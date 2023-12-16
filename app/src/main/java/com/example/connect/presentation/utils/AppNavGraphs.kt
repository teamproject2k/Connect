package com.example.connect.presentation.utils

import com.ramcosta.composedestinations.annotation.NavGraph

@NavGraph
annotation class AuthenticationNavGraph(
    val start: Boolean = false
)


@NavGraph
annotation class HomeNavGraph(
    val start: Boolean = false
)


@NavGraph
annotation class ChatNavGraph(
    val start: Boolean = false
)