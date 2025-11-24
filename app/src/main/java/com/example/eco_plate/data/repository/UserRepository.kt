package com.example.eco_plate.data.repository

import com.example.eco_plate.data.api.UserApi
import com.example.eco_plate.data.models.Address
import com.example.eco_plate.data.models.ApiResponse
import com.example.eco_plate.data.models.User
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi
) {
    suspend fun getProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse.message ?: "Failed to fetch profile"))
                }
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch profile"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateProfile(updates: Map<String, Any>): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.updateProfile(updates)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update profile"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateLocation(latitude: Double, longitude: Double): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val locationData = mapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
            val response = userApi.updateLocation(locationData)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success == true && apiResponse.data != null) {
                    emit(Resource.Success(apiResponse.data))
                } else {
                    emit(Resource.Error(apiResponse.message ?: "Failed to update location"))
                }
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update location"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getAddresses(): Flow<Resource<List<Address>>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.getAddresses()
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch addresses"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun createAddress(address: Map<String, Any>): Flow<Resource<Address>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.createAddress(address)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to create address"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateAddress(addressId: String, address: Map<String, Any>): Flow<Resource<Address>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.updateAddress(addressId, address)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to update address"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun deleteAddress(addressId: String): Flow<Resource<ApiResponse<Nothing>>> = flow {
        emit(Resource.Loading())
        try {
            val response = userApi.deleteAddress(addressId)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to delete address"))
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
