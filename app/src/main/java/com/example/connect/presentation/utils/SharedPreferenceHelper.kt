package com.example.connect.presentation.utils

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val IsUserDetailEntered = "is_user_detail_entered"
    }

    var isUserDetailsEntered
        get() = sharedPreferences.getBoolean(IsUserDetailEntered, false)
        set(isEntered) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(IsUserDetailEntered, isEntered)
            editor.apply()
        }
}