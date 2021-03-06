package com.example.landd.database.task

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.Task

@Dao
interface TaskDao {
    @Insert
    fun insert(task: Task?): Long

    @Delete
    fun delete(task: Task?)

    @Update
    fun update(task: Task)

    @Query("SELECT * FROM task_table where has_finish=0")
    fun findUnFinishedAll(): List<Task>

    @Query("SELECT * FROM task_table where has_finish=1")
    fun findFinishedAll(): List<Task>
}