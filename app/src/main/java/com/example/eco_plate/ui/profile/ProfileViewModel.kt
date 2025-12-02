package com.example.eco_plate.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.eco_plate.data.repository.AuthRepository
import com.example.eco_plate.data.repository.CartRepository
import com.example.eco_plate.data.repository.OrderRepository
import com.example.eco_plate.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "ProfileViewModel"
        private const val PROFILE_PIC_PREFS = "profile_pic_prefs"
        private const val KEY_PROFILE_PIC_PATH = "profile_pic_path"
    }
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _businessProfile = MutableStateFlow<BusinessProfile?>(null)
    val businessProfile: StateFlow<BusinessProfile?> = _businessProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _profilePicturePath = MutableStateFlow<String?>(null)
    val profilePicturePath: StateFlow<String?> = _profilePicturePath.asStateFlow()
    
    private val _orderCount = MutableStateFlow(0)
    val orderCount: StateFlow<Int> = _orderCount.asStateFlow()
    
    init {
        loadUserProfile()
        loadProfilePicture()
        loadOrderCount()
        // Listen to changes in auth state
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                if (user != null) {
                    updateProfileFromUser(user)
                }
            }
        }
    }
    
    private fun loadOrderCount() {
        viewModelScope.launch {
            orderRepository.getMyOrders().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val count = result.data?.size ?: 0
                        _orderCount.value = count
                        Log.d(TAG, "Loaded order count: $count")
                        // Update profiles with real order count
                        _userProfile.value?.let { profile ->
                            _userProfile.value = profile.copy(totalOrders = count)
                        }
                        _businessProfile.value?.let { profile ->
                            _businessProfile.value = profile.copy(totalOrders = count)
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "Error loading orders: ${result.message}")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }
    
    private fun loadProfilePicture() {
        val prefs = context.getSharedPreferences(PROFILE_PIC_PREFS, Context.MODE_PRIVATE)
        _profilePicturePath.value = prefs.getString(KEY_PROFILE_PIC_PATH, null)
    }
    
    fun updateProfilePicture(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.filesDir, "profile_picture.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                saveProfilePicturePath(file.absolutePath)
                Log.d(TAG, "Profile picture saved: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile picture", e)
            }
        }
    }
    
    fun updateProfilePicture(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val file = File(context.filesDir, "profile_picture.jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
                
                saveProfilePicturePath(file.absolutePath)
                Log.d(TAG, "Profile picture saved from camera: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving profile picture from camera", e)
            }
        }
    }
    
    private fun saveProfilePicturePath(path: String) {
        val prefs = context.getSharedPreferences(PROFILE_PIC_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_PIC_PATH, path).apply()
        _profilePicturePath.value = path
        
        // Update profile with new picture
        _userProfile.value?.let { profile ->
            _userProfile.value = profile.copy(profilePicture = path)
        }
        _businessProfile.value?.let { profile ->
            _businessProfile.value = profile.copy(businessImageUrl = path)
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
        updateProfilePicture(uri)
    }

    fun updateBusinessImage(bitmap: Bitmap) {
        updateProfilePicture(bitmap)
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
        
        // Use local profile picture if available, otherwise use server's
        val profilePic = _profilePicturePath.value ?: user.profilePicture
        val currentOrderCount = _orderCount.value
        
        // Calculate savings based on order count (estimate $5 per order)
        val estimatedSavings = currentOrderCount * 5.0f
        // Calculate CO2 saved (estimate 0.3kg per order)
        val estimatedCo2Saved = currentOrderCount * 0.3f
        
        _userProfile.value = UserProfile(
            name = displayName,
            email = user.email,
            phone = user.phone ?: "Not provided",
            memberSince = formatMemberSince(user.createdAt),
            totalSaved = estimatedSavings,
            totalOrders = currentOrderCount,
            co2Saved = estimatedCo2Saved,
            profilePicture = profilePic
        )
        _businessProfile.value = BusinessProfile(
            name = displayName,
            email = user.email,
            phone = user.phone ?: "Not provided",
            memberSince = formatMemberSince(user.createdAt),
            totalSaved = estimatedSavings,
            totalOrders = currentOrderCount,
            co2Saved = estimatedCo2Saved,
            businessName = "Your Store",
            businessImageUrl = profilePic
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
