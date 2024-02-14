package com.teamproject2k.connect.domain.useCase.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteOnlyFriendsOnlyPostOfUserFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(userFireBaseId: String): Int {
        return repository.deleteAllPostOfUserWithFriendsOnlyVisibilityFromLocal(userFireBaseId)
    }
}