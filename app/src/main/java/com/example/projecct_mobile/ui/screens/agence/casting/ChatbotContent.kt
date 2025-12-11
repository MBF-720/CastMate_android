package com.example.projecct_mobile.ui.screens.agence.casting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.ChatMessage
import com.example.projecct_mobile.data.model.ChatbotResponse
import com.example.projecct_mobile.data.model.SuggestedActor
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.data.repository.ChatConversationRepository
import com.example.projecct_mobile.data.repository.GroqChatbotRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Contenu de l'onglet Chatbot pour filtrer les acteurs
 */
@Composable
fun ChatbotContent(
    castingId: String,
    castingTitle: String,
    casting: com.example.projecct_mobile.data.model.Casting,
    modifier: Modifier = Modifier,
    onViewActorProfile: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val groqRepository = remember { GroqChatbotRepository() }
    val castingRepository = remember { CastingRepository() }
    val conversationRepository = remember { ChatConversationRepository(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var chatbotResponse by remember { mutableStateOf<ChatbotResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var chatHistory by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var currentCasting by remember { mutableStateOf(casting) }
    var conversationId by remember { mutableStateOf<Long?>(null) }
    var isLoadingHistory by remember { mutableStateOf(true) }
    
    // Charger la conversation existante et l'historique
    LaunchedEffect(castingId) {
        scope.launch {
            try {
                // Créer ou récupérer la conversation
                val conversation = conversationRepository.getOrCreateConversation(castingId, castingTitle)
                conversationId = conversation.conversationId
                
                // Charger l'historique des messages
                val savedMessages = conversationRepository.getMessages(castingId)
                
                if (savedMessages.isEmpty()) {
                    // Ajouter le message de bienvenue initial
                    val welcomeMessage = ChatMessage(
                        text = "Bonjour ! Je suis votre assistant IA pour vous aider à trouver les meilleurs acteurs pour le casting \"$castingTitle\".\n\nPosez-moi une question, par exemple :\n• \"Trouve-moi les acteurs de 25-35 ans\"\n• \"Quels acteurs ont plus de 5 ans d'expérience ?\"\n• \"Montre-moi les candidats de Tunis\"",
                        isBot = true
                    )
                    chatHistory = listOf(welcomeMessage)
                    
                    // Sauvegarder le message de bienvenue
                    conversationRepository.saveMessage(
                        conversationId = conversationId!!,
                        text = welcomeMessage.text,
                        isBot = true
                    )
                } else {
                    // Restaurer l'historique sauvegardé
                    chatHistory = savedMessages
                    android.util.Log.d("ChatbotContent", "✅ Historique restauré: ${savedMessages.size} messages")
                }
                
                // Recharger le casting pour avoir les candidats à jour
                val result = castingRepository.getCastingById(castingId)
                result.onSuccess { updatedCasting ->
                    currentCasting = updatedCasting
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatbotContent", "Erreur chargement conversation: ${e.message}", e)
            } finally {
                isLoadingHistory = false
            }
        }
    }
    
    // Scroll automatique vers le bas quand un nouveau message arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Indicateur de chargement initial
        if (isLoadingHistory) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else {
            // Zone de chat
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatHistory) { message ->
                    ChatMessageBubble(message = message)
                    
                    // Afficher les suggestions si le message en a
                    message.suggestedActors?.let { actors ->
                        SuggestedActorsInline(
                            actors = actors,
                            onViewActorProfile = onViewActorProfile
                        )
                    }
                }
                
                // Afficher l'erreur si présente
                errorMessage?.let { error ->
                    item {
                        ErrorBubble(message = error)
                    }
                }
                
                // Indicateur de chargement
                if (isLoading) {
                    item {
                        LoadingBubble()
                    }
                }
            }
        }
        
        // Zone de saisie
        ChatInputField(
            query = query,
            onQueryChange = { query = it },
            onSend = {
                if (query.isNotBlank() && !isLoading && conversationId != null) {
                    val userMessage = query
                    query = ""
                    errorMessage = null
                    
                    // Ajouter le message de l'utilisateur
                    val userChatMessage = ChatMessage(text = userMessage, isBot = false)
                    chatHistory = chatHistory + userChatMessage
                    
                    // Sauvegarder le message utilisateur
                    scope.launch {
                        conversationRepository.saveMessage(
                            conversationId = conversationId!!,
                            text = userMessage,
                            isBot = false
                        )
                    }
                    
                    // Envoyer la requête au chatbot Gemini
                    isLoading = true
                    scope.launch {
                        try {
                            // Recharger le casting pour avoir les candidats à jour
                            val refreshResult = castingRepository.getCastingById(castingId)
                            refreshResult.onSuccess { updatedCasting ->
                                currentCasting = updatedCasting
                                
                                // Appeler Groq (100% gratuit)
                                val result = groqRepository.queryChatbot(
                                    casting = updatedCasting,
                                    query = userMessage
                                )
                                result.onSuccess { response ->
                                    // Créer le message bot avec les suggestions
                                    val botChatMessage = ChatMessage(
                                        text = response.answer,
                                        isBot = true,
                                        suggestedActors = response.suggestedActors.ifEmpty { null }
                                    )
                                    chatHistory = chatHistory + botChatMessage
                                    
                                    // Sauvegarder le message bot avec les suggestions
                                    conversationRepository.saveMessage(
                                        conversationId = conversationId!!,
                                        text = response.answer,
                                        isBot = true,
                                        suggestedActors = response.suggestedActors.ifEmpty { null }
                                    )
                                }
                                result.onFailure { exception ->
                                    errorMessage = exception.message ?: "Erreur lors de l'interrogation du chatbot"
                                }
                            }
                            refreshResult.onFailure {
                                errorMessage = "Erreur lors du chargement du casting"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erreur: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = !isLoading && !isLoadingHistory
        )
    }
}

/**
 * Bulle de message de chat
 */
@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isBot) 4.dp else 16.dp,
                bottomEnd = if (message.isBot) 16.dp else 4.dp
            ),
            color = if (message.isBot) Color(0xFFF0F0F0) else DarkBlue
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = if (message.isBot) Color(0xFF1A1A1A) else White,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Section des acteurs suggérés (affichée inline avec les messages)
 */
@Composable
private fun SuggestedActorsInline(
    actors: List<SuggestedActor>,
    onViewActorProfile: (String) -> Unit
) {
    if (actors.isEmpty()) {
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Acteurs suggérés (${actors.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        
        actors.forEach { actor ->
            SuggestedActorCard(
                actor = actor,
                onViewProfile = { onViewActorProfile(actor.acteurId) }
            )
        }
    }
}

/**
 * Section des acteurs suggérés
 */
@Composable
private fun SuggestedActorsSection(
    response: ChatbotResponse,
    onViewActorProfile: (String) -> Unit
) {
    if (response.suggestedActors.isEmpty()) {
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Acteurs suggérés (${response.filteredCount}/${response.totalCandidates})",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )
        
        response.suggestedActors.forEach { actor ->
            SuggestedActorCard(
                actor = actor,
                onViewProfile = { onViewActorProfile(actor.acteurId) }
            )
        }
    }
}

/**
 * Carte d'acteur suggéré
 */
@Composable
private fun SuggestedActorCard(
    actor: SuggestedActor,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${actor.prenom ?: ""} ${actor.nom ?: ""}".trim(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                
                // Score de correspondance
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        actor.matchScore >= 0.8 -> Color(0xFF4CAF50)
                        actor.matchScore >= 0.6 -> Color(0xFFFF9800)
                        else -> Color(0xFF9E9E9E)
                    }
                ) {
                    Text(
                        text = "${(actor.matchScore * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
            
            // Informations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                actor.age?.let {
                    Text(
                        text = "$it ans",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                actor.experience?.let {
                    Text(
                        text = "$it ans d'expérience",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                actor.gouvernorat?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            // Raisons de correspondance
            if (actor.matchReasons.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actor.matchReasons.forEach { reason ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(DarkBlue)
                            )
                            Text(
                                text = reason,
                                fontSize = 11.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bulle d'erreur
 */
@Composable
private fun ErrorBubble(message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFEBEE)
    ) {
        Text(
            text = "❌ $message",
            modifier = Modifier.padding(12.dp),
            fontSize = 14.sp,
            color = Color(0xFFD32F2F)
        )
    }
}

/**
 * Bulle de chargement
 */
@Composable
private fun LoadingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF0F0F0)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = DarkBlue
                )
                Text(
                    text = "Recherche en cours...",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

/**
 * Champ de saisie pour le chat
 */
@Composable
private fun ChatInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        text = "Posez votre question...",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
            
            IconButton(
                onClick = onSend,
                enabled = enabled && query.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (enabled && query.isNotBlank()) DarkBlue else Color(0xFFCCCCCC),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Envoyer",
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

