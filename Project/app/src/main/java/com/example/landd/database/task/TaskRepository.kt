package com.example.landd.database.task

import com.example.landd.logic.model.Task

class TaskRepository (private val dao: TaskDao) {

    val finishedTask = dao.findFinishedAll()

    val unfinishedTask = dao.findUnFinishedAll()

    suspend fun insert(task: Task){
        return dao.insert(task)
    }

    suspend fun delete(task: Task){
        return dao.delete(task)
    }
}