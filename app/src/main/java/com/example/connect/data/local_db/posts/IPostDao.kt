package com.example.connect.data.local_db.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.connect.data.models.post.PostDbEntity
import com.example.connect.data.models.post.PostWithUserDetailsFromLocal

@Dao
interface IPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(postDetails: PostDbEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPostList(postDetailsList: List<PostDbEntity>): LongArray

    @Query("SELECT * FROM posts WHERE createdByUserFirebaseId = :createdByUserFirebaseId AND whetherDeleted=0 ORDER BY createdAt DESC")
    fun getPostList(createdByUserFirebaseId: String): List<PostDbEntity>


    @Transaction
    @Query("SELECT * FROM posts WHERE postFirebaseId IN (:savedPostIds) ORDER BY createdAt DESC")
    fun getSavedPostsAndUsers(savedPostIds: List<String>): List<PostWithUserDetailsFromLocal>

    @Transaction
    @Query("SELECT * FROM posts WHERE whetherDeleted=0 ORDER BY createdAt DESC")
    fun getPostDetailsWithUsers(): List<PostWithUserDetailsFromLocal>

    @Update
    fun updatePostDetails(postDetails: PostDbEntity): Int


    @Query("DELETE FROM posts WHERE postFirebaseId = :postFirebaseId")
    fun deletePost(postFirebaseId: String): Int


    @Query("DELETE FROM posts")
    fun deleteAllPosts(): Int
}