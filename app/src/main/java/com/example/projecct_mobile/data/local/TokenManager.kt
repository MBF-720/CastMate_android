package com.example.projecct_mobile.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONException
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_preferences")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_RESPONSABLE_KEY = stringPreferencesKey("user_responsable")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_DESCRIPTION_KEY = stringPreferencesKey("user_description")
    }

    data class CachedAgencyProfile(
        val nom: String?,
        val responsable: String?,
        val phone: String?,
        val description: String?
    )

    // Chaque agence est indexée par son email normalisé pour conserver ses infos entre deux connexions
    private fun sanitizeEmail(email: String): String = email.lowercase().replace(Regex("[^a-z0-9_]"), "_")
    private fun agencyProfileKey(email: String) = stringPreferencesKey("agency_profile_${sanitizeEmail(email)}")

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun getTokenSync(): String? {
        return context.dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    private fun JSONObject.putIfNotBlank(key: String, value: String?) {
        if (!value.isNullOrBlank()) {
            put(key, value)
        }
    }

    suspend fun saveUserInfo(
        userId: String?,
        email: String?,
        role: String?,
        nom: String? = null,
        responsable: String? = null,
        phone: String? = null,
        description: String? = null
    ) {
        context.dataStore.edit { preferences ->
            userId?.let { preferences[USER_ID_KEY] = it }
            email?.let { preferences[USER_EMAIL_KEY] = it }
            role?.let { preferences[USER_ROLE_KEY] = it }

            nom?.let {
                if (it.isBlank()) preferences.remove(USER_NAME_KEY) else preferences[USER_NAME_KEY] = it
            }
            responsable?.let {
                if (it.isBlank()) preferences.remove(USER_RESPONSABLE_KEY) else preferences[USER_RESPONSABLE_KEY] = it
            }
            phone?.let {
                if (it.isBlank()) preferences.remove(USER_PHONE_KEY) else preferences[USER_PHONE_KEY] = it
            }
            description?.let {
                if (it.isBlank()) preferences.remove(USER_DESCRIPTION_KEY) else preferences[USER_DESCRIPTION_KEY] = it
            }

            if (!email.isNullOrBlank()) {
                val key = agencyProfileKey(email)
                val cached = try {
                    preferences[key]?.let { JSONObject(it) } ?: JSONObject()
                } catch (_: JSONException) {
                    JSONObject()
                }
                cached.putIfNotBlank("nom", nom)
                cached.putIfNotBlank("responsable", responsable)
                cached.putIfNotBlank("phone", phone)
                cached.putIfNotBlank("description", description)
                if (cached.length() > 0) {
                    // On mémorise les infos agence pour les futures connexions
                    preferences[key] = cached.toString()
                }
            }
        }
    }

    suspend fun saveAgencyProfileCache(
        email: String,
        nom: String?,
        responsable: String?,
        phone: String?,
        description: String?
    ) {
        context.dataStore.edit { preferences ->
            val key = agencyProfileKey(email)
            val cached = JSONObject().apply {
                putIfNotBlank("nom", nom)
                putIfNotBlank("responsable", responsable)
                putIfNotBlank("phone", phone)
                putIfNotBlank("description", description)
            }
            if (cached.length() > 0) {
                preferences[key] = cached.toString()
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun getAgencyProfileCache(email: String): CachedAgencyProfile? {
        val prefs = context.dataStore.data.first()
        val json = prefs[agencyProfileKey(email)] ?: return null
        return try {
            val obj = JSONObject(json)
            CachedAgencyProfile(
                nom = obj.optString("nom", "").takeIf { it.isNotEmpty() },
                responsable = obj.optString("responsable", "").takeIf { it.isNotEmpty() },
                phone = obj.optString("phone", "").takeIf { it.isNotEmpty() },
                description = obj.optString("description", "").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(USER_RESPONSABLE_KEY)
            preferences.remove(USER_PHONE_KEY)
            preferences.remove(USER_DESCRIPTION_KEY)
        }
    }

    suspend fun hasToken(): Boolean {
        return getTokenSync() != null
    }

    suspend fun getUserIdSync(): String? {
        return context.dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun getUserEmailSync(): String? {
        return context.dataStore.data.first()[USER_EMAIL_KEY]
    }

    suspend fun getUserRoleSync(): String? {
        return context.dataStore.data.first()[USER_ROLE_KEY]
    }

    suspend fun getUserNomSync(): String? {
        return context.dataStore.data.first()[USER_NAME_KEY]
    }

    suspend fun getUserResponsableSync(): String? {
        return context.dataStore.data.first()[USER_RESPONSABLE_KEY]
    }

    suspend fun getUserPhoneSync(): String? {
        return context.dataStore.data.first()[USER_PHONE_KEY]
    }

    suspend fun getUserDescriptionSync(): String? {
        return context.dataStore.data.first()[USER_DESCRIPTION_KEY]
    }
}

