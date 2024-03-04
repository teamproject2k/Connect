package com.teamproject2k.connect.presentation.base

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.teamproject2k.connect.domain.use_case.app.DeleteAllDataFromLocalUseCase
import com.teamproject2k.connect.presentation.ui.auth.AuthenticationActivity
import com.teamproject2k.connect.presentation.utils.SharedPreferenceHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class BaseActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var sharedPreferences: SharedPreferenceHelper

    @Inject
    protected lateinit var deleteAllDataFromLocalUseCase: DeleteAllDataFromLocalUseCase

    fun logout() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                sharedPreferences.isUserDetailsEntered = false
                firebaseAuth.signOut()
                deleteAllDataFromLocalUseCase()
            }
            withContext(Dispatchers.Main) {
                val intent = Intent(this@BaseActivity, AuthenticationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}