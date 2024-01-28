package com.example.connect.data.local_db

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class AppTypeConverters {

    @TypeConverter
    fun fromMapOfStringStringToString(map: MutableMap<String, String>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMapOfStringStringFromString(string: String): MutableMap<String, String> {
        return Gson().fromJson(
            string,
            object : TypeToken<MutableMap<String, String>>() {}.type
        )
    }

    @TypeConverter
    fun fromListOfStringToString(listOfString: ArrayList<String>): String {
        return Gson().toJson(listOfString)
    }

    @TypeConverter
    fun toListOfStringFromString(stringOfList: String): ArrayList<String> {
        return Gson().fromJson(
            stringOfList,
            object : TypeToken<ArrayList<String>>() {}.type
        )
    }
}