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

    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs.remove(USER_ROLE_KEY)
            prefs.remove(STORE_ID_KEY)
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
