package com.example.projecct_mobile.ui.screens.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.InterviewResponse
import com.example.projecct_mobile.data.repository.InterviewRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran pour sélectionner une date d'interview parmi les dates proposées
 */
@Composable
fun SelectInterviewDateScreen(
    interview: InterviewResponse,
    onDateSelected: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val interviewRepository = remember { InterviewRepository() }
    var selectedDateIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    // Fonction pour formater la date
    fun formatDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            if (date != null) {
                displayDateFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
    
    // Fonction pour soumettre la sélection
    fun submitSelection() {
        if (selectedDateIndex == null) {
            errorMessage = "Veuillez sélectionner une date"
            return
        }
        
        val selectedDateOption = interview.proposedDates[selectedDateIndex!!]
        isSubmitting = true
        errorMessage = null
        
        scope.launch {
            val result = interviewRepository.selectInterviewDate(
                interviewId = interview.id,
                date = selectedDateOption.date,
                time = selectedDateOption.time
            )
            
            result.onSuccess {
                successMessage = "Date d'interview confirmée avec succès !"
                isSubmitting = false
                // Attendre un peu avant de fermer
                kotlinx.coroutines.delay(1500)
                onDateSelected()
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Erreur lors de la sélection de la date"
                isSubmitting = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = White
                    )
                }
                
                Text(
                    text = "Choisir une date",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                
                Spacer(modifier = Modifier.width(48.dp)) // Équilibrer l'espace
            }
        }
        
        // Contenu
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informations sur le casting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Casting",
                        fontSize = 12.sp,
                        color = GrayBorder,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = interview.castingTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Text(
                        text = "Agence: ${interview.agenceName}",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            Text(
                text = "Choisissez UNE date parmi les 3 options proposées :",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            Text(
                text = "L'agence a proposé ${interview.proposedDates.size} dates. Sélectionnez celle qui vous convient le mieux.",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Liste des dates proposées (exactement 3)
            if (interview.proposedDates.size == 3) {
                interview.proposedDates.forEachIndexed { index, dateOption ->
                    DateOptionCard(
                        date = dateOption.date,
                        time = dateOption.time,
                        isSelected = selectedDateIndex == index,
                        onClick = {
                            // Désélectionner si on clique sur la même date, sinon sélectionner la nouvelle
                            selectedDateIndex = if (selectedDateIndex == index) null else index
                            errorMessage = null
                        },
                        formatDate = ::formatDate,
                        dateNumber = index + 1
                    )
                }
            } else {
                // Cas où il n'y a pas exactement 3 dates (ne devrait pas arriver)
                Text(
                    text = "Erreur: L'agence doit proposer exactement 3 dates",
                    color = Red,
                    fontSize = 14.sp
                )
            }
            
            // Message d'erreur
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            fontSize = 14.sp,
                            color = Red
                        )
                    }
                }
            }
            
            // Message de succès
            if (successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = successMessage!!,
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Message si aucune date sélectionnée
            if (selectedDateIndex == null && !isSubmitting) {
                Text(
                    text = "⚠️ Veuillez sélectionner une date pour continuer",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Bouton de confirmation
            Button(
                onClick = { submitSelection() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting && selectedDateIndex != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDateIndex != null) DarkBlue else GrayBorder
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmation...", color = White)
                } else {
                    if (selectedDateIndex != null) {
                        Text("Confirmer cette date", color = White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Sélectionnez une date", color = White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DateOptionCard(
    date: String,
    time: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    formatDate: (String) -> String,
    dateNumber: Int = 1
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkBlue.copy(alpha = 0.1f) else White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, DarkBlue)
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge avec le numéro de la date (1, 2 ou 3)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (isSelected) DarkBlue else GrayBorder.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dateNumber.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) White else Color(0xFF1A1A1A)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isSelected) DarkBlue else GrayBorder,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = formatDate(date),
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) DarkBlue else Color(0xFF1A1A1A)
                    )
                }
                
                // Afficher l'heure (devrait toujours être présente car obligatoire)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isSelected) DarkBlue else GrayBorder,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (time != null) "À $time" else "Heure non spécifiée",
                        fontSize = 14.sp,
                        color = if (isSelected) DarkBlue else Color(0xFF666666),
                        fontStyle = if (time == null) androidx.compose.ui.text.font.FontStyle.Italic else null
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sélectionné",
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

