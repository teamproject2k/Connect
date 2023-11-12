package com.example.connect.data.local_db

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class AppTypeConverters {

    @TypeConverter
    fun fromFriendList(listOfString: List<String>): String {
        return Gson().toJson(listOfString)
    }

    @TypeConverter
    fun toFriendList(listOfString: String): List<String> {
        return Gson().fromJson(
            listOfString,
            object : TypeToken<List<String?>?>() {}.type
        )
    }
}