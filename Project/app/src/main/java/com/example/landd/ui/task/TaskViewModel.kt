package com.example.landd.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.landd.database.AppDataBase
import com.example.landd.logic.model.Host
import com.example.landd.logic.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    val downloadingTaskList = MutableLiveData<List<Task>>()
    val finishedTaskList = MutableLiveData<MutableList<Task>>()

    public suspend fun refreshTaskList() {
        downloadingTaskList.postValue(AppDataBase.getDatabase().taskDao().findUnFinishedAll())
        val list = mutableListOf<Task>()
        list.addAll(AppDataBase.getDatabase().taskDao().findFinishedAll())
        finishedTaskList.postValue(list)
    }

    public fun refreshTaskListInIOThread() {
        GlobalScope.launch(Dispatchers.IO) {
            refreshTaskList()
        }
    }

}