package com.example.eco_plate.ui.storeHome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.repository.CartItem
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.ui.search.SearchProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreHomeViewModel @Inject constructor(
    //private val cartRepository: CartRepository
) : ViewModel() {


}