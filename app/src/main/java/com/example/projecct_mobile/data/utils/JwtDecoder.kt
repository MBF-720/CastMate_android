package com.example.projecct_mobile.data.utils

import android.util.Base64
import org.json.JSONObject

/**
 * Utilitaire pour décoder un token JWT et extraire les informations
 */
object JwtDecoder {
    
    /**
     * Décode un token JWT et retourne le payload sous forme de JSONObject
     */
    fun decodePayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                return null
            }
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes)
            JSONObject(decodedString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extrait l'ID utilisateur depuis un token JWT
     * Cherche dans les champs: sub, userId, id, _id
     */
    fun getUserIdFromToken(token: String): String? {
        val payload = decodePayload(token) ?: return null
        
        return payload.optString("sub", null)
            ?: payload.optString("userId", null)
            ?: payload.optString("id", null)
            ?: payload.optString("_id", null)
            ?: payload.optString("user_id", null)
    }
    
    /**
     * Extrait l'email utilisateur depuis un token JWT
     */
    fun getEmailFromToken(token: String): String? {
        val payload = decodePayload(token) ?: return null
        return payload.optString("email", null)
    }
}

