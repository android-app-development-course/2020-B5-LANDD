package com.example.landd.database.host

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.landd.logic.model.Host

@Database(entities = [Host::class], version = 1,exportSchema = false)
@TypeConverters(StateConverter::class)
abstract class HostDataBases : RoomDatabase() {
    abstract val hostDao: HostDao
    // 通过伴生对象实现单例模式
    companion object {
        @Volatile
        private var INSTANCE: HostDataBases? = null
        fun getInstance(context: Context): HostDataBases {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HostDataBases::class.java,
                        "host_table"
                    ).build()
                }
                return instance
            }
        }
    }
}