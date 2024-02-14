package com.teamproject2k.connect.domain.useCase.chat

import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetUserWithLastMessageWithUnreadCountFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    suspend operator fun invoke(loggedInUserFirebaseId: String): List<ChatWithUserAndCountBean> {
        return repository.getUserWithLastMessageWithUnreadCount(loggedInUserFirebaseId)
    }
}