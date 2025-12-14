package com.example.projecct_mobile.ui.screens.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
 * Écran pour sélectionner une date d'interview parmi les 3 proposées (Acteur uniquement)
 */
@Composable
fun SelectInterviewDateScreen(
    interviewId: String,
    interview: InterviewResponse,
    onBackClick: () -> Unit,
    onDateSelected: () -> Unit
) {
    val interviewRepository = remember { InterviewRepository() }
    val scope = rememberCoroutineScope()
    var selectedDateIndex by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Format pour afficher les dates en français
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
    
    /**
     * Soumettre la sélection
     */
    fun submitSelection() {
        if (selectedDateIndex == null) {
            errorMessage = "Veuillez sélectionner une date"
            return
        }
        
        if (interview.proposedDates.size != 3) {
            errorMessage = "Erreur: l'interview doit avoir exactement 3 dates proposées"
            return
        }
        
        val selectedDateOption = interview.proposedDates[selectedDateIndex!!]
        isLoading = true
        errorMessage = null
        
        scope.launch {
            val result = interviewRepository.selectInterviewDate(
                interviewId = interview.id,
                date = selectedDateOption.date,
                time = selectedDateOption.time
            )
            
            isLoading = false
            
            result.onSuccess {
                onDateSelected()
            }
            
            result.onFailure { exception ->
                errorMessage = exception.message ?: "Erreur lors de la sélection de la date"
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // En-tête
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBlue)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        }
        
        // Contenu
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informations du casting
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = interview.castingTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    
                    Text(
                        text = "Agence: ${interview.agenceName}",
                        fontSize = 14.sp,
                        color = GrayBorder
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sélectionnez une date parmi les 3 proposées:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Black
            )
            
            // Affichage des 3 dates proposées
            if (interview.proposedDates.size == 3) {
                interview.proposedDates.forEachIndexed { index, dateOption ->
                    DateOptionCard(
                        date = dateOption.date,
                        time = dateOption.time ?: "",
                        isSelected = selectedDateIndex == index,
                        onClick = {
                            selectedDateIndex = if (selectedDateIndex == index) null else index
                            errorMessage = null
                        },
                        displayDateFormat = displayDateFormat,
                        dateFormat = dateFormat
                    )
                }
            } else {
                Text(
                    text = "Erreur: l'interview doit avoir exactement 3 dates proposées",
                    color = Color(0xFFFF0000),
                    fontSize = 14.sp
                )
            }
            
            // Message d'erreur
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF0000),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bouton de soumission
            Button(
                onClick = { submitSelection() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && selectedDateIndex != null,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White
                    )
                } else {
                    Text(
                        text = "Confirmer la sélection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
    }
}

@Composable
fun DateOptionCard(
    date: String,
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    displayDateFormat: SimpleDateFormat,
    dateFormat: SimpleDateFormat
) {
    val formattedDate = try {
        val parsed = dateFormat.parse(date)
        parsed?.let { displayDateFormat.format(it) } ?: date
    } catch (e: Exception) {
        date
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkBlueLight else White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, DarkBlue)
        } else {
            null
        }
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
                Text(
                    text = formattedDate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) White else DarkBlue
                )
                
                Text(
                    text = "à $time",
                    fontSize = 14.sp,
                    color = if (isSelected) White.copy(alpha = 0.9f) else GrayBorder
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sélectionné",
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

