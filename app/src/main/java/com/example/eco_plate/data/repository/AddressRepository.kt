package com.example.eco_plate.data.repository

import android.util.Log
import com.example.eco_plate.data.api.AddressApi
import com.example.eco_plate.data.models.CreateAddressRequest
import com.example.eco_plate.data.models.UpdateAddressRequest
import com.example.eco_plate.data.models.UserAddress
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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

    fun getAddresses(): Flow<Resource<List<UserAddress>>> = flow {
        emit(Resource.Loading())
        val response = addressApi.getAddresses()
        if (response.isSuccessful && response.body()?.success == true) {
            Log.d(TAG, "Fetched ${response.body()?.data?.size ?: 0} addresses")
            emit(Resource.Success(response.body()!!.data))
        } else {
            Log.e(TAG, "Failed to fetch addresses: ${response.code()}")
            emit(Resource.Error(response.message() ?: "Failed to fetch addresses"))
        }
    }.catch { e ->
        Log.e(TAG, "Error fetching addresses", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }

    fun getDefaultAddress(): Flow<Resource<UserAddress?>> = flow {
        emit(Resource.Loading())
        val response = addressApi.getDefaultAddress()
        if (response.isSuccessful) {
            emit(Resource.Success(response.body()?.data))
        } else {
            emit(Resource.Error(response.message() ?: "Failed to fetch default address"))
        }
    }.catch { e ->
        Log.e(TAG, "Error fetching default address", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }

    fun createAddress(request: CreateAddressRequest): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        Log.d(TAG, "Creating address: ${request.label} - ${request.line1}")
        val response = addressApi.createAddress(request)
        if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
            Log.d(TAG, "Address created successfully: ${response.body()!!.data!!.id}")
            emit(Resource.Success(response.body()!!.data!!))
        } else {
            Log.e(TAG, "Failed to create address: ${response.code()}")
            emit(Resource.Error(response.message() ?: "Failed to create address"))
        }
    }.catch { e ->
        Log.e(TAG, "Error creating address", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }

    fun updateAddress(id: String, request: UpdateAddressRequest): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        val response = addressApi.updateAddress(id, request)
        if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
            emit(Resource.Success(response.body()!!.data!!))
        } else {
            emit(Resource.Error(response.message() ?: "Failed to update address"))
        }
    }.catch { e ->
        Log.e(TAG, "Error updating address", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }

    fun setDefaultAddress(id: String): Flow<Resource<UserAddress>> = flow {
        emit(Resource.Loading())
        val response = addressApi.setDefaultAddress(id)
        if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
            Log.d(TAG, "Set default address: $id")
            emit(Resource.Success(response.body()!!.data!!))
        } else {
            emit(Resource.Error(response.message() ?: "Failed to set default address"))
        }
    }.catch { e ->
        Log.e(TAG, "Error setting default address", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }

    fun deleteAddress(id: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        val response = addressApi.deleteAddress(id)
        if (response.isSuccessful) {
            Log.d(TAG, "Deleted address: $id")
            emit(Resource.Success(true))
        } else {
            emit(Resource.Error(response.message() ?: "Failed to delete address"))
        }
    }.catch { e ->
        Log.e(TAG, "Error deleting address", e)
        emit(Resource.Error(e.message ?: "An unexpected error occurred"))
    }
}
