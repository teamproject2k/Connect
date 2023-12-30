package com.example.connect.presentation.services

import com.example.connect.domain.useCase.fcm.SendTokenToRemoteUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
open class MessagingService : FirebaseMessagingService() {

    @Inject
    protected lateinit var sendTokenToRemoteUseCase: SendTokenToRemoteUseCase

    @Inject
    protected lateinit var firebaseAuth: FirebaseAuth


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val currentUser = firebaseAuth.currentUser ?: return
        CoroutineScope(Dispatchers.IO).launch {
            sendTokenToRemoteUseCase.invoke(currentUser.uid, token)
        }
    }

}