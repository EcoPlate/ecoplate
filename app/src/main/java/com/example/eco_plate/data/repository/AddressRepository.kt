package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.AddressApi
import com.example.eco_plate.data.models.CreateAddressRequest
import com.example.eco_plate.data.models.UpdateAddressRequest
import com.example.eco_plate.data.models.UserAddress
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val addressApi: AddressApi
) {
    private val TAG = "AddressRepository"

    suspend fun getAddresses(): Flow<Resource<List<UserAddress>>> = flow {
        emit(Resource.Loading())
        try {
            val response = addressApi.getAddresses()
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "Fetched ${response.body()?.data?.size ?: 0} addresses")
                emit(Resource.Success(response.body()!!.data))
            } else {
                Log.e(TAG, "Failed to fetch addresses: ${response.code()}")
                emit(Resource.Error(response.message() ?: "Failed to fetch addresses"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error fetching addresses", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error fetching addresses", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching addresses", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun getDefaultAddress(): Flow<Resource<UserAddress?>> = flow {
        emit(Resource.Loading())
        try {
            val response = addressApi.getDefaultAddress()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()?.data))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to fetch default address"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun createAddress(request: CreateAddressRequest): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        try {
            Log.d(TAG, "Creating address: ${request.label} - ${request.line1}")
            val response = addressApi.createAddress(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Log.d(TAG, "Address created successfully: ${response.body()!!.data!!.id}")
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                Log.e(TAG, "Failed to create address: ${response.code()}")
                emit(Resource.Error(response.message() ?: "Failed to create address"))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error creating address", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            Log.e(TAG, "Network error creating address", e)
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating address", e)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun updateAddress(id: String, request: UpdateAddressRequest): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        try {
            val response = addressApi.updateAddress(id, request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
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

    suspend fun setDefaultAddress(id: String): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        try {
            val response = addressApi.setDefaultAddress(id)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Log.d(TAG, "Set default address: $id")
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(response.message() ?: "Failed to set default address"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun deleteAddress(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = addressApi.deleteAddress(id)
            if (response.isSuccessful) {
                Log.d(TAG, "Deleted address: $id")
                emit(Resource.Success(true))
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

