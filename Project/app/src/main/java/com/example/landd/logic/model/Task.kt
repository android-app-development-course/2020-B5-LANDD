package com.example.landd.logic.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="task_table")
data class Task (
    @PrimaryKey(autoGenerate = true)
    var id:Int,
    var file_name:String,
    var file_size:String,
    var file_type:String,
    var finish_time:String,
    var has_finish:Boolean
)