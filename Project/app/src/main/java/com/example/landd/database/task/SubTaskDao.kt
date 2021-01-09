package com.example.landd.database.task

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.SubTask

@Dao
interface SubTaskDao {
    @Insert
    fun insert(subTask: SubTask): Long

    @Delete
    fun delete(subTask: SubTask)

    @Update
    fun update(subTask: SubTask)

    @Query("SELECT * FROM sub_task_table where taskId=:taskId and hasFinish=0")
    fun findUnFinishedAll(taskId: Long): List<SubTask>

    @Query("SELECT * FROM sub_task_table where taskId=:taskId and hasFinish=1")
    fun findFinishedAll(taskId: Long): List<SubTask>
}