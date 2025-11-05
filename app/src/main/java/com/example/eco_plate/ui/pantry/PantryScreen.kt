package com.example.eco_plate.ui.pantry

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun PantryScreen(viewModel: PantryViewModel) {
    val items by viewModel.items.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val sortedItems = remember(items) {
        items.sortedBy { it.expiryDate }
    }
    Column(Modifier.fillMaxSize().padding(8.dp)) {

        Button(onClick = { showDialog = true }, modifier = Modifier.padding(bottom = 8.dp, top= 80.dp)) {
            Text("Add Item")
        }

        LazyColumn {
            items(sortedItems) { item ->
                PantryItemCard(item)
            }
        }
    }

    if (showDialog) {
        AddPantryItemDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, qty, expiry ->
                viewModel.addItem(
                    PantryItem(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        quantity = qty,
                        expiryDate = expiry
                    )
                )
                showDialog = false
            }
        )
    }
}

@Composable
fun PantryItemCard(item: PantryItem) {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val daysUntilExpiry = LocalDate.now().until(item.expiryDate).days
    val backgroundColor = when {
        daysUntilExpiry <= 1 -> Color(0xFFFFCDD2) // Red for very close
        daysUntilExpiry <= 3 -> Color(0xFFFFF9C4) // Yellowish for soon
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Expires: ${item.expiryDate.format(formatter)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AddPantryItemDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Int, expiry: LocalDate) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var expiryDays by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pantry Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it },
                    label = { Text("Expires in (days)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qtyInt = quantity.toIntOrNull() ?: 1
                val expiryInt = expiryDays.toIntOrNull() ?: 3
                val expiryDate = LocalDate.now().plusDays(expiryInt.toLong())
                onAdd(name, qtyInt, expiryDate)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}