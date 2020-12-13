package com.example.landd.database.host

import com.example.landd.logic.model.Host

class HostRepository (private val dao: HostDao) {
    val subscribers = dao.findAll()

    suspend fun insert(host: Host){
        return dao.insert(host)
    }

    suspend fun update(host: Host){
        return dao.update(host)
    }

    suspend fun delete(host: Host){
        return dao.delete(host)
    }
}