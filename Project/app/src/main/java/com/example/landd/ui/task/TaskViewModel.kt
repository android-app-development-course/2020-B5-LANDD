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
    val downloadingTaskList = MutableLiveData<MutableList<Task>>()
    val finishedTaskList = MutableLiveData<MutableList<Task>>()

    public fun refreshTaskList() {
        val downloadingTaskList = mutableListOf<Task>()
        downloadingTaskList.addAll(AppDataBase.getDatabase().taskDao().findUnFinishedAll())
        this.downloadingTaskList.postValue(downloadingTaskList)
        val finishedTaskList = mutableListOf<Task>()
        finishedTaskList.addAll(AppDataBase.getDatabase().taskDao().findFinishedAll())
        this.finishedTaskList.postValue(finishedTaskList)
    }

    public fun refreshTaskListInIOThread() {
        GlobalScope.launch(Dispatchers.IO) {
            refreshTaskList()
        }
    }

}