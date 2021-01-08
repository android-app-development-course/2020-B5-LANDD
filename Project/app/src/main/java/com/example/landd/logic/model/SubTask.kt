package com.example.landd.logic.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "sub_task_table")
data class SubTask(
    @ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )
    var taskId: Int, var start: Int, var end: Int, var hasFinish: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}