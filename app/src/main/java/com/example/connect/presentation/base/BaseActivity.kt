package com.example.connect.presentation.base

import androidx.activity.ComponentActivity
import com.example.connect.presentation.utils.SharedPreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

abstract class BaseActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper
}