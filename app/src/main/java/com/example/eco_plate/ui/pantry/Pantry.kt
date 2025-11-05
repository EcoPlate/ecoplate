package com.example.eco_plate.ui.pantry

import java.time.LocalDate

data class PantryItem(
    val id: String,
    val name: String,
    val quantity: Int = 1,
    val expiryDate: LocalDate
)