@file:Suppress("DEPRECATION")

package com.example.projecct_mobile.data.utils

import android.content.Context
import android.content.Intent
import com.example.projecct_mobile.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/**
 * Gestionnaire utilitaire pour simplifier les interactions avec Google Sign-In.
 * La nouvelle API Identity n'est pas encore intégrée côté backend, on conserve donc l'ancienne implémentation en supprimant les avertissements.
 */
class GoogleAuthUiClient(
    private val context: Context
) {
    private val signInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = signInClient.signInIntent

    fun getAccountFromIntent(data: Intent?): Result<GoogleSignInAccount> {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return runCatching {
            task.getResult(ApiException::class.java)
        }
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun signOut() {
        signInClient.signOut()
    }
}

