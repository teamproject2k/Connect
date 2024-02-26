package com.teamproject2k.connect.domain.use_case.posts

import com.teamproject2k.connect.domain.models.PostBean
import com.teamproject2k.connect.domain.repository.IPostRepository
import javax.inject.Inject

class UpdatePostDetailsOnLocalUseCase @Inject constructor(private val repository: IPostRepository) {
    suspend operator fun invoke(postDetails: PostBean): Int {
        return repository.updatePostDetailsOnLocal(postDetails)
    }
}