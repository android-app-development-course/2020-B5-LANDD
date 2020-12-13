package com.example.landd.ui.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.landd.database.host.HostRepository
import com.example.landd.logic.model.Host

class HostViewModel(private val repository: HostRepository): ViewModel() {
    val hostList = ArrayList<Host>()
    /*
        数据库增删改查时由于不能在主线程进行，采用协程suspend，调用时写成这样：
        GlobalScope.launch(Dispatchers.Main) {
                            hostViewModel.insert(host!!) }
    */
    suspend fun insert(host: Host){
        return repository.insert(host)
    }

    suspend fun update(host: Host){
        return repository.update(host)
    }

    suspend fun delete(host: Host){
        return repository.delete(host)
    }
    fun getHostList():LiveData<List<Host>>{
        return repository.subscribers
    }
}