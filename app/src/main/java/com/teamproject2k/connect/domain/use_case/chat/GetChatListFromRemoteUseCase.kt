package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.UserWithChatListBean
import com.teamproject2k.connect.domain.network_utils.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetChatListFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Invokes the function to retrieve a list of chats for the logged-in user from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing an array list of [UserWithChatListBean] objects representing
     *         users along with their chat lists. If successful, returns the list; otherwise, contains an error message.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>> {
        return repository.getChatListFromRemote(loggedInUserFirebaseId)
    }
}