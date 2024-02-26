package com.teamproject2k.connect.domain.use_case.chat

import com.teamproject2k.connect.domain.models.ChatWithUserAndCountBean
import com.teamproject2k.connect.domain.repository.IChatRepository
import javax.inject.Inject

class GetUserWithLastMessageWithUnreadCountFromLocalUseCase @Inject constructor(private val repository: IChatRepository) {
    /**
     * Retrieves a list of ChatWithUserAndCountBean objects representing users with their last message and unread count.
     * This function is invoked as an operator on an instance of a class or function object.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A list of ChatWithUserAndCountBean objects representing users with their last message and unread count.
     * Note: This function is marked as suspend as it might involve asynchronous operations.
     */
    suspend operator fun invoke(loggedInUserFirebaseId: String): List<ChatWithUserAndCountBean> {
        return repository.getUserWithLastMessageWithUnreadCount(loggedInUserFirebaseId)
    }
}