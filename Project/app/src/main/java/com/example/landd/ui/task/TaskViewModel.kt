package com.example.landd.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.landd.database.task.TaskRepository
import com.example.landd.logic.model.Task

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

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

    /*
        数据库增删改查时由于不能在主线程进行，采用协程suspend，调用时写成这样：
        GlobalScope.launch(Dispatchers.Main) {
                            taskViewModel.insert(task!!) }
    */
    suspend fun insert(task: Task){
        return repository.insert(task)
    }

    suspend fun delete(task: Task){
        return repository.delete(task)
    }
    fun getFinishedList():LiveData<List<Task>>{
        return repository.finishedTask
    }
    fun getUnFinishedList():LiveData<List<Task>>{
        return repository.unfinishedTask
    }
}