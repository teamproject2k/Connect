package com.teamproject2k.connect.domain.use_case.story

import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.repository.IStoryRepository
import com.teamproject2k.connect.domain.repository.IUserRepository
import javax.inject.Inject

class GetAllStoriesWithUserFromLocalUseCase @Inject constructor(
    private val storyRepository: IStoryRepository,
    private val userRepository: IUserRepository
) {
    /**
     * Suspended function to retrieve a list of stories along with user details for the logged-in user from the local repository.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return An [ArrayList] of [StoriesWithUserBean] containing stories along with user details.
     */
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