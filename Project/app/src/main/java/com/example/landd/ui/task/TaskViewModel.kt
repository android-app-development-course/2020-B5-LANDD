package com.example.landd.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.landd.logic.model.Task

class TaskViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is task Fragment"
    }
    val text: LiveData<String> = _text
    //DownLoad
    private val download: MutableLiveData<Task> = MutableLiveData<Task>()
    fun setDownLoad(item: Task) {
        download.setValue(item)
    }
    fun getDownLoad(): LiveData<Task>? {
        return download
    }

    //Finish
    private val finish: MutableLiveData<Task> = MutableLiveData<Task>()
    fun setFinish(item: Task) {
        finish.setValue(item)
    }
    fun getFinish(): LiveData<Task>? {
        return finish
    }
}