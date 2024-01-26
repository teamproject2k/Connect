package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoriesWithUser
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllStoriesWithUserFromLocalUseCase @Inject constructor(
    private val storyRepository: IStoryRepository,
    private val userRepository: IUserRepository
) {
    suspend fun invoke(loggedInUserFirebaseId: String): ArrayList<StoriesWithUser> {
        val storiesList = storyRepository.getAllStoriesFromLocal()
        val userIdList = storiesList.map { it.createdByUserFirebaseId }.toSet().toMutableList()
        val usersList = userRepository.getAllUsersFromIdFromLocal(userIdList)
        val storiesWithUsersList = arrayListOf<StoriesWithUser>()
        val storiesIdToStoryListMap = mutableMapOf<String, ArrayList<StoryBean>>()
        // to show logged in user stories at first place
        if (userIdList.contains(loggedInUserFirebaseId)) {
            storiesIdToStoryListMap[loggedInUserFirebaseId] = arrayListOf()
        }
        storiesList.forEach {
            if (storiesIdToStoryListMap.containsKey(it.createdByUserFirebaseId)) {
                storiesIdToStoryListMap[it.createdByUserFirebaseId]?.add(it)
            } else {
                storiesIdToStoryListMap[it.createdByUserFirebaseId] = arrayListOf(it)
            }
        }
        storiesIdToStoryListMap.forEach {
            val storyPoster = usersList.find { user -> user.firebaseUserId == it.key }
            if (storyPoster != null) {
                storiesWithUsersList.add(StoriesWithUser(storyPoster, it.value))
            }
        }
        return storiesWithUsersList
    }
}