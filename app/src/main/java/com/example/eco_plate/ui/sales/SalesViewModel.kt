package com.example.eco_plate.ui.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class SalesViewModel @Inject constructor() : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "This is Store Sales Fragment"
    }
    val text: LiveData<String> = _text
}