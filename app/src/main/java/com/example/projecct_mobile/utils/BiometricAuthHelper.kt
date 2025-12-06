package com.example.projecct_mobile.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executors

/**
 * Helper pour gérer l'authentification biométrique (empreinte digitale, reconnaissance faciale)
 */
object BiometricAuthHelper {
    
    /**
     * Vérifie si l'authentification biométrique est disponible sur l'appareil
     * Retourne le code de statut BiometricManager
     */
    fun checkBiometricSupport(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or 
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }
    
    /**
     * Vérifie si l'authentification biométrique est disponible sur l'appareil
     */
    fun isBiometricAvailable(context: Context): Boolean {
        return checkBiometricSupport(context) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Affiche le prompt biométrique et exécute la callback en cas de succès
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String = "Authentification requise",
        subtitle: String = "Veuillez vous authentifier pour continuer",
        negativeButtonText: String = "Annuler",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        // Utiliser le MainExecutor pour que les callbacks s'exécutent sur le thread UI
        val executor = ContextCompat.getMainExecutor(activity)
        
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    android.util.Log.d("BiometricAuth", "✅ Authentification biométrique réussie")
                    onSuccess()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    android.util.Log.e("BiometricAuth", "❌ Erreur authentification: $errorCode - $errString")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            android.util.Log.d("BiometricAuth", "🚫 Authentification annulée par l'utilisateur")
                            onCancel()
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onError("Trop de tentatives échouées. Veuillez réessayer plus tard.")
                        }
                        else -> {
                            onError(errString.toString())
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    android.util.Log.w("BiometricAuth", "⚠️ Authentification échouée (empreinte non reconnue)")
                    // Ne pas appeler onError ici, le BiometricPrompt gère déjà l'affichage
                    // L'utilisateur peut réessayer
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            // Utiliser BIOMETRIC_STRONG comme recommandé dans le guide
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        android.util.Log.d("BiometricAuth", "🔐 Lancement du prompt biométrique")
        biometricPrompt.authenticate(promptInfo)
    }
}

