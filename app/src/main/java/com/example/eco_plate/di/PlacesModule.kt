package com.example.eco_plate.di // Or your dependency injection package

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Singleton
    @Provides
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        // You MUST initialize Places first
        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyAkSsK-MrhYkgxWBYHNwy6-pNQEpLZfH-w")
        }
        return Places.createClient(context)
    }
}
