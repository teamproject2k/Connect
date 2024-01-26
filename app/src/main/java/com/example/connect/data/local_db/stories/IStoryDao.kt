package com.example.connect.data.local_db.stories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.connect.data.models.story.StoryDbEntity

@Dao
interface IStoryDao {

    @Query("SELECT * FROM STORIES WHERE whetherDeleted =0")
    fun getAllStories(): List<StoryDbEntity>

    @Query("DELETE FROM stories")
    fun deleteAllStories(): Int

    @Query("DELETE FROM stories WHERE storyFirebaseId =:storyFirebaseId")
    fun deleteStory(storyFirebaseId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllStories(storiesList: List<StoryDbEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStory(storyDbEntity: StoryDbEntity): Long

}