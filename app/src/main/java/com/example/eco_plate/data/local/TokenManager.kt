package com.example.eco_plate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eco_plate_prefs")

/**
 * Data class to hold cached store information
 */
data class CachedStoreInfo(
    val id: String,
    val name: String,
    val address: String?,
    val phone: String?,
    val description: String?,
    val imageUrl: String?,
    val rating: Double?
)

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val STORE_ID_KEY = stringPreferencesKey("store_id")
        // Store info cache keys
        private val STORE_NAME_KEY = stringPreferencesKey("store_name")
        private val STORE_ADDRESS_KEY = stringPreferencesKey("store_address")
        private val STORE_PHONE_KEY = stringPreferencesKey("store_phone")
        private val STORE_DESCRIPTION_KEY = stringPreferencesKey("store_description")
        private val STORE_IMAGE_URL_KEY = stringPreferencesKey("store_image_url")
        private val STORE_RATING_KEY = stringPreferencesKey("store_rating")
        // User info cache keys
        private val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
        private val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN_KEY]
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN_KEY]
    }

    val userId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ID_KEY]
    }

    val userRole: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ROLE_KEY]
    }

    val storeId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_ID_KEY]
    }
    
    // Store info flows
    val storeName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_NAME_KEY]
    }
    
    val storeAddress: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_ADDRESS_KEY]
    }
    
    val storePhone: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_PHONE_KEY]
    }
    
    val storeDescription: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_DESCRIPTION_KEY]
    }
    
    val storeImageUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[STORE_IMAGE_URL_KEY]
    }
    
    val storeRating: Flow<Double?> = context.dataStore.data.map { prefs ->
        prefs[STORE_RATING_KEY]?.toDoubleOrNull()
    }
    
    // User info flows
    val userFirstName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_FIRST_NAME_KEY]
    }
    
    val userLastName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_LAST_NAME_KEY]
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY]
    }
    
    /**
     * Get cached store info synchronously
     */
    fun getCachedStoreInfo(): CachedStoreInfo? {
        return runBlocking {
            val prefs = context.dataStore.data.first()
            val id = prefs[STORE_ID_KEY] ?: return@runBlocking null
            val name = prefs[STORE_NAME_KEY] ?: return@runBlocking null
            CachedStoreInfo(
                id = id,
                name = name,
                address = prefs[STORE_ADDRESS_KEY],
                phone = prefs[STORE_PHONE_KEY],
                description = prefs[STORE_DESCRIPTION_KEY],
                imageUrl = prefs[STORE_IMAGE_URL_KEY],
                rating = prefs[STORE_RATING_KEY]?.toDoubleOrNull()
            )
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserInfo(userId: String, userRole: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USER_ROLE_KEY] = userRole
        }
    }

    suspend fun saveStoreId(storeId: String) {
        context.dataStore.edit { prefs ->
            prefs[STORE_ID_KEY] = storeId
        }
    }
    
    /**
     * Save complete store information for caching
     */
    suspend fun saveStoreInfo(
        storeId: String,
        name: String,
        address: String? = null,
        phone: String? = null,
        description: String? = null,
        imageUrl: String? = null,
        rating: Double? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[STORE_ID_KEY] = storeId
            prefs[STORE_NAME_KEY] = name
            address?.let { prefs[STORE_ADDRESS_KEY] = it }
            phone?.let { prefs[STORE_PHONE_KEY] = it }
            description?.let { prefs[STORE_DESCRIPTION_KEY] = it }
            imageUrl?.let { prefs[STORE_IMAGE_URL_KEY] = it }
            rating?.let { prefs[STORE_RATING_KEY] = it.toString() }
        }
    }
    
    /**
     * Save user profile information for caching
     */
    suspend fun saveUserProfile(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null
    ) {
        context.dataStore.edit { prefs ->
            firstName?.let { prefs[USER_FIRST_NAME_KEY] = it }
            lastName?.let { prefs[USER_LAST_NAME_KEY] = it }
            email?.let { prefs[USER_EMAIL_KEY] = it }
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(STORE_ID_KEY)
            // Also clear cached store info
            prefs.remove(STORE_NAME_KEY)
            prefs.remove(STORE_ADDRESS_KEY)
            prefs.remove(STORE_PHONE_KEY)
            prefs.remove(STORE_DESCRIPTION_KEY)
            prefs.remove(STORE_IMAGE_URL_KEY)
            prefs.remove(STORE_RATING_KEY)
            // Clear user profile info
            prefs.remove(USER_FIRST_NAME_KEY)
            prefs.remove(USER_LAST_NAME_KEY)
            prefs.remove(USER_EMAIL_KEY)
        }
    }

    suspend fun getAccessTokenAsync(): String? {
        var token: String? = null
        context.dataStore.data.map { prefs ->
            token = prefs[ACCESS_TOKEN_KEY]
        }.collect { }
        return token
    }

    suspend fun getRefreshTokenSync(): String? {
        var token: String? = null
        context.dataStore.data.map { prefs ->
            token = prefs[REFRESH_TOKEN_KEY]
        }.collect { }
        return token
    }
    
    fun getAccessTokenSync(): String? {
        // This is a non-suspend synchronous method, used for quick checks
        // Note: This may return stale data as DataStore is async
        return runBlocking {
            context.dataStore.data.first()[ACCESS_TOKEN_KEY]
        }
    }
}
