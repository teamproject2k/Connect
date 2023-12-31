package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.models.UsersBean
import com.example.connect.domain.network_request_response.ResponseState
import com.example.connect.domain.repository.IStoryRepository
import javax.inject.Inject

class GetStoryDetailsWithUserDetailsUseCase @Inject constructor(private val repository: IStoryRepository) {
    suspend fun invoke(currentUserFirebaseId: String): ResponseState<Pair<MutableMap<String, ArrayList<StoryBean>>, ArrayList<UsersBean>>> {
        return repository.getAllStoriesWithUserDetailsFromRemote(currentUserFirebaseId)
    }
}