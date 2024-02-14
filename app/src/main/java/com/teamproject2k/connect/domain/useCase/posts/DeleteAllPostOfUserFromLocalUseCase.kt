package com.teamproject2k.connect.domain.useCase.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostOfUserFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend operator fun invoke(userFirebaseId: String): Int {
        return repository.deleteAllPostOfUserFromLocal(userFirebaseId)
    }
}