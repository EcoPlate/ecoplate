package com.example.eco_plate.ui.foodFeed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun FoodFeedScreen(viewModel: FoodFeedViewModel) {

    // Sample data
    val offers = listOf(
        FoodOffer("1", "Bread Loaf", "Fresh bread expiring tomorrow", GeoPoint(49.2827, -123.1207)),
        FoodOffer("2", "Tomatoes", "Extra ripe, free pickup", GeoPoint(49.27, -123.13))
    )

    Column(Modifier.fillMaxSize()) {
        // Map
        AndroidView(
            factory = { context ->
                // Initialize osmdroid config
                Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", 0))

                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Center on Vancouver
                    controller.setZoom(12.5)
                    controller.setCenter(GeoPoint(49.2827, -123.1207))

                    // Examples, change it later
                    offers.forEach { offer ->
                        val marker = Marker(this)
                        marker.position = offer.location
                        marker.title = offer.title
                        marker.subDescription = offer.description
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        this.overlays.add(marker)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top= 80.dp)
        )

        // List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            //Temporary sample offers
            items(offers) { offer ->
                FoodOfferCard(offer)
            }
        }
    }
}
@Composable
fun FoodOfferCard(offer: FoodOffer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = offer.title, style = MaterialTheme.typography.titleMedium)
            Text(text = offer.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

