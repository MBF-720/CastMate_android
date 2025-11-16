package com.example.projecct_mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.ui.theme.*

/**
 * Dialogue d'erreur détaillé qui affiche tous les détails d'une erreur
 * pour aider au débogage et à la compréhension des problèmes
 */
@Composable
fun ErrorDetailDialog(
    title: String,
    message: String,
    errorDetails: String? = null,
    exception: Throwable? = null,
    isAgency: Boolean = true,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icône d'erreur
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Erreur",
                        tint = Red,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Titre
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    textAlign = TextAlign.Center
                )
                
                Divider(color = GrayBorder)
                
                // Message principal
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Détails techniques (si disponibles)
                if (errorDetails != null || exception != null) {
                    Divider(color = GrayBorder)
                    
                    Text(
                        text = if (isAgency) "Détails techniques :" else "Technical Details:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = GrayBorder.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Type d'exception
                        exception?.let { ex ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isAgency) "Type:" else "Type:",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkBlue
                                )
                                Text(
                                    text = ex.javaClass.simpleName,
                                    fontSize = 12.sp,
                                    color = Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        
                        // Code HTTP (si ApiException)
                        when (exception) {
                            is ApiException.NotFoundException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Code HTTP:" else "HTTP Code:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = "404",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ApiException.BadRequestException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Code HTTP:" else "HTTP Code:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = "400",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ApiException.UnauthorizedException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Code HTTP:" else "HTTP Code:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = "401",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ApiException.ForbiddenException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Code HTTP:" else "HTTP Code:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = "403",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ApiException.ServerException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Code HTTP:" else "HTTP Code:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = "500",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            is ApiException.NetworkException -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAgency) "Erreur:" else "Error:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = DarkBlue
                                    )
                                    Text(
                                        text = if (isAgency) "Problème de connexion réseau" else "Network connection issue",
                                        fontSize = 12.sp,
                                        color = Red,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                        
                        // Message brut de l'exception
                        exception?.message?.let { msg ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isAgency) "Message:" else "Message:",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkBlue
                                )
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        // Détails additionnels
                        errorDetails?.let { details ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isAgency) "Informations additionnelles:" else "Additional Info:",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = DarkBlue
                                )
                                Text(
                                    text = details,
                                    fontSize = 11.sp,
                                    color = Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bouton de fermeture
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                ) {
                    Text(
                        text = if (isAgency) "Fermer" else "Close",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

