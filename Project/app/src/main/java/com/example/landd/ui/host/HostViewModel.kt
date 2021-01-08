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
    val hostList = MutableLiveData<List<Host>>()

    public suspend fun refreshHostList() {
        hostList.postValue(AppDataBase.getDatabase().hostDao().findAll())
    }

    public fun refreshHostListInIOThread() {
        GlobalScope.launch(Dispatchers.IO) {
            refreshHostList()
        }
    }
}