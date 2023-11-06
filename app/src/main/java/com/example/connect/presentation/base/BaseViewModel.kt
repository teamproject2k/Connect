package com.example.connect.presentation.base

import androidx.lifecycle.ViewModel
import com.example.connect.presentation.utils.SharedPreferenceHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
open class BaseViewModel @Inject constructor() : ViewModel() {
    @Inject
    lateinit var sharedPreference: SharedPreferenceHelper

    @Inject
    lateinit var fireBaseAuth: FirebaseAuth
}