package com.example.connect.domain.useCase.user

import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateSavedPostsOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(loggedInUserFireBaseId: String, savedPostList: List<String>): Int {
        return repository.updateSavedPostOnLocal(loggedInUserFireBaseId, savedPostList)
    }
}