package com.example.landd.database

import androidx.room.*
import com.example.landd.LANDDApplication
import com.example.landd.database.host.HostDao
import com.example.landd.database.host.StateConverter
import com.example.landd.database.task.SubTaskDao
import com.example.landd.database.task.TaskDao
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.SubTask
import com.example.landd.logic.model.Task

@Database(entities = [Host::class, Task::class, SubTask::class], version = 1, exportSchema = false)
@TypeConverters(StateConverter::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun hostDao(): HostDao
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao

    companion object {

        private var instance: AppDataBase? = null

        @Synchronized
        fun getDatabase(): AppDataBase {
            return instance ?: Room.databaseBuilder(
                LANDDApplication.context,
                AppDataBase::class.java,
                "app_database"
            ).build().apply { instance = this }
        }
    }
}