package com.example.eco_plate.data.repository

import com.example.eco_plate.data.api.SearchApi
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchApi: SearchApi
) {
    suspend fun searchStores(
        latitude: Double,
        longitude: Double,
        radius: Double? = 5.0,
        category: String? = null,
        query: String? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): Flow<Resource<SearchStoresResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = searchApi.searchStores(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                category = category,
                query = query,
                limit = limit,
                offset = offset
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to search stores"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun searchItems(
        latitude: Double,
        longitude: Double,
        radius: Double? = 5.0,
        category: String? = null,
        query: String? = null,
        minDiscount: Int? = null,
        maxPrice: Double? = null,
        isVegetarian: Boolean? = null,
        isVegan: Boolean? = null,
        isGlutenFree: Boolean? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): Flow<Resource<SearchItemsResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = searchApi.searchItems(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                category = category,
                query = query,
                minDiscount = minDiscount,
                maxPrice = maxPrice,
                isVegetarian = isVegetarian,
                isVegan = isVegan,
                isGlutenFree = isGlutenFree,
                limit = limit,
                offset = offset
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to search items"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getNearbyStores(
        latitude: Double,
        longitude: Double,
        limit: Int? = 10
    ): Flow<Resource<List<Store>>> = flow {
        emit(Resource.Loading())
        try {
            val response = searchApi.getNearbyStores(latitude, longitude, limit)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch nearby stores"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
