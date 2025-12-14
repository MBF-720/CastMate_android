package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.*
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.RankingRepository
import com.example.projecct_mobile.data.repository.RankingException
import com.example.projecct_mobile.ui.components.ProfilePhoto
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Écran de classement global des acteurs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(
    onBackClick: () -> Unit
) {
    val rankingRepository = remember { RankingRepository() }
    val acteurRepository = remember { ActeurRepository() }
    val scope = rememberCoroutineScope()
    
    var myRanking by remember { mutableStateOf<ActorRanking?>(null) }
    var leaderboard by remember { mutableStateOf<LeaderboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var myActeurId by remember { mutableStateOf<String?>(null) }
    var statsRanking by remember { mutableStateOf<ActorRanking?>(null) }
    
    // Charger le classement de l'acteur connecté et le leaderboard
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null
            
            // Récupérer l'ID de l'acteur connecté
            val acteurId = acteurRepository.getCurrentActeurId()
            myActeurId = acteurId
            
            // Charger le ranking de l'acteur
            if (acteurId != null) {
                rankingRepository.getActorRanking(acteurId).onSuccess { ranking ->
                    myRanking = ranking
                }.onFailure { exception ->
                    android.util.Log.w("RankingScreen", "Erreur chargement ranking: ${exception.message}")
                }
            }
            
            // Charger le leaderboard
            rankingRepository.getLeaderboard(page = currentPage, limit = 50).onSuccess { response ->
                leaderboard = response
                isLoading = false
            }.onFailure { exception ->
                errorMessage = when (exception) {
                    is RankingException.NetworkError -> "Erreur de connexion. Vérifiez votre internet."
                    is RankingException.Unauthorized -> "Vous devez être connecté pour voir le classement."
                    else -> "Erreur lors du chargement du classement: ${exception.message}"
                }
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Erreur inattendue: ${e.message}"
            isLoading = false
        }
    }
    
    // Charger la page suivante
    val loadNextPage: () -> Unit = {
        scope.launch {
            val totalPages = leaderboard?.totalPages ?: 0
            if (currentPage < totalPages) {
                val nextPage = currentPage + 1
                rankingRepository.getLeaderboard(page = nextPage, limit = 50).onSuccess { response ->
                    // Ajouter les nouveaux acteurs à la liste existante
                    val currentActors = leaderboard?.actors ?: emptyList()
                    leaderboard = response.copy(actors = currentActors + response.actors)
                    currentPage = nextPage
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBlue,           // Bleu foncé de l'app
                        DarkBlueLight       // Bleu clair de l'app
                    )
                )
            )
    ) {
        // Header avec gradient violet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.18f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Classement",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                
                Spacer(modifier = Modifier.size(44.dp)) // Pour centrer le titre
            }
        }
        
        // Contenu avec coins arrondis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFFF3F5FB),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Text(
                                text = errorMessage ?: "Erreur",
                                fontSize = 16.sp,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        currentPage = 1
                                        // Recharger les données
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text("Réessayer", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Afficher le ranking de l'acteur connecté en haut
                        item {
                            // Toujours afficher la carte, même si myRanking est null (pour le podium)
                            val actors = leaderboard?.actors ?: emptyList()
                            val currentRanking = myRanking ?: actors.find { it.acteurId == myActeurId }?.ranking
                            
                            if (currentRanking != null || actors.isNotEmpty()) {
                                MyRankingCard(
                                    ranking = currentRanking,
                                    topActors = actors.take(3),
                                    myActeurId = myActeurId,
                                    onInfoClick = { 
                                        if (currentRanking != null) {
                                            statsRanking = currentRanking 
                                        }
                                    }
                                )
                            }
                        }
                        
                        val actors = leaderboard?.actors ?: emptyList()
                        itemsIndexed(actors) { index, actor ->
                            LeaderboardItem(
                                position = (currentPage - 1) * 50 + index + 1,
                                actor = actor,
                                isCurrentUser = actor.acteurId == myActeurId
                            )
                        }
                        
                        // Afficher un bouton pour charger plus si nécessaire
                        item {
                            val totalPages = leaderboard?.totalPages ?: 0
                            if (currentPage < totalPages) {
                                Button(
                                    onClick = loadNextPage,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                    .height(50.dp)
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue
                                ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Charger plus", color = White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogue des statistiques détaillées
    if (statsRanking != null) {
        StatsDialog(
            ranking = statsRanking!!,
            onDismiss = { statsRanking = null }
        )
    }
}

/**
 * Carte affichant le ranking de l'acteur connecté avec podium
 */
@Composable
private fun MyRankingCard(
    ranking: ActorRanking?,
    topActors: List<LeaderboardActor>,
    myActeurId: String?,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Orange  // Orange de l'app
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Message d'encouragement avec percentile
                if (ranking != null) {
                    Text(
                        text = "You are doing better than\n${ranking.percentile}% of other players!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        lineHeight = 22.sp
                    )
                } else {
                    Text(
                        text = "Classement Top 3",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                
                // Podium stylisé avec les vrais joueurs
                if (topActors.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Position 2
                        PodiumPosition(
                            position = 2,
                            actor = topActors.getOrNull(1),
                            height = 100.dp,
                            backgroundColor = DarkBlueLight.copy(alpha = 0.8f),
                            isCurrentUser = topActors.getOrNull(1)?.acteurId == myActeurId
                        )
                        
                        // Position 1
                        PodiumPosition(
                            position = 1,
                            actor = topActors.getOrNull(0),
                            height = 140.dp,
                            backgroundColor = DarkBlue,
                            isCurrentUser = topActors.getOrNull(0)?.acteurId == myActeurId
                        )
                        
                        // Position 3
                        PodiumPosition(
                            position = 3,
                            actor = topActors.getOrNull(2),
                            height = 80.dp,
                            backgroundColor = DarkBlueLight.copy(alpha = 0.6f),
                            isCurrentUser = topActors.getOrNull(2)?.acteurId == myActeurId
                        )
                    }
                } else {
                    // Message si aucun joueur
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun classement disponible",
                            color = White
                        )
                    }
                }
            }
            
            // Bouton d'information (i) en haut à droite - Seulement si ranking existe
            if (ranking != null) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Voir mes statistiques",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialogue des statistiques détaillées
 */
@Composable
private fun StatsDialog(
    ranking: ActorRanking,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Statistiques",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grille de statistiques
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItemDetailed(
                            label = "Position Globale",
                            value = if (ranking.globalPosition != null) "#${ranking.globalPosition}" else "N/A",
                            modifier = Modifier.weight(1f)
                        )
                        StatItemDetailed(
                            label = "Rang",
                            value = ranking.rank.displayName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItemDetailed(
                            label = "Score Global",
                            value = "${ranking.globalScore} pts",
                            modifier = Modifier.weight(1f)
                        )
                        StatItemDetailed(
                            label = "Score Moyen",
                            value = String.format("%.1f", ranking.averageScore),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItemDetailed(
                            label = "Sessions",
                            value = "${ranking.totalSessions}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItemDetailed(
                            label = "NFTs Gagnés",
                            value = "${ranking.totalNFTs}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItemDetailed(
                            label = "Niveau Max",
                            value = "${ranking.maxLevel}",
                            modifier = Modifier.weight(1f)
                        )
                        StatItemDetailed(
                            label = "Consistance",
                            value = String.format("%.1f%%", ranking.consistency),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItemDetailed(
                            label = "Performance Récente",
                            value = String.format("%.1f", ranking.recentPerformance),
                            modifier = Modifier.weight(1f)
                        )
                        StatItemDetailed(
                            label = "Win Streak",
                            value = "🔥 ${ranking.winStreak}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DarkBlue
                )
            ) {
                Text(
                    "Fermer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Position du podium avec données réelles
 */
@Composable
private fun PodiumPosition(
    position: Int,
    actor: LeaderboardActor?,
    height: Dp,
    backgroundColor: Color,
    isCurrentUser: Boolean = false
) {
    if (actor == null) return
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar circulaire avec photo de profil si disponible
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (isCurrentUser) 3.dp else 0.dp,
                    color = Gold,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!actor.photoFileId.isNullOrBlank()) {
                // Afficher la photo de profil si elle existe
                ProfilePhoto(
                    photoFileId = actor.photoFileId,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder avatar avec initiales
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DarkBlue.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actor.getInitials(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
            }
        }
        
        // Nom
        Text(
            text = actor.prenom,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = White,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        
        // Colonne du podium
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Numéro de position
                Text(
                    text = "$position",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = White
                )
                // Points
                Text(
                    text = "${actor.ranking.globalScore} pts",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Composant pour afficher une statistique détaillée
 */
@Composable
private fun StatItemDetailed(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8E8E93),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
    }
}

/**
 * Item du leaderboard (style moderne)
 */
@Composable
private fun LeaderboardItem(
    position: Int,
    actor: LeaderboardActor,
    isCurrentUser: Boolean
) {
    val ranking = actor.ranking
    val backgroundColor = if (isCurrentUser) {
        LightBlue  // Bleu clair de l'app
    } else {
        White
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCurrentUser) 4.dp else 1.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position avec badge rond
            Text(
                text = "$position",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Avatar circulaire avec photo de profil si disponible
            if (!actor.photoFileId.isNullOrBlank()) {
                // Afficher la photo de profil si elle existe
                ProfilePhoto(
                    photoFileId = actor.photoFileId,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder avatar avec initiales
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actor.getInitials(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }
            }
            
            // Nom et informations
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actor.getFullName(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                Text(
                    text = "${ranking.globalScore} points",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
            }
            
            // Badge de rang
            if (position <= 3) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (position) {
                                1 -> Gold
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (position) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> ""
                        },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Composant pour afficher une statistique
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = White.copy(alpha = 0.8f)
        )
    }
}

