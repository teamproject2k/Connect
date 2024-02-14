package com.teamproject2k.connect.presentation.base

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.teamproject2k.connect.presentation.utils.SharedPreferenceHelper
import javax.inject.Inject


abstract class BaseViewModel : ViewModel() {
    @Inject
    lateinit var sharedPreference: SharedPreferenceHelper

    @Inject
    lateinit var fireBaseAuth: FirebaseAuth

}