package com.example.eco_plate.ui.foodFeed

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FoodFeedViewModel @Inject constructor() : ViewModel() {
    // Add search-related state here
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

