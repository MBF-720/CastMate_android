package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Page de profil éditable pour les acteurs
 */
@Composable
fun ActorProfileScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    
    // Données utilisateur
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    
    val acteurRepository = remember { ActeurRepository() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Charger les données acteur - seulement au premier chargement, pas quand on édite
    LaunchedEffect(Unit) {
        if (!isEditing) {
            isLoading = true
            errorMessage = null
            
            try {
                val result = acteurRepository.getCurrentActeur()
                
                result.onSuccess { acteur ->
                    nom = acteur.nom
                    prenom = acteur.prenom
                    email = acteur.email
                    telephone = acteur.tel
                    age = acteur.age.toString()
                    gouvernorat = acteur.gouvernorat
                    experience = acteur.experience.toString()
                    instagram = acteur.socialLinks?.instagram ?: ""
                    youtube = acteur.socialLinks?.youtube ?: ""
                    tiktok = acteur.socialLinks?.tiktok ?: ""
                    isLoading = false
                }
                
                result.onFailure { exception ->
                    // Si on ne peut pas charger le profil acteur, charger au moins l'email depuis TokenManager
                    android.util.Log.w("ActorProfileScreen", "Impossible de charger le profil acteur: ${exception.message}")
                    try {
                        val tokenManager = TokenManager(context)
                        val emailValue = tokenManager.getUserEmailSync()
                        if (emailValue != null) {
                            email = emailValue
                        }
                        // Ne pas afficher d'erreur bloquante - permettre à l'utilisateur de remplir les champs
                        errorMessage = null
                    } catch (e: Exception) {
                        // Ne pas afficher d'erreur non plus
                        errorMessage = null
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ActorProfileScreen", "Erreur lors du chargement: ${e.message}", e)
                // Essayer de charger au moins l'email
                try {
                    val tokenManager = TokenManager(context)
                    val emailValue = tokenManager.getUserEmailSync()
                    if (emailValue != null) {
                        email = emailValue
                    }
                } catch (ex: Exception) {
                    // Ignorer
                }
                // Ne pas afficher d'erreur - permettre à l'utilisateur de remplir les champs
                errorMessage = null
                isLoading = false
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(DarkBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Mon Profil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            android.util.Log.d("ActorProfileScreen", "Bouton cliqué, isEditing actuel: $isEditing")
                            if (isEditing) {
                                // Sauvegarder les modifications
                                android.util.Log.d("ActorProfileScreen", "Sauvegarde en cours...")
                                
                                // Valider l'email si fourni
                                if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMessage = "L'email n'est pas valide"
                                    return@clickable
                                }
                                
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    
                                    try {
                                        val updateRequest = com.example.projecct_mobile.data.model.UpdateActeurRequest(
                                            nom = nom.takeIf { it.isNotBlank() },
                                            prenom = prenom.takeIf { it.isNotBlank() },
                                            email = email.takeIf { it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() },
                                            tel = telephone.takeIf { it.isNotBlank() },
                                            age = age.toIntOrNull(),
                                            gouvernorat = gouvernorat.takeIf { it.isNotBlank() },
                                            experience = experience.toIntOrNull(),
                                            socialLinks = com.example.projecct_mobile.data.model.SocialLinks(
                                                instagram = instagram.takeIf { it.isNotBlank() },
                                                youtube = youtube.takeIf { it.isNotBlank() },
                                                tiktok = tiktok.takeIf { it.isNotBlank() }
                                            )
                                        )
                                        
                                        val result = acteurRepository.updateCurrentActeur(updateRequest)
                                        
                                        result.onSuccess {
                                            successMessage = "Profil mis à jour avec succès"
                                            isLoading = false
                                            isEditing = false
                                            android.util.Log.d("ActorProfileScreen", "Profil mis à jour avec succès")
                                        }
                                        
                                        result.onFailure { exception ->
                                            errorMessage = getErrorMessage(exception)
                                            isLoading = false
                                            android.util.Log.e("ActorProfileScreen", "Erreur lors de la sauvegarde: ${exception.message}")
                                            // Ne pas désactiver le mode édition en cas d'erreur
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = getErrorMessage(e)
                                        isLoading = false
                                        android.util.Log.e("ActorProfileScreen", "Exception lors de la sauvegarde: ${e.message}", e)
                                    }
                                }
                            } else {
                                // Activer le mode édition
                                android.util.Log.d("ActorProfileScreen", "Activation du mode édition")
                                isEditing = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Sauvegarder" else "Modifier",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Contenu
        if (isLoading && !isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else if (errorMessage != null && !isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ErrorMessage(
                    message = errorMessage ?: "Erreur",
                    onRetry = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val result = acteurRepository.getCurrentActeur()
                                result.onSuccess { acteur ->
                                    nom = acteur.nom
                                    prenom = acteur.prenom
                                    email = acteur.email
                                    telephone = acteur.tel
                                    age = acteur.age.toString()
                                    gouvernorat = acteur.gouvernorat
                                    experience = acteur.experience.toString()
                                    instagram = acteur.socialLinks?.instagram ?: ""
                                    youtube = acteur.socialLinks?.youtube ?: ""
                                    tiktok = acteur.socialLinks?.tiktok ?: ""
                                    isLoading = false
                                }
                                result.onFailure { exception ->
                                    errorMessage = getErrorMessage(exception)
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = getErrorMessage(e)
                                isLoading = false
                            }
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(White)
            ) {
                // Contenu scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Message de succès
                successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(3000)
                        successMessage = null
                    }
                }
                
                // Message informatif si le profil n'a pas pu être chargé
                if (errorMessage == null && nom.isEmpty() && prenom.isEmpty() && email.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Remplissez vos informations pour compléter votre profil",
                            modifier = Modifier.padding(16.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Photo de profil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Photo de profil",
                            tint = DarkBlue,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                // Message d'édition
                if (isEditing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Mode édition activé - Modifiez vos informations",
                            modifier = Modifier.padding(12.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Informations personnelles
                Text(
                    text = "Informations personnelles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = nom,
                    onValueChange = { if (isEditing) nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { if (isEditing) prenom = it },
                    label = { Text("Prénom") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (isEditing) email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { if (isEditing) telephone = it },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (isEditing) age = it },
                    label = { Text("Âge") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = gouvernorat,
                    onValueChange = { if (isEditing) gouvernorat = it },
                    label = { Text("Gouvernorat") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = experience,
                    onValueChange = { if (isEditing) experience = it },
                    label = { Text("Années d'expérience") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                // Liens sociaux
                Text(
                    text = "Réseaux sociaux",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = instagram,
                    onValueChange = { if (isEditing) instagram = it },
                    label = { Text("Instagram") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Photo, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = youtube,
                    onValueChange = { if (isEditing) youtube = it },
                    label = { Text("YouTube") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.VideoLibrary, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = tiktok,
                    onValueChange = { if (isEditing) tiktok = it },
                    label = { Text("TikTok") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.MusicNote, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                // Bouton de déconnexion
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Déconnexion",
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Déconnexion",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                
                // Espace en bas pour le scroll
                Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Navbar en bas
            ProfileBottomNavigationBar(
                onHomeClick = onHomeClick,
                onAgendaClick = onAgendaClick,
                onHistoryClick = { showComingSoon = "Historique" },
                onProfileClick = { /* Déjà sur le profil */ },
                onAdvancedClick = { showComingSoon = "Fonctionnalité avancée" }
            )
        }
    }
    
    // Alerte Coming Soon
    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
}

@Composable
private fun ProfileBottomNavigationBar(
    onHomeClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdvancedClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Navbar flottante avec transparence
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = DarkBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                ProfileNavigationItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    onClick = onHomeClick,
                    isSelected = false
                )
                
                // Bouton + au milieu
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .clickable { onAdvancedClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Fonctionnalité avancée",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Profile
                ProfileNavigationItem(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    onClick = onProfileClick,
                    isSelected = true
                )
                
                // Espace pour équilibrer (remplace Historique)
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
private fun ProfileNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DarkBlue else GrayBorder,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) DarkBlue else GrayBorder,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

