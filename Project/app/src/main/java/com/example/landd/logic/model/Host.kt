package com.example.landd.logic.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.landd.database.host.StateConverter

//@TypeConverters(
//    StateConverter.class)
enum class State {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    UNAUTHORIZED,
    UNUSED
}

@Entity(tableName="host_table")
data class Host(
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    val ip: String,
    val port: Int,
    val token: String,
    var state: State
)