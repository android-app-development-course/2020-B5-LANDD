package com.example.landd.database.task

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.SubTask

@Dao
interface SubTaskDao {
    @Insert
    suspend fun insert(subTask: SubTask)

    @Delete
    suspend fun delete(subTask: SubTask)

    @Update
    suspend fun update(subTask: SubTask)

    @Query("SELECT * FROM sub_task_table where taskId=:taskId and hasFinish=0")
    fun findUnFinishedAll(taskId: Int): List<SubTask>

    @Query("SELECT * FROM sub_task_table where taskId=:taskId and hasFinish=1")
    fun findFinishedAll(taskId: Int): List<SubTask>
}