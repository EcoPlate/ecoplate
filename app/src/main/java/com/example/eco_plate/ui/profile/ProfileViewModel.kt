package com.example.eco_plate.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.eco_plate.data.repository.AuthRepository
import com.example.eco_plate.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _businessProfile = MutableStateFlow<BusinessProfile?>(null)
    val businessProfile: StateFlow<BusinessProfile?> = _businessProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadUserProfile()
        // Listen to changes in auth state
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                if (user != null) {
                    updateProfileFromUser(user)
                }
            }
        }
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // First check if we have a user in memory
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                updateProfileFromUser(currentUser)
                _isLoading.value = false
            } else if (authRepository.isLoggedInSync()) {
                // If we're logged in but don't have user data, fetch it from API
                authRepository.fetchCurrentUserProfile().collect { result ->
                    when (result) {
                        is com.example.eco_plate.utils.Resource.Success -> {
                            result.data?.let { user ->
                                updateProfileFromUser(user)
                            }
                            _isLoading.value = false
                        }
                        is com.example.eco_plate.utils.Resource.Error -> {
                            // Fallback to default data if API call fails
                            _userProfile.value = UserProfile(
                                name = "Guest User",
                                email = "guest@example.com",
                                phone = "Not provided",
                                memberSince = "Member since 2024",
                                totalSaved = 0f,
                                totalOrders = 0,
                                co2Saved = 0f
                            )
                            _isLoading.value = false
                        }
                        is com.example.eco_plate.utils.Resource.Loading -> {
                            // Keep loading state
                        }
                    }
                }
            } else {
                // Not logged in at all
                _userProfile.value = UserProfile(
                    name = "Guest User",
                    email = "guest@example.com",
                    phone = "Not provided",
                    memberSince = "Member since 2024",
                    totalSaved = 0f,
                    totalOrders = 0,
                    co2Saved = 0f
                )
                _isLoading.value = false
            }
        }
    }

    fun updateBusinessImage(uri: Uri) {
        // TODO
    }

    fun updateBusinessImage(bitmap: Bitmap) {
        // TODO
    }

    private fun updateProfileFromUser(user: com.example.eco_plate.data.models.User) {
        // Use real user data, ensuring we never pass null
        val firstName = user.firstName?.trim() ?: ""
        val lastName = user.lastName?.trim() ?: ""
        val fullName = "$firstName $lastName".trim()
        
        val displayName = when {
            fullName.isNotEmpty() -> fullName
            !user.username.isNullOrEmpty() -> user.username
            else -> user.email
        }
        
        _userProfile.value = UserProfile(
            name = displayName,
            email = user.email,
            phone = user.phone ?: "Not provided",
            memberSince = formatMemberSince(user.createdAt),
            // These would come from a stats API in a real app
            totalSaved = 245.50f,
            totalOrders = 47,
            co2Saved = 15.3f
        )
        _businessProfile.value = BusinessProfile(
            name = displayName,
            email = user.email,
            phone = user.phone ?: "Not provided",
            memberSince = formatMemberSince(user.createdAt),
            // These would come from a stats API in a real app
            totalSaved = 245.50f,
            totalOrders = 47,
            co2Saved = 15.3f,
            businessName = "Your Store",
            businessImageUrl = null
        )
    }
    
    private fun formatMemberSince(createdAt: String?): String {
        return if (createdAt != null && createdAt.length >= 10) {
            try {
                val datePart = createdAt.substring(0, 10) // Extract YYYY-MM-DD
                val parts = datePart.split("-")
                if (parts.size >= 2) {
                    val year = parts[0]
                    val month = parts[1].toIntOrNull()?.let { monthNum ->
                        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                               "Jul", "Aug", "Sep", "Oct", "Nov", "Dec").getOrNull(monthNum - 1) ?: ""
                    } ?: ""
                    "Member since $month $year"
                } else {
                    "Member since 2024"
                }
            } catch (e: Exception) {
                "Member since 2024"
            }
        } else {
            "Member since 2024"
        }
    }
    
    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            // TODO: Update profile in repository
            _userProfile.value = profile
            _isLoading.value = false
        }
    }
    
    fun refreshProfile() {
        if (authRepository.isLoggedInSync()) {
            viewModelScope.launch {
                _isLoading.value = true
                authRepository.fetchCurrentUserProfile().collect { result ->
                    when (result) {
                        is com.example.eco_plate.utils.Resource.Success -> {
                            result.data?.let { user ->
                                updateProfileFromUser(user)
                            }
                            _isLoading.value = false
                        }
                        is com.example.eco_plate.utils.Resource.Error -> {
                            // Keep existing profile data on error
                            _isLoading.value = false
                        }
                        is com.example.eco_plate.utils.Resource.Loading -> {
                            // Keep loading state
                        }
                    }
                }
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            authRepository.changePassword(oldPassword, newPassword).collect { result ->
                when (result) {
                    is com.example.eco_plate.utils.Resource.Success -> {
                        onResult(true, "Password changed successfully")
                    }
                    is com.example.eco_plate.utils.Resource.Error -> {
                        onResult(false, result.message)
                    }
                    is com.example.eco_plate.utils.Resource.Loading -> {
                        // Loading state handled in UI
                    }
                }
            }
        }
    }
    
    fun updateEmail(newEmail: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            authRepository.updateEmail(newEmail).collect { result ->
                when (result) {
                    is com.example.eco_plate.utils.Resource.Success -> {
                        result.data?.let { user ->
                            updateProfileFromUser(user)
                        }
                        onResult(true, "Email updated successfully")
                    }
                    is com.example.eco_plate.utils.Resource.Error -> {
                        onResult(false, result.message)
                    }
                    is com.example.eco_plate.utils.Resource.Loading -> {
                        // Loading state handled in UI
                    }
                }
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                when (result) {
                    is com.example.eco_plate.utils.Resource.Success -> {
                        // Clear cart on logout
                        cartRepository.clearCart()
                        _userProfile.value = null
                    }
                    is com.example.eco_plate.utils.Resource.Error -> {
                        // Even on error, clear local data
                        cartRepository.clearCart()
                        _userProfile.value = null
                    }
                    is com.example.eco_plate.utils.Resource.Loading -> {
                        // Show loading if needed
                    }
                }
            }
        }
    }
    
    fun isSignedIn(): Boolean {
        return authRepository.isLoggedInSync()
    }
}
