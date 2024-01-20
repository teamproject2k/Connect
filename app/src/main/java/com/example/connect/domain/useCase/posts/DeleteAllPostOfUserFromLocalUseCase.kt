package com.example.connect.domain.useCase.posts

import com.example.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeleteAllPostOfUserFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {

    suspend fun invoke(userFirebaseId: String): Int {
        return repository.deleteAllPostOfUserFromLocal(userFirebaseId)
    }
}