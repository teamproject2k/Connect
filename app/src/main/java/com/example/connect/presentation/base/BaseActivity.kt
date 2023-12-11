package com.example.connect.presentation.base

import android.content.Intent
import androidx.activity.ComponentActivity
import com.example.connect.presentation.ui.auth.AuthenticationActivity
import com.example.connect.presentation.utils.SharedPreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

abstract class BaseActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper


    fun logout() {
        sharedPreferences.isUserDetailsEntered = false
        firebaseAuth.signOut()
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}