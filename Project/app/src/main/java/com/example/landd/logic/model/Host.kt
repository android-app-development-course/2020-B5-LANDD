package com.example.landd.logic.model

enum class State {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    UNAUTHORIZED,
    UNUSED
}

data class Host(
    val ip: String,
    val port: Int,
    val token: String,
    var state: State
)