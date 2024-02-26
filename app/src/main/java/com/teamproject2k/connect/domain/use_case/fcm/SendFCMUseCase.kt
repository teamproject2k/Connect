package com.teamproject2k.connect.domain.use_case.fcm

import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IFCMRepository
import javax.inject.Inject

class SendFCMUseCase @Inject constructor(private val repository: IFCMRepository) {
    /**
     * Sends a Firebase Cloud Messaging (FCM) message with the specified token, data, and recipient.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param token The FCM token used to send the message.
     * @param data The data to be included in the FCM message.
     * @param sendTo The recipient of the FCM message.
     * @return A ResponseState representing the result of the operation:
     *         - ResponseState.Success if the FCM message was sent successfully.
     *         - ResponseState.Error with an error message if the operation failed.
     *         - ResponseState.Loading if the operation is in progress.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(
        token: String,
        data: Map<String, String>,
        sendTo: String
    ): ResponseState<Nothing> {
        return repository.sendFCMMessage(token, data, sendTo)
    }
}