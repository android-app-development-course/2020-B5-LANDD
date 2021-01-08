package com.example.landd.database.host

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.Host

@Dao
interface HostDao {
    @Insert
    suspend fun insert(host: Host?)
    @Update
    suspend fun update(host: Host?)
    @Delete
    suspend fun delete(host: Host?)
    @Query("SELECT * FROM host_table")
    suspend fun findAll (): List<Host>
}