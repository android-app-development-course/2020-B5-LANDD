package com.example.landd.ui.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.landd.database.host.HostRepository

class HostViewModelFactory (private val repository: HostRepository):
    ViewModelProvider.Factory  {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HostViewModel::class.java)){
            return HostViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel类型不匹配")
    }
}