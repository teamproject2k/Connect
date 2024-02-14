package com.teamproject2k.connect.domain.useCase.posts

import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class DeletePostFromLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(postFirebaseId: String) {
        repository.deletePostFromLocal(postFirebaseId)
    }
}