package com.teamproject2k.connect.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.teamproject2k.connect.data.remote.IRemoteRepository
import com.teamproject2k.connect.data.utils.FCMConstantHelper
import com.teamproject2k.connect.domain.logger.LoggingHelper
import com.teamproject2k.connect.domain.logger.LoggingLevelEnum
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IFCMRepository
import com.teamproject2k.connect.domain.utils.FirebaseErrorCodes
import com.teamproject2k.connect.presentation.utils.ConstantsHelper
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
            // If the token is not null, return it as a success response.
            if (token != null) {
                ResponseState.success(token)
            } else {
                // Otherwise, return an error response.
                ResponseState.error(FirebaseErrorCodes.FCM_TOKEN_NOT_GENERATED)
            }
        } catch (exception: Exception) {
            // If an exception occurs, return an error response with the exception message.
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

    override suspend fun sendFCMMessage(
        token: String,
        data: Map<String, String>,
        sendTo: String
    ): ResponseState<Nothing> {
        return try {
            // Create a JSON object with the message data.
            val dataJson = JsonObject()
            data.forEach {
                // Add each key-value pair to the JSON object.
                dataJson.addProperty(it.key, it.value)
            }
            // Create a JSON object with the message recipient.
            val messageJson = JsonObject()
            messageJson.addProperty(FCMConstantHelper.TOKEN_KEY, sendTo)
            messageJson.add(FCMConstantHelper.DATA_KEY, dataJson)
            // Create a JSON object with the entire message.
            val parentJson = JsonObject()
            parentJson.add(FCMConstantHelper.MESSAGE_KEY, messageJson)
            // Send the message to the FCM server.
            remoteRepository.sendFcmMessage(
                token = token,
                data = parentJson
            ).await()
            ResponseState.success(null)
        } catch (exception: Exception) {
            LoggingHelper.logData(
                LoggingLevelEnum.Error,
                ConstantsHelper.ERROR_TAG,
                "sendFCMMessage",
                exception.localizedMessage ?: ""
            )
            ResponseState.error(exception.localizedMessage ?: "")
        }
    }

}