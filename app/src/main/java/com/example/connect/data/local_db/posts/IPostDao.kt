package com.example.connect.data.local_db.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.connect.data.models.post.PostLocalEntity
import com.example.connect.data.models.post.PostWithUserDetailsFromLocalEntity
import com.example.connect.domain.utils.VisibilityScopeEnum

@Dao
interface IPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(postDetails: PostLocalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPostList(postDetailsList: List<PostLocalEntity>): LongArray

    @Query("SELECT * FROM posts WHERE createdByUserFirebaseId = :createdByUserFirebaseId AND whetherDeleted=0 ORDER BY createdAt DESC")
    fun getPostList(createdByUserFirebaseId: String): List<PostLocalEntity>


    @Transaction
    @Query("SELECT * FROM posts WHERE postFirebaseId IN (:savedPostFirebaseIds) ORDER BY createdAt DESC")
    fun getSavedPostsAndUsers(savedPostFirebaseIds: List<String>): List<PostWithUserDetailsFromLocalEntity>

    @Transaction
    @Query("SELECT * FROM posts WHERE whetherDeleted=0 ORDER BY createdAt DESC")
    fun getPostDetailsWithUsers(): List<PostWithUserDetailsFromLocalEntity>

    @Update
    fun updatePostDetails(postDetails: PostLocalEntity): Int


    @Query("DELETE FROM posts WHERE postFirebaseId = :postFirebaseId")
    fun deletePost(postFirebaseId: String): Int


    @Query("DELETE FROM posts")
    fun deleteAllPosts(): Int


    @Query("DELETE FROM posts WHERE createdByUserFirebaseId= :userFirebaseId")
    fun deleteAllPostOfUser(userFirebaseId: String): Int

    @Query("DELETE FROM posts WHERE createdByUserFirebaseId = :userFirebaseId AND postVisibilityScope = :visibilityScope")
    fun deleteOnlyFriendsOnlyPostOfUser(
        userFirebaseId: String,
        visibilityScope: String = VisibilityScopeEnum.FriendsOnly.name
    ): Int
}