package com.example.eco_plate.ui.foodFeed

import org.osmdroid.util.GeoPoint

data class FoodOffer (
    val id: String,
    val title: String,
    val description: String,
    val location: GeoPoint
    //add expiry date etc
)