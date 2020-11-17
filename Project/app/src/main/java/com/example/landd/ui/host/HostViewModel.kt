package com.example.landd.ui.host

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HostViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is host Fragment"
    }
    val text: LiveData<String> = _text
}