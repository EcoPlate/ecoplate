package com.example.eco_plate.ui.newItem

import androidx.lifecycle.ViewModel
import com.example.eco_plate.data.models.Item
import com.example.eco_plate.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewItemViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
): ViewModel(){
    fun createItem(item: Item){
        //inventoryRepository.createItem()
    }
}