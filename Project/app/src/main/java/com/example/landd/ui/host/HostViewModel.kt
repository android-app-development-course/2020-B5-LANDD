package com.example.landd.ui.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.Host
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HostViewModel : ViewModel() {
    private val _hostList = MutableLiveData<List<Host>>()
    val hostList: LiveData<List<Host>> = Transformations.map(_hostList) { it }

    public suspend fun refreshHostList() {
        _hostList.postValue(AppDataBase.getDatabase().hostDao().findAll())
    }

    public fun refreshHostListInIOThread() {
        GlobalScope.launch(Dispatchers.IO) {
            refreshHostList()
        }
    }
}