package com.example.projecct_mobile.ui.screens.casting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ai.FreeVideoAnalysisService
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.data.model.TrainingFeedback
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.White
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.InputStream

/**
 * Données d'un essai vidéo
 */
data class VideoAttempt(
    val videoUri: Uri,
    val feedback: TrainingFeedback,
    val attemptNumber: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastingApplicationScreen(
    casting: Casting,
    onBackClick: () -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Service IA gratuit (ML Kit + MediaPipe pour l'analyse vidéo)
    val videoAnalysisService = remember { FreeVideoAnalysisService(context) }
    val castingRepository = remember { CastingRepository() }
    
    // États
    var currentVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentFeedback by remember { mutableStateOf<TrainingFeedback?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attempts by remember { mutableStateOf<List<VideoAttempt>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var hasAlreadyApplied by remember { mutableStateOf(false) }
    var isLoadingStatus by remember { mutableStateOf(true) }
    
    val maxAttempts = 2
    
    var hasConfirmedInterview by remember { mutableStateOf(false) }
    
    // Vérifier si l'acteur a déjà postulé et s'il a une interview confirmée
    LaunchedEffect(casting.actualId) {
        val castingId = casting.actualId
        if (castingId != null) {
            isLoadingStatus = true
            try {
                // 1. Vérifier si l'acteur a déjà postulé
                val statusResult = castingRepository.getMyStatus(castingId)
                statusResult.onSuccess { status ->
                    hasAlreadyApplied = status.hasApplied
                    if (status.hasApplied) {
                        errorMessage = "Vous avez déjà postulé à ce casting. Vous ne pouvez pas envoyer une nouvelle candidature."
                    }
                }.onFailure { exception ->
                    // Si erreur 401, l'acteur n'a pas encore postulé (normal)
                    if (exception is ApiException.UnauthorizedException) {
                        hasAlreadyApplied = false
                    } else {
                        // Autre erreur, on assume qu'il n'a pas postulé
                        hasAlreadyApplied = false
                    }
                }
                
                // 2. Vérifier si l'acteur a une interview confirmée pour ce casting
                try {
                    val interviewRepository = com.example.projecct_mobile.data.repository.InterviewRepository()
                    // Charger toutes les interviews pour être sûr de ne rien manquer
                    val allInterviewsResult = interviewRepository.getActorInterviews(null)
                    allInterviewsResult.onSuccess { allInterviews ->
                        // Vérifier s'il y a une interview confirmée pour ce casting
                        hasConfirmedInterview = allInterviews.any { interview ->
                            interview.castingId == castingId && 
                            interview.statusEnum == com.example.projecct_mobile.data.model.InterviewStatus.CONFIRMED
                        }
                        if (hasConfirmedInterview) {
                            errorMessage = "Vous avez une interview confirmée pour ce casting. Vous ne pouvez plus postuler à nouveau."
                        }
                        android.util.Log.d("CastingApplicationScreen", "✅ Vérification interviews: ${allInterviews.size} interviews trouvées, confirmée: $hasConfirmedInterview pour casting $castingId")
                        if (hasConfirmedInterview) {
                            val confirmedInterview = allInterviews.firstOrNull { 
                                it.castingId == castingId && 
                                it.statusEnum == com.example.projecct_mobile.data.model.InterviewStatus.CONFIRMED 
                            }
                            android.util.Log.d("CastingApplicationScreen", "📅 Interview confirmée: ${confirmedInterview?.selectedDate} à ${confirmedInterview?.selectedTime}")
                        }
                    }.onFailure { exception ->
                        android.util.Log.e("CastingApplicationScreen", "❌ Erreur chargement interviews: ${exception.message}")
                        hasConfirmedInterview = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CastingApplicationScreen", "❌ Exception chargement interviews: ${e.message}", e)
                    hasConfirmedInterview = false
                }
                
                isLoadingStatus = false
            } catch (e: Exception) {
                android.util.Log.e("CastingApplicationScreen", "Erreur vérification statut: ${e.message}")
                hasAlreadyApplied = false
                hasConfirmedInterview = false
                isLoadingStatus = false
            }
        } else {
            isLoadingStatus = false
        }
    }
    
    // Launcher pour sélectionner une vidéo
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        currentVideoUri = uri
        errorMessage = null
        currentFeedback = null
    }
    
    // Fonction pour vérifier la taille de la vidéo
    fun checkVideoSize(uri: Uri): Long? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                var size = 0L
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    size += bytesRead
                }
                size
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Fonction d'analyse
    fun analyzeVideo(uri: Uri) {
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            currentFeedback = null
            
            // Vérifier la taille de la vidéo avant l'analyse
            val videoSizeBytes = checkVideoSize(uri)
            if (videoSizeBytes != null) {
                val videoSizeMB = videoSizeBytes / (1024.0 * 1024.0) // Conversion en Double
                if (videoSizeMB > 10.0) {
                    errorMessage = "La vidéo est trop volumineuse (${String.format("%.1f", videoSizeMB)} MB). Maximum: 10 MB. Veuillez compresser ou réduire la durée."
                    isAnalyzing = false
                    return@launch
                }
            }
            
            // Passer les informations du casting pour un contexte plus précis
            val result = videoAnalysisService.analyzeActingVideo(
                videoUri = uri,
                roleDescription = casting.descriptionRole,
                synopsis = casting.synopsis,
                castingTitle = casting.titre
            )
            
            result.onSuccess { feedbackResult ->
                currentFeedback = feedbackResult
                isAnalyzing = false
            }.onFailure { error ->
                // Gérer les erreurs d'analyse
                errorMessage = error.message ?: "Erreur lors de l'analyse vidéo"
                isAnalyzing = false
            }
        }
    }
    
    // Sauvegarder un essai
    fun saveAttempt() {
        val videoUri = currentVideoUri ?: return
        val feedback = currentFeedback ?: return
        
        if (attempts.size >= maxAttempts) {
            errorMessage = "Vous avez atteint le maximum de $maxAttempts essais"
            return
        }
        
        val newAttempt = VideoAttempt(
            videoUri = videoUri,
            feedback = feedback,
            attemptNumber = attempts.size + 1
        )
        
        attempts = attempts + newAttempt
        currentVideoUri = null
        currentFeedback = null
        errorMessage = null
    }
    
    // Envoyer la candidature
    fun submitApplication() {
        // Vérifier si l'acteur a déjà postulé
        if (hasAlreadyApplied) {
            errorMessage = "Vous avez déjà postulé à ce casting. Vous ne pouvez pas envoyer une nouvelle candidature."
            return
        }
        
        // Vérifier si l'acteur a une interview confirmée
        if (hasConfirmedInterview) {
            errorMessage = "Vous avez une interview confirmée pour ce casting. Vous ne pouvez plus postuler à nouveau."
            return
        }
        
        if (attempts.isEmpty()) {
            errorMessage = "Veuillez enregistrer au moins une vidéo avant de postuler"
            return
        }
        
        scope.launch {
            isSubmitting = true
            errorMessage = null
            
            // Choisir le meilleur essai (score le plus élevé)
            val bestAttempt = attempts.maxByOrNull { it.feedback.globalScore }
                ?: attempts.last()
            
            // Convertir le feedback en JSON
            val gson = Gson()
            val aiFeedbackJson = gson.toJson(bestAttempt.feedback)
            
            val castingId = casting.actualId
            if (castingId == null) {
                errorMessage = "ID du casting invalide"
                isSubmitting = false
                return@launch
            }
            
            // Envoyer la candidature avec vidéo
            val result = castingRepository.applyToCastingWithVideo(
                id = castingId,
                context = context,
                videoUri = bestAttempt.videoUri,
                aiFeedback = aiFeedbackJson
            )
            
            result.onSuccess {
                showSuccessDialog = true
                isSubmitting = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Erreur lors de l'envoi de la candidature"
                isSubmitting = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Postuler au casting",
                        color = White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF3F5FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // En-tête avec info casting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        casting.titre ?: "Casting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enregistrez une vidéo d'audition (max 30s, max 10 MB)\nVous avez ${maxAttempts} essais",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section : Enregistrer un nouvel essai (désactivée si déjà postulé)
            if (attempts.size < maxAttempts && !hasAlreadyApplied && !isLoadingStatus) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Essai ${attempts.size + 1}/$maxAttempts",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bouton sélectionner vidéo
                        if (currentVideoUri == null) {
                            Button(
                                onClick = { videoPickerLauncher.launch("video/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.VideoCall, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📹 Choisir une vidéo", fontSize = 16.sp)
                            }
                        } else {
                            // Vidéo sélectionnée
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Vidéo sélectionnée",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                        Text(
                                            "Prête pour l'analyse",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(onClick = {
                                        currentVideoUri = null
                                        currentFeedback = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Supprimer")
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Bouton analyser
                            Button(
                                onClick = { analyzeVideo(currentVideoUri!!) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isAnalyzing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("⏳ Analyse en cours...", fontSize = 16.sp)
                                } else {
                                    Icon(Icons.Default.Psychology, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("🚀 Analyser ma vidéo", fontSize = 16.sp)
                                }
                            }
                        }
                        
                        // Afficher le feedback si disponible
                        currentFeedback?.let { feedback ->
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        feedback.globalScore >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        feedback.globalScore >= 60 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                                        else -> Color(0xFFFF5722).copy(alpha = 0.2f)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Score: ${feedback.globalScore}/100",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            feedback.globalScore >= 80 -> Color(0xFF4CAF50)
                                            feedback.globalScore >= 60 -> Color(0xFFFFC107)
                                            else -> Color(0xFFFF5722)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        feedback.summary,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Bouton sauvegarder cet essai
                            Button(
                                onClick = { saveAttempt() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("💾 Sauvegarder cet essai", fontSize = 14.sp)
                            }
                        }
                        
                        // Message d'information pendant l'analyse
                        if (isAnalyzing) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3CD)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                         "L'IA Gemini analyse votre vidéo...\nCela peut prendre 30-60 secondes",
                                        fontSize = 14.sp,
                                        color = Color(0xFF856404)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Section : Essais enregistrés
            if (attempts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "💾 Essais enregistrés (${attempts.size}/$maxAttempts)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        attempts.forEach { attempt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Essai ${attempt.attemptNumber}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Score: ${attempt.feedback.globalScore}/100",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    "${attempt.feedback.globalScore}/100",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        attempt.feedback.globalScore >= 80 -> Color(0xFF4CAF50)
                                        attempt.feedback.globalScore >= 60 -> Color(0xFFFFC107)
                                        else -> Color(0xFFFF5722)
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Afficher les erreurs
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8D7DA)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFF721C24),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            error,
                            color = Color(0xFF721C24),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Message d'avertissement si interview confirmée
            if (hasConfirmedInterview) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Vous avez une interview confirmée pour ce casting. Vous ne pouvez plus postuler à nouveau. Consultez votre agenda pour plus de détails.",
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Message d'avertissement si déjà postulé
            if (hasAlreadyApplied && !hasConfirmedInterview) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Vous avez déjà postulé à ce casting. Vous ne pouvez pas envoyer une nouvelle candidature.",
                            fontSize = 14.sp,
                            color = Color(0xFF856404),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Bouton final : Envoyer la candidature
            if (attempts.isNotEmpty()) {
                Button(
                    onClick = { submitApplication() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSubmitting && !hasAlreadyApplied && !hasConfirmedInterview && !isLoadingStatus,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSubmitting || hasAlreadyApplied || hasConfirmedInterview) Color.Gray else DarkBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting || isLoadingStatus) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("⏳ Envoi en cours...", fontSize = 16.sp)
                    } else if (hasConfirmedInterview) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Interview confirmée", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else if (hasAlreadyApplied) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Déjà postulé", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📤 Envoyer ma candidature", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Info : meilleur essai sélectionné
                val bestAttempt = attempts.maxByOrNull { it.feedback.globalScore }
                bestAttempt?.let {
                    Text(
                        "💡 Votre meilleur essai (Score: ${it.feedback.globalScore}/100) sera envoyé",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Dialog de succès
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSuccess()
            },
            title = {
                Text("✅ Candidature envoyée !")
            },
            text = {
                Text("Votre candidature avec vidéo a été envoyée avec succès.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSuccess()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

