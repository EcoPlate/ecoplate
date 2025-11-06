package com.example.eco_plate.ui.pantry

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor() : ViewModel() {
    //Temporary
    private val _items = MutableStateFlow<List<PantryItem>>(listOf(
        PantryItem(UUID.randomUUID().toString(), "Milk", 1, LocalDate.now().plusDays(2)),
        PantryItem(UUID.randomUUID().toString(), "Bread", 2, LocalDate.now().plusDays(5)),
        PantryItem(UUID.randomUUID().toString(), "Eggs", 12, LocalDate.now().plusDays(1))
    ))
    val items = _items.asStateFlow()

    fun addItem(item: PantryItem) {
        _items.value = _items.value + item
    }

    fun removeItem(item: PantryItem) {
        _items.value = _items.value - item
    }
}