package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.UserWithChatListBean
import com.teamproject2k.connect.domain.network_request_response.ResponseState
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetChatListFromRemoteUseCase @Inject constructor(private val repository: IChatRepository) {

    suspend operator fun invoke(loggedInUserFirebaseId: String): ResponseState<ArrayList<UserWithChatListBean>> {
        return repository.getChatListFromRemote(loggedInUserFirebaseId)
    }
}