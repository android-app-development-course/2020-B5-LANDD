package com.example.landd.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

//@TypeConverters(
//    StateConverter.class)
enum class State {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    UNAUTHORIZED,
    UNUSED
}

@Entity(tableName = "host_table")
data class Host(
    val ip: String,
    val port: Int,
    var username: String,
    var password: String,
    var state: State
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}