package com.example.landd.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is task Fragment"
    }
    val text: LiveData<String> = _text
    //DownLoad
    private val download: MutableLiveData<DownLoadEntity> = MutableLiveData<DownLoadEntity>()
    fun setDownLoad(item: DownLoadEntity) {
        download.setValue(item)
    }
    fun getDownLoad(): LiveData<DownLoadEntity>? {
        return download
    }

    //Finish
    private val finish: MutableLiveData<FinishEntity> = MutableLiveData<FinishEntity>()
    fun setFinish(item: FinishEntity) {
        finish.setValue(item)
    }
    fun getFinish(): LiveData<FinishEntity>? {
        return finish
    }
}