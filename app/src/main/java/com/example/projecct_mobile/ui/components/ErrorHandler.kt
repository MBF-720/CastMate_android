package com.example.projecct_mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.ui.theme.*

/**
 * Fonction utilitaire pour obtenir un message d'erreur convivial
 */
fun getErrorMessage(exception: Throwable): String {
    return when (exception) {
        is ApiException.CanceledException -> 
            "" // Les requêtes annulées ne doivent pas afficher de message d'erreur
        is ApiException.NetworkException -> 
            "Pas de connexion internet. Vérifiez votre connexion et réessayez."
        is ApiException.UnauthorizedException -> 
            "Session expirée. Veuillez vous reconnecter."
        is ApiException.BadRequestException -> 
            exception.message ?: "Vérifiez vos informations et réessayez."
        is ApiException.NotFoundException -> 
            "Ressource non trouvée."
        is ApiException.ServerException -> 
            "Erreur serveur. Veuillez réessayer plus tard."
        is ApiException.ForbiddenException -> 
            "Vous n'avez pas les permissions nécessaires."
        is ApiException.ConflictException -> 
            exception.message ?: "Cette ressource existe déjà."
        else -> 
            exception.message ?: "Une erreur est survenue. Veuillez réessayer."
    }
}

/**
 * Composant pour afficher un message d'erreur avec un bouton de retry
 */
@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp
        )
        
        Text(
            text = message,
            fontSize = 16.sp,
            color = Red,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        onRetry?.let {
            Button(
                onClick = it,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                )
            ) {
                Text(
                    text = "Réessayer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

