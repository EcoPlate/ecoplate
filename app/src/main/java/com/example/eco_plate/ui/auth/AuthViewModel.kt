package com.example.eco_plate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eco_plate.data.models.*
import com.example.eco_plate.data.repository.AuthRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<Resource<AuthResponse>>()
    val authState: LiveData<Resource<AuthResponse>> = _authState

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _logoutState = MutableLiveData<Resource<Nothing>>()
    val logoutState: LiveData<Resource<Nothing>> = _logoutState

    private val _authCheckComplete = MutableLiveData<Boolean>(false)
    val authCheckComplete: LiveData<Boolean> = _authCheckComplete

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            authRepository.isLoggedIn()
                .onEach { isLoggedIn ->
                    _isLoggedIn.value = isLoggedIn
                    _authCheckComplete.value = true
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
}
