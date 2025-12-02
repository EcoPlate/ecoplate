package com.example.eco_plate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.*
import com.example.eco_plate.data.repository.AuthRepository
import com.example.eco_plate.data.repository.StoreRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _logoutState = MutableLiveData<Resource<Nothing>>()
    val logoutState: LiveData<Resource<Nothing>> = _logoutState

    private val _authCheckComplete = MutableLiveData<Boolean>(false)
    val authCheckComplete: LiveData<Boolean> = _authCheckComplete
    
    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // First get the user role from storage
            val role = authRepository.getUserRoleSync()
            _userRole.value = role
            
            // Then check login status
            authRepository.isLoggedIn()
                .onEach { isLoggedIn ->
                    _isLoggedIn.value = isLoggedIn
                    
                    // If logged in and we have a role, or not logged in, mark as complete
                    if (isLoggedIn && role != null) {
                        _authCheckComplete.value = true
                    } else if (!isLoggedIn) {
                        _authCheckComplete.value = true
                    } else if (isLoggedIn && role == null) {
                        // If logged in but no role, try to fetch from profile
                        fetchUserProfile()
                    }
                }
                .launchIn(viewModelScope)
        }
    }
    
    private fun fetchUserProfile() {
        viewModelScope.launch {
            authRepository.fetchCurrentUserProfile()
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _userRole.value = result.data?.role
                            _authCheckComplete.value = true
                        }
                        is Resource.Error -> {
                            // If we can't fetch profile, assume regular user
                            _userRole.value = "USER"
                            _authCheckComplete.value = true
                        }
                        else -> {}
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun signUp(email: String, password: String, firstName: String? = null, lastName: String? = null, username: String? = null, phone: String? = null) {
        viewModelScope.launch {
            val request = SignUpRequest(
                email = email,
                password = password,
                username = username,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
            authRepository.signUp(request)
                .onEach { result ->
                    _authState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val request = SignInRequest(
                email = email,
                password = password
            )
            authRepository.signIn(request)
                .onEach { result ->
                    _authState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onEach { result ->
                    _logoutState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = false
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val request = ChangePasswordRequest(
                oldPassword = oldPassword,
                newPassword = newPassword
            )
            authRepository.changePassword(request)
                .launchIn(viewModelScope)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    //                      BUSINESS SIGNUP                                       //
    ////////////////////////////////////////////////////////////////////////////////

    fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }
    fun businessSignUp(userEmail: String, businessName: String, businessAddress: String, businessPhone: String, businessEmail: String) {
        viewModelScope.launch {

            val request = BusinessSignUpRequest(
                email = userEmail,
                businessName = businessName,
                businessAddress = businessAddress,
                businessPhone = businessPhone,
                businessEmail = businessEmail
            )

            authRepository.businessSignUp(request)
                .onEach { result ->
                    _authState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun signUpAsStoreOwner(
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null,
        phone: String? = null,
        storeName: String,
        storeAddress: String,
        storePhone: String,
        storeDescription: String? = null,
        storeCity: String? = null,
        storeProvince: String? = null,
        storePostalCode: String? = null,
        storeLatitude: Double? = null,
        storeLongitude: Double? = null
    ) {
        viewModelScope.launch {
            android.util.Log.d("AuthViewModel", "Creating store owner account with coordinates: lat=$storeLatitude, lng=$storeLongitude")
            
            // Create user account with STORE_OWNER role and store details
            // The backend will automatically create the store during signup
            val request = SignUpRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                role = "STORE_OWNER",
                storeName = storeName,
                storeAddress = storeAddress,
                storePhone = storePhone,
                storeDescription = storeDescription,
                storeCity = storeCity,
                storeProvince = storeProvince,
                storePostalCode = storePostalCode,
                storeLatitude = storeLatitude,
                storeLongitude = storeLongitude
            )
            
            authRepository.signUp(request)
                .onEach { result ->
                    _authState.value = result
                    if (result is Resource.Success) {
                        _isLoggedIn.value = true
                        android.util.Log.d("AuthViewModel", "Store owner signup successful - store created by backend with coordinates")
                        // Note: The backend automatically creates the store during signup
                        // No need to create it again here
                    }
                }
                .launchIn(viewModelScope)
        }
    }
}
