package com.example.projecct_mobile.ui.screens.acteur

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ai.GeminiTrainingService
import com.example.projecct_mobile.data.model.TrainingFeedback
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.LightBlue
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorTrainingScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<TrainingFeedback?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Service Gemini
    val geminiService = remember { GeminiTrainingService(context) }
    
    // Launcher pour sélectionner une vidéo
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        errorMessage = null
        feedback = null // Réinitialiser le feedback précédent
    }
    
    // Fonction d'analyse
    fun analyzeVideo(uri: Uri) {
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            feedback = null
            
            val result = geminiService.analyzeActingVideo(uri)
            
            result.onSuccess { feedbackResult ->
                feedback = feedbackResult
                isAnalyzing = false
            }.onFailure { error ->
                errorMessage = error.message ?: "Erreur lors de l'analyse"
                isAnalyzing = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🎭 Entraînement",
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
            // En-tête informatif
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightBlue.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Entraînez-vous avec l'IA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Envoyez une vidéo de 30 secondes max et recevez un feedback personnalisé sur votre jeu d'acteur",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bouton pour sélectionner une vidéo
            if (selectedVideoUri == null) {
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
                    shape = RoundedCornerShape(12.dp),
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
                            selectedVideoUri = null
                            feedback = null
                            errorMessage = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Supprimer")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bouton d'analyse
                Button(
                    onClick = { analyzeVideo(selectedVideoUri!!) },
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
                        Text("🚀 Analyser ma performance", fontSize = 16.sp)
                    }
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
            
            // Afficher les erreurs
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
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
            }
            
            // Afficher le feedback
            feedback?.let { fb ->
                Spacer(modifier = Modifier.height(24.dp))
                FeedbackResultCard(feedback = fb)
            }
        }
    }
}

@Composable
fun FeedbackResultCard(feedback: TrainingFeedback) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score global
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    feedback.globalScore >= 80 -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    feedback.globalScore >= 60 -> Color(0xFFFFC107).copy(alpha = 0.2f)
                    else -> Color(0xFFFF5722).copy(alpha = 0.2f)
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Score Global",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${feedback.globalScore}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        feedback.globalScore >= 80 -> Color(0xFF4CAF50)
                        feedback.globalScore >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }
                )
                Text(
                    "/ 100",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
            }
        }
        
        // Résumé
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = DarkBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Résumé",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    feedback.summary,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
        
        // Points forts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "💪 Points Forts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                feedback.strengths.forEach { strength ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("✓", color = Color(0xFF4CAF50), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strength,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
        
        // Recommandations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF9C4)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "📝 Recommandations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                feedback.recommendations.forEach { rec ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("→", color = Color(0xFFFFC107), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            rec,
                            fontSize = 14.sp,
                            color = Color(0xFFF57F17)
                        )
                    }
                }
            }
        }
        
        // Détails par catégorie
        Text(
            "📊 Analyse Détaillée",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Émotions
        DetailCategoryCard(
            title = "🎭 Émotions",
            score = feedback.emotions.coherence,
            comment = feedback.emotions.comment,
            details = listOf(
                "Détectées: ${feedback.emotions.detected.joinToString(", ")}",
                "Cohérence: ${feedback.emotions.coherence}/100",
                "Intensité: ${feedback.emotions.intensity}/100"
            )
        )
        
        // Posture
        DetailCategoryCard(
            title = "🧍 Posture",
            score = feedback.posture.score,
            comment = feedback.posture.comment,
            details = feedback.posture.strengths.map { "✓ $it" } + 
                      feedback.posture.improvements.map { "→ $it" }
        )
        
        // Intonation
        DetailCategoryCard(
            title = "🎤 Intonation",
            score = feedback.intonation.score,
            comment = feedback.intonation.comment,
            details = listOf(
                "Clarté: ${feedback.intonation.clarity}/100",
                "Rythme: ${feedback.intonation.rhythm}/100",
                "Expressivité: ${feedback.intonation.expressiveness}/100"
            )
        )
        
        // Expressivité
        DetailCategoryCard(
            title = "✨ Expressivité",
            score = feedback.expressivite.score,
            comment = feedback.expressivite.comment,
            details = listOf(
                feedback.expressivite.facialExpressions,
                feedback.expressivite.bodyLanguage
            )
        )
    }
}

@Composable
fun DetailCategoryCard(
    title: String,
    score: Int,
    comment: String,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$score/100",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        score >= 80 -> Color(0xFF4CAF50)
                        score >= 60 -> Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Barre de progression
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    score >= 80 -> Color(0xFF4CAF50)
                    score >= 60 -> Color(0xFFFFC107)
                    else -> Color(0xFFFF5722)
                },
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                comment,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
            
            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                details.forEach { detail ->
                    Text(
                        detail,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

