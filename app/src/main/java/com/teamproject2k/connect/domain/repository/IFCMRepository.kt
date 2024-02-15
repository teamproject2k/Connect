package com.teamproject2k.connect.domain.repository

import com.teamproject2k.connect.domain.network_request_response.ResponseState

interface IFCMRepository {

    /**
     * Gets the Firebase Cloud Messaging (FCM) token.
     *
     * @return A [ResponseState] containing the FCM token or an error.
     */
    suspend fun getFCMToken(): ResponseState<String>


    /**
     * Sends a FCM message to a specific token.
     *
     * @param token The token of current user to validate the request.
     * @param data The data to send with the message.
     * @param sendTo FCM token whom to send the message.
     *
     * @return A [ResponseState] containing the result of the operation.
     */
    suspend fun sendFCMMessage(
        token: String,
        data: Map<String, String>,
        sendTo: String
    ): ResponseState<Nothing>
}
