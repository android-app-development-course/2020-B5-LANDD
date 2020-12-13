package com.example.landd.database.task

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.Task

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDataBases : RoomDatabase() {
    abstract val taskDao: TaskDao
    // 通过伴生对象实现单例模式
    companion object {
        @Volatile
        private var INSTANCE: TaskDataBases? = null
        fun getInstance(context: Context): TaskDataBases {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TaskDataBases::class.java,
                        "task_table"
                    ).build()
                }
                return instance
            }
        }
    }

}