package com.example.landd.database.host

import androidx.room.TypeConverter
import com.example.landd.logic.model.State
import java.util.*

object StateConverter {
    @TypeConverter
    @JvmStatic
    fun toOrdinal(type: State): Int = type.ordinal

    @TypeConverter
    @JvmStatic
    fun toState(ordinal: Int): State = State.values().first { it.ordinal == ordinal }

}