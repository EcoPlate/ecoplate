package com.example.eco_plate.widget

import com.example.eco_plate.data.models.Order

fun ordersToWidgetLines(orders: List<Order>): List<String> {
    return orders
        .sortedByDescending { it.createdAt }
        .take(3)
        .map { order ->
            val status = order.status
                .replace("_", " ")
                .lowercase()
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar { c -> c.uppercase() }
                }

            "Order #${order.orderNumber} • $status"
        }
}