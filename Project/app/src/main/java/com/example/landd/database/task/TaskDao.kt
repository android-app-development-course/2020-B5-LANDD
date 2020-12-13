package com.example.landd.database.task

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.Task

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task?)

    @Delete
    suspend fun delete(task: Task?)

    @Query("SELECT * FROM task_table where has_finish=0")
    fun findUnFinishedAll (): LiveData<List<Task>>

    @Query("SELECT * FROM task_table where has_finish=1")
    fun findFinishedAll (): LiveData<List<Task>>
}