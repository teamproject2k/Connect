package com.example.connect.data.repository

import com.example.connect.data.remote.IRemoteRepository
import com.example.connect.data.utils.FCMConstantHelper
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IFCMRepository
import com.example.connect.domain.utils.FirebaseErrorCodes
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import retrofit2.await
import javax.inject.Inject


class IFCMRepositoryImpl @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val remoteRepository: IRemoteRepository
) : IFCMRepository {


    override suspend fun getFCMToken(): ResponseState<String> {
        return try {
            val token = firebaseMessaging.token.await()
            if (token != null) {
                ResponseState.success(token)
            } else {
                ResponseState.error(FirebaseErrorCodes.FCM_TOKEN_NOT_GENERATED)
            }
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun sendFCMMessage(
        token: String,
        data: Map<String, Any>,
        sendTo: String
    ): ResponseState<Nothing> {
        return try {
            val dataJson = JsonObject()
            data.forEach {
                dataJson.add(it.key, it.value as JsonElement?)
            }
            val messageJson = JsonObject()
            messageJson.addProperty(FCMConstantHelper.TOKEN_KEY, sendTo)
            messageJson.add(FCMConstantHelper.DATA_KEY, dataJson)
            val parentJson = JsonObject()
            parentJson.add(FCMConstantHelper.MESSAGE_KEY, messageJson)
            remoteRepository.sendFcmMessage(
                token = token,
                data = parentJson
            ).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}