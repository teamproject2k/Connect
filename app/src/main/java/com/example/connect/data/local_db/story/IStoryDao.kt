package com.example.connect.data.local_db.story

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.connect.data.models.story.StoryDbEntity

@Dao
interface IStoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStory(storyDetails: StoryDbEntity): Long
}