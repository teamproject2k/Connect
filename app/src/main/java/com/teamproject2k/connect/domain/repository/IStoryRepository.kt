package com.teamproject2k.connect.domain.repository

import com.teamproject2k.connect.domain.models.StoriesWithUserBean
import com.teamproject2k.connect.domain.models.StoryBean
import com.teamproject2k.connect.domain.models.StorySeenTimeWithUserDetailsBean
import com.teamproject2k.connect.domain.network_utils.ResponseState

interface IStoryRepository {
    /**
     * Adds a story to the remote server.
     *
     * @param story The story to be added.
     * @return A [ResponseState] containing the ID of the added story if successful; otherwise, contains an error message.
     */
    suspend fun addStoryToRemote(story: StoryBean): ResponseState<String>

    /**
     * Retrieves all stories with user details from the remote server.
     *
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing an array list of [StoriesWithUserBean] representing stories with user details.
     *         If successful, returns the list; otherwise, contains an error message.
     */
    suspend fun getAllStoriesWithUserDetailsFromRemote(
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StoriesWithUserBean>>

    /**
     * Adds a user to the seen list of a story on the remote server.
     *
     * @param storyId The ID of the story.
     * @param storySeenBy The Firebase ID of the user who has seen the story.
     * @param storySeenAt The timestamp indicating when the story was seen.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend fun addUserToSeenListInRemote(
        storyId: String,
        storySeenBy: String,
        storySeenAt: Long
    ): ResponseState<Nothing>

    /**
     * Retrieves the list of users who have seen a story from the remote server.
     *
     * @param storyId The ID of the story.
     * @param loggedInUserFirebaseId The Firebase ID of the logged-in user.
     * @return A [ResponseState] containing an array list of [StorySeenTimeWithUserDetailsBean] representing
     *         users who have seen the story along with their details. If successful, returns the list;
     *         otherwise, contains an error message.
     */
    suspend fun getSeenListFromRemote(
        storyId: String,
        loggedInUserFirebaseId: String
    ): ResponseState<ArrayList<StorySeenTimeWithUserDetailsBean>>

    /**
     * Deletes a story from the remote server.
     *
     * @param storyId The ID of the story to be deleted.
     * @return A [ResponseState] representing the result of the operation. If successful, returns `null`;
     *         otherwise, contains an error message.
     */
    suspend fun deleteStoryInRemote(storyId: String): ResponseState<Nothing>

    /**
     * Retrieves all stories from the local database.
     *
     * @return A list of [StoryBean] objects representing all stories stored in the local database.
     */
    suspend fun getAllStoriesFromLocal(): List<StoryBean>

    /**
     * Adds a list of stories to the local database.
     *
     * @param storyList The list of stories to be added to the local database.
     * @return An array of long values representing the IDs of the inserted stories in the local database.
     *         The order of IDs corresponds to the order of stories in the input list.
     */
    suspend fun addAllStoriesToLocal(storyList: List<StoryBean>): LongArray

    /**
     * Deletes all stories from the local database.
     *
     * @return The number of stories deleted from the local database.
     */
    suspend fun deleteAllStoriesFromLocal(): Int

    /**
     * Deletes a story from the local database.
     *
     * @param storyId The ID of the story to be deleted.
     * @return The number of stories deleted from the local database.
     */
    suspend fun deleteStoryFromLocal(storyId: String): Int

    /**
     * Adds a story to the local database.
     *
     * @param story The story to be added to the local database.
     * @return The ID of the inserted story in the local database.
     */
    suspend fun addStoryToLocal(story: StoryBean): Long

    /**
     * Updates a story in the local database.
     *
     * @param story The story to be updated in the local database.
     * @return The number of stories updated in the local database.
     */
    suspend fun updateStoryOnLocal(story: StoryBean): Int
}