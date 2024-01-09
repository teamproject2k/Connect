package com.example.connect.data.local_db.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.connect.data.models.post.PostDbEntity

@Dao
interface IPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(postDetails: PostDbEntity): Long

    @Insert
    fun insertPostList(postDetailsList: List<PostDbEntity>): LongArray

    @Query("SELECT * FROM PostDbEntity WHERE createdByUserFirebaseId = :createdByUserFirebaseId ORDER BY createdAt DESC")
    fun getPostList(createdByUserFirebaseId: String): List<PostDbEntity>
}