package com.example.connect.data.local_db.posts

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPost(postDetails: PostDetails): Long

    @Insert
    fun insertPostList(postDetailsList: List<PostDetails>): LongArray

    @Query("SELECT * FROM PostDetails WHERE fireBaseUserId = :fireBaseId")
    fun getPostList(fireBaseId: String): List<PostDetails>?
}