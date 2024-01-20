package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdatedSavedPostsOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend fun invoke(loggedInUserFireBaseId: String, savedPostList: List<String>): Int {
        return repository.updateSavedPostOnLocal(loggedInUserFireBaseId, savedPostList)
    }
}