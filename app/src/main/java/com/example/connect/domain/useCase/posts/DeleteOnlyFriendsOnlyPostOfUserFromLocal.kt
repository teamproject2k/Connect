package com.example.connect.domain.useCase.posts

import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteOnlyFriendsOnlyPostOfUserFromLocal @Inject constructor(private val repository: IPostRepository) {
    suspend fun invoke(userFireBaseId: String): Int {
        return repository.deleteAllPostOfUserWithFriendsOnlyVisibilityFromLocal(userFireBaseId)
    }
}