package com.teamproject2k.connect.presentation.base

import android.content.Intent
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.teamproject2k.connect.presentation.ui.auth.AuthenticationActivity
import com.teamproject2k.connect.presentation.utils.SharedPreferenceHelper
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