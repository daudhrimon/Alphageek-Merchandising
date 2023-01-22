package com.gdm.alphageek.database

import androidx.room.TypeConverter
import com.gdm.alphageek.data.local.down_sync.Answers
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun answersToJson(list: List<Answers?>?): String? = Gson().toJson(list)

    @TypeConverter
    fun jsonToAnswers(json: String?): List<Answers?>? = Gson().fromJson(json, object : TypeToken<List<Answers?>?>() {}.type)
}