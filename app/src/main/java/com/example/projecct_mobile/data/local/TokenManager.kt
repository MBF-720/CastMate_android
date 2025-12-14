package com.example.projecct_mobile.data.local

import android.content.Context
import android.util.Base64
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
        return context.dataStore.data.first()[TOKEN_KEY]?.trim() // Trim les espaces
    }
    
    /**
     * Obtient le token de manière synchrone (bloquante).
     * À utiliser uniquement dans les contexts non-suspend comme les intercepteurs OkHttp.
     */
    fun getTokenBlocking(): String? {
        return try {
            kotlinx.coroutines.runBlocking {
                context.dataStore.data.first()[TOKEN_KEY]?.trim() // Trim les espaces
            }
        } catch (e: Exception) {
            android.util.Log.e("TokenManager", "Erreur lors de la récupération du token: ${e.message}")
            null
        }
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token.trim() // Trim avant de sauvegarder
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
    
    /**
     * Stocke le mot de passe généré pour un compte créé via Google Sign-In
     * @param email Email de l'utilisateur
     * @param password Mot de passe généré pour ce compte
     */
    suspend fun saveGoogleAccountPassword(email: String, password: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("google_password_${sanitizeEmail(email)}")
            preferences[key] = password
        }
    }
    
    /**
     * Récupère le mot de passe généré pour un compte créé via Google Sign-In
     * @param email Email de l'utilisateur
     * @return Mot de passe stocké ou null si aucun mot de passe n'est stocké
     */
    suspend fun getGoogleAccountPassword(email: String): String? {
        val prefs = context.dataStore.data.first()
        val key = stringPreferencesKey("google_password_${sanitizeEmail(email)}")
        return prefs[key]
    }
    
    /**
     * Supprime le mot de passe stocké pour un compte Google (optionnel, pour nettoyer)
     */
    suspend fun clearGoogleAccountPassword(email: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("google_password_${sanitizeEmail(email)}")
            preferences.remove(key)
        }
    }
    
    /**
     * Stocke un token de réinitialisation de mot de passe pour un email donné
     * @param email Email de l'utilisateur
     * @param resetToken Token de réinitialisation
     */
    suspend fun saveResetToken(email: String, resetToken: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("reset_token_${sanitizeEmail(email)}")
            preferences[key] = resetToken
            // Stocker aussi la date de création pour expiration (optionnel)
            val timestampKey = stringPreferencesKey("reset_token_timestamp_${sanitizeEmail(email)}")
            preferences[timestampKey] = System.currentTimeMillis().toString()
        }
    }
    
    /**
     * Récupère le token de réinitialisation stocké pour un email donné
     * @param email Email de l'utilisateur
     * @return Token stocké ou null si aucun token n'est stocké ou expiré (> 1 heure)
     */
    suspend fun getResetToken(email: String): String? {
        val prefs = context.dataStore.data.first()
        val key = stringPreferencesKey("reset_token_${sanitizeEmail(email)}")
        val token = prefs[key] ?: return null
        
        // Vérifier l'expiration (1 heure)
        val timestampKey = stringPreferencesKey("reset_token_timestamp_${sanitizeEmail(email)}")
        val timestamp = prefs[timestampKey]?.toLongOrNull() ?: return token
        
        val now = System.currentTimeMillis()
        val oneHourInMillis = 60 * 60 * 1000L
        if (now - timestamp > oneHourInMillis) {
            // Token expiré, le supprimer
            clearResetToken(email)
            return null
        }
        
        return token
    }
    
    /**
     * Supprime le token de réinitialisation stocké pour un email donné
     * @param email Email de l'utilisateur
     */
    suspend fun clearResetToken(email: String) {
        context.dataStore.edit { preferences ->
            val key = stringPreferencesKey("reset_token_${sanitizeEmail(email)}")
            val timestampKey = stringPreferencesKey("reset_token_timestamp_${sanitizeEmail(email)}")
            preferences.remove(key)
            preferences.remove(timestampKey)
        }
    }
    
    /**
     * Décode le token JWT et extrait l'ID utilisateur depuis le payload
     */
    suspend fun getUserIdFromToken(): String? {
        return try {
            val token = getTokenSync() ?: return null
            
            // Un token JWT a 3 parties séparées par des points: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                android.util.Log.e("TokenManager", "❌ Token JWT invalide: nombre de parties incorrect")
                return null
            }
            
            // Décoder le payload (2ème partie)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            
            // Logger le payload décodé pour le débogage
            android.util.Log.d("TokenManager", "📋 Payload JWT décodé: $decodedString")
            
            // Parser le JSON pour extraire l'ID
            val jsonObject = JSONObject(decodedString)
            
            // Logger les clés importantes du payload pour le débogage
            android.util.Log.d("TokenManager", "📋 Informations du token JWT:")
            android.util.Log.d("TokenManager", "  - id: ${jsonObject.optString("id", "N/A")}")
            android.util.Log.d("TokenManager", "  - email: ${jsonObject.optString("email", "N/A")}")
            android.util.Log.d("TokenManager", "  - role: ${jsonObject.optString("role", "N/A")}")
            if (jsonObject.has("exp")) {
                val exp = jsonObject.getLong("exp")
                val now = System.currentTimeMillis() / 1000
                val remaining = exp - now
                android.util.Log.d("TokenManager", "  - exp: $exp (expire dans ${remaining / 60} minutes)")
            } else {
                android.util.Log.w("TokenManager", "  - exp: N/A (token sans expiration)")
            }
            
            // Le backend peut stocker l'ID sous différents noms: "id", "userId", "sub", "_id", "actorId", etc.
            val userId = jsonObject.optString("id", null)
                ?: jsonObject.optString("userId", null)
                ?: jsonObject.optString("sub", null)
                ?: jsonObject.optString("_id", null)
                ?: jsonObject.optString("actorId", null)
                ?: jsonObject.optString("user_id", null)
                ?: jsonObject.optString("actor_id", null)
            
            if (userId.isNullOrBlank()) {
                android.util.Log.e("TokenManager", "❌ ID utilisateur introuvable dans le token JWT")
                android.util.Log.e("TokenManager", "❌ Payload décodé: $decodedString")
            } else {
                android.util.Log.d("TokenManager", "✅ ID utilisateur extrait du token JWT: $userId")
            }
            
            userId
        } catch (e: Exception) {
            android.util.Log.e("TokenManager", "❌ Erreur lors du décodage du token JWT: ${e.message}", e)
            null
        }
    }
    
    /**
     * Vérifie si le token JWT est expiré
     * @return true si le token est expiré ou invalide, false sinon
     */
    suspend fun isTokenExpired(): Boolean {
        return try {
            val token = getTokenSync() ?: return true // Pas de token = considéré comme expiré
            
            // Un token JWT a 3 parties séparées par des points: header.payload.signature
            val parts = token.split(".")
            if (parts.size != 3) {
                android.util.Log.e("TokenManager", "❌ Token JWT invalide: nombre de parties incorrect")
                return true
            }
            
            // Décoder le payload (2ème partie)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            
            // Parser le JSON pour extraire l'expiration
            val jsonObject = JSONObject(decodedString)
            
            // Le champ "exp" contient le timestamp Unix d'expiration
            if (!jsonObject.has("exp")) {
                android.util.Log.w("TokenManager", "⚠️ Token JWT sans champ 'exp' - considéré comme valide")
                return false // Pas de champ exp = considéré comme non expiré (token sans expiration)
            }
            
            val expTimestamp = jsonObject.getLong("exp")
            val currentTimestamp = System.currentTimeMillis() / 1000 // Timestamp actuel en secondes
            
            val isExpired = currentTimestamp >= expTimestamp
            
            if (isExpired) {
                android.util.Log.w("TokenManager", "⚠️ Token JWT expiré: exp=$expTimestamp, maintenant=$currentTimestamp")
                android.util.Log.w("TokenManager", "⚠️ Différence: ${currentTimestamp - expTimestamp} secondes d'expiration")
                // Supprimer le token expiré
                clearToken()
            } else {
                val remainingSeconds = expTimestamp - currentTimestamp
                val remainingMinutes = remainingSeconds / 60
                val remainingHours = remainingMinutes / 60
                if (remainingHours > 0) {
                    android.util.Log.d("TokenManager", "✅ Token JWT valide: expire dans ${remainingHours}h ${remainingMinutes % 60}min")
                } else {
                    android.util.Log.d("TokenManager", "✅ Token JWT valide: expire dans ${remainingMinutes} minutes")
                }
            }
            
            isExpired
        } catch (e: Exception) {
            android.util.Log.e("TokenManager", "❌ Erreur lors de la vérification d'expiration du token: ${e.message}", e)
            true // En cas d'erreur, considérer comme expiré
        }
    }
    
    /**
     * Vérifie si le token JWT est valide (présent et non expiré)
     * @return true si le token est valide, false sinon
     */
    suspend fun isTokenValid(): Boolean {
        val token = getTokenSync() ?: return false
        return !isTokenExpired()
    }
}

