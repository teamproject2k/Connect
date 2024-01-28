package com.example.connect.domain.useCase.story

import com.example.connect.domain.models.StoriesWithUserBean
import com.example.connect.domain.models.StoryBean
import com.example.connect.domain.repository.IStoryRepository
import com.example.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllStoriesWithUserFromLocalUseCase @Inject constructor(
    private val storyRepository: IStoryRepository,
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(loggedInUserFirebaseId: String): ArrayList<StoriesWithUserBean> {
        val storiesList =
            storyRepository.getAllStoriesFromLocal().sortedByDescending { it.createdAt }
        val userIdList = storiesList.map { it.createdByUserFirebaseId }.toSet().toMutableList()
        val usersList = userRepository.getAllUsersFromIdsFromLocal(userIdList)
        val storiesWithUsersListBean = arrayListOf<StoriesWithUserBean>()
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
                storiesWithUsersListBean.add(StoriesWithUserBean(storyPoster, it.value))
            }
        }
        storiesWithUsersListBean.forEach {
            it.storiesList.reverse()
        }
        return storiesWithUsersListBean
    }
}