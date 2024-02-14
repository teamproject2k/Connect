package com.teamproject2k.connect.domain.use_case.user

import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class UpdateSavedPostsOnLocalUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(loggedInUserFireBaseId: String, savedPostList: List<String>): Int {
        return repository.updateSavedPostOnLocal(loggedInUserFireBaseId, savedPostList)
    }
}