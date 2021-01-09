package com.example.landd.database.host

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.State

@Dao
interface HostDao {
    @Insert
    fun insert(host: Host?): Long

    @Update
    fun update(host: Host?)

    @Delete
    fun delete(host: Host?)

    @Query("SELECT * FROM host_table")
    suspend fun findAll(): List<Host>

    @Query("select * from host_table where state=0")
    fun getAllValid(): List<Host>
}