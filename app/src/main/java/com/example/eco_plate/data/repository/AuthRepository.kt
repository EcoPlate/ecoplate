package com.example.eco_plate.data.repository

import com.example.eco_plate.data.api.AuthApi
import com.example.eco_plate.data.api.UserApi
import com.example.eco_plate.data.local.TokenManager
import com.example.eco_plate.data.models.*
import com.example.eco_plate.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenManager: TokenManager
) {
    private val _currentUserFlow = MutableStateFlow<User?>(null)
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()
    suspend fun signUp(signUpRequest: SignUpRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApi.signUp(signUpRequest)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Check if the backend response indicates success
                if (authResponse.success != true) {
                    val errorMsg = authResponse.error ?: authResponse.message ?: "Sign up failed"
                    Log.e("AuthRepository", "Sign up failed: $errorMsg")
                    emit(Resource.Error(errorMsg))
                    return@flow
                }
                
                // Check if data field exists
                if (authResponse.data == null) {
                    Log.e("AuthRepository", "Sign up failed: data field is null")
                    emit(Resource.Error("Invalid response from server: missing data"))
                    return@flow
                }
                
                // Validate nested data fields
                if (authResponse.user == null || authResponse.accessToken == null || authResponse.refreshToken == null) {
                    Log.e("AuthRepository", "Sign up failed: missing required fields")
                    emit(Resource.Error("Invalid response from server: missing required fields"))
                    return@flow
                }
                
                // Save tokens and user info
                val accessToken = authResponse.accessToken!!
                val refreshToken = authResponse.refreshToken!!
                val user = authResponse.user!!
                
                tokenManager.saveTokens(accessToken, refreshToken)
                tokenManager.saveUserInfo(user.id, user.role ?: "USER")
                _currentUserFlow.value = user
                
                Log.d("AuthRepository", "Sign up successful for user: ${user.email}")
                emit(Resource.Success(authResponse))
            } else {
                val errorBody = response.errorBody()?.string()
                
                // Try to parse error body as JSON
                try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                    val errorMsg = errorResponse?.error ?: errorResponse?.message ?: "Sign up failed"
                    emit(Resource.Error(errorMsg))
                } catch (e: Exception) {
                emit(Resource.Error(errorBody ?: response.message() ?: "Sign up failed"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AuthRepository", "Sign up error: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "An unexpected error occurred"}"))
        }
    }

    suspend fun signIn(signInRequest: SignInRequest): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            Log.d("AuthRepository", "Attempting sign in for: ${signInRequest.email}")
            val response = authApi.signIn(signInRequest)
            Log.d("AuthRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Check if the backend response indicates success
                if (authResponse.success != true) {
                    val errorMsg = authResponse.error ?: authResponse.message ?: "Sign in failed"
                    Log.e("AuthRepository", "Sign in failed: $errorMsg")
                    emit(Resource.Error(errorMsg))
                    return@flow
                }
                
                // Check if data field exists
                if (authResponse.data == null) {
                    Log.e("AuthRepository", "Sign in failed: data field is null")
                    emit(Resource.Error("Invalid response from server: missing data"))
                    return@flow
                }
                
                // Validate nested data fields
                if (authResponse.user == null || authResponse.accessToken == null || authResponse.refreshToken == null) {
                    Log.e("AuthRepository", "Sign in failed: missing required fields - user: ${authResponse.user != null}, accessToken: ${authResponse.accessToken != null}, refreshToken: ${authResponse.refreshToken != null}")
                    emit(Resource.Error("Invalid response from server: missing required fields"))
                    return@flow
                }
                
                // Save tokens
                val accessToken = authResponse.accessToken!!
                val refreshToken = authResponse.refreshToken!!
                val user = authResponse.user!!
                
                tokenManager.saveTokens(accessToken, refreshToken)
                
                // Save user info
                tokenManager.saveUserInfo(user.id, user.role ?: "USER")
                _currentUserFlow.value = user
                
                Log.d("AuthRepository", "Sign in successful for user: ${user.email}")
                emit(Resource.Success(authResponse))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Sign in failed: ${response.code()} - $errorBody")
                
                // Try to parse error body as JSON
                try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, AuthResponse::class.java)
                    val errorMsg = errorResponse?.error ?: errorResponse?.message ?: "Sign in failed"
                    emit(Resource.Error(errorMsg))
                } catch (e: Exception) {
                emit(Resource.Error(errorBody ?: response.message() ?: "Sign in failed"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AuthRepository", "Sign in error: ${e.message}", e)
            emit(Resource.Error("Error: ${e.message ?: "An unexpected error occurred"}"))
        }
    }

    suspend fun refreshToken(refreshToken: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                // Validate response has required fields
                if (authResponse.accessToken == null || authResponse.refreshToken == null) {
                    emit(Resource.Error("Invalid response from server: missing tokens"))
                    return@flow
                }
                val accessToken = authResponse.accessToken!!
                val refreshToken = authResponse.refreshToken!!
                tokenManager.saveTokens(accessToken, refreshToken)
                emit(Resource.Success(authResponse))
            } else {
                emit(Resource.Error(response.message() ?: "Token refresh failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    suspend fun logout(): Flow<Resource<Nothing>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApi.logout()
            tokenManager.clearTokens()
            _currentUserFlow.value = null
            if (response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error(response.message() ?: "Logout failed"))
            }
        } catch (e: Exception) {
            // Clear tokens even if API call fails
            tokenManager.clearTokens()
            emit(Resource.Success(null))
        }
    }

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): Flow<Resource<Nothing>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApi.changePassword(changePasswordRequest)
            if (response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                emit(Resource.Error(response.message() ?: "Password change failed"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }

    fun isLoggedIn(): Flow<Boolean> = tokenManager.accessToken.map { token ->
        !token.isNullOrEmpty()
    }
    
    fun getCurrentUser(): User? {
        return currentUserFlow.value
    }

    fun isLoggedInSync(): Boolean {
        return !tokenManager.getAccessTokenSync().isNullOrEmpty()
    }
    
    suspend fun changePassword(oldPassword: String, newPassword: String): Flow<Resource<Nothing>> = flow {
        emit(Resource.Loading())
        try {
            val request = ChangePasswordRequest(oldPassword, newPassword)
            val response = authApi.changePassword(request)
            if (response.isSuccessful) {
                emit(Resource.Success(null))
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error(errorBody ?: "Failed to change password"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
    
    suspend fun updateEmail(newEmail: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val updateData = mapOf("email" to newEmail)
            val response = userApi.updateProfile(updateData)
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                _currentUserFlow.value = user
                emit(Resource.Success(user))
            } else {
                val errorBody = response.errorBody()?.string()
                emit(Resource.Error(errorBody ?: "Failed to update email"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
    
    suspend fun fetchCurrentUserProfile(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // Check if we have a token
            val token = tokenManager.getAccessTokenSync()
            Log.d("AuthRepository", "Fetching profile with token: ${if (!token.isNullOrEmpty()) "Bearer ${token.take(20)}..." else "NO TOKEN"}")
            
            if (token.isNullOrEmpty()) {
                Log.e("AuthRepository", "No authentication token found in storage")
                emit(Resource.Error("No authentication token found"))
                return@flow
            }
            
            Log.d("AuthRepository", "Making API call to get profile...")
            val response = userApi.getProfile()
            Log.d("AuthRepository", "Profile API response code: ${response.code()}")
            
            if (response.isSuccessful && response.body()?.data != null) {
                val user = response.body()!!.data!!
                _currentUserFlow.value = user
                
                // Save user info to token manager
                tokenManager.saveUserInfo(user.id, user.role ?: "USER")
                
                Log.d("AuthRepository", "Fetched user profile successfully: ${user.email}")
                emit(Resource.Success(user))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepository", "Failed to fetch user profile: ${response.code()} - $errorBody")
                
                // If we get 401, the token might be expired
                if (response.code() == 401) {
                    Log.e("AuthRepository", "Token might be expired or invalid. Consider refreshing token or re-login.")
                }
                
                emit(Resource.Error(errorBody ?: "Failed to fetch user profile"))
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HTTP error fetching profile: ${e.code()} - ${e.message()}")
            emit(Resource.Error(e.localizedMessage ?: "An unexpected HTTP error occurred"))
        } catch (e: IOException) {
            Log.e("AuthRepository", "Network error fetching profile: ${e.message}")
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error fetching profile: ${e.message}")
            e.printStackTrace()
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
