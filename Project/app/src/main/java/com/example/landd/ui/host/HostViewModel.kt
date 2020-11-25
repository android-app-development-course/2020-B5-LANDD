package com.example.landd.ui.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.landd.logic.model.Host

class HostViewModel : ViewModel() {
    val hostList = ArrayList<Host>()
}