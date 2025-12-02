package com.example.eco_plate.data.api

import com.example.eco_plate.data.models.AddressListResponse
import com.example.eco_plate.data.models.AddressResponse
import com.example.eco_plate.data.models.CreateAddressRequest
import com.example.eco_plate.data.models.UpdateAddressRequest
import com.example.eco_plate.data.models.UserAddress
import retrofit2.Response
import retrofit2.http.*

interface AddressApi {
    
    @GET("addresses")
    suspend fun getAddresses(): Response<AddressListResponse>
    
    @GET("addresses/default")
    suspend fun getDefaultAddress(): Response<AddressResponse>
    
    @GET("addresses/{id}")
    suspend fun getAddress(@Path("id") id: String): Response<AddressResponse>
    
    @POST("addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): Response<AddressResponse>
    
    @PATCH("addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body request: UpdateAddressRequest
    ): Response<AddressResponse>
    
    @POST("addresses/{id}/set-default")
    suspend fun setDefaultAddress(@Path("id") id: String): Response<AddressResponse>
    
    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<AddressResponse>
}

