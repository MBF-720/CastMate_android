package com.example.projecct_mobile.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userRepository = remember { UserRepository() }
    val scope = rememberCoroutineScope()

    // Charger les informations du profil utilisateur
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        try {
            val result = userRepository.getCurrentUser()

            result.onSuccess { userProfile ->
                user = userProfile
                isLoading = false
            }

            result.onFailure { exception ->
                errorMessage = "Erreur lors du chargement: ${exception.message}"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }

                    Text(
                        text = "Profil",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    IconButton(onClick = onEditProfileClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Photo de profil et informations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo de profil
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.photoProfil != null && user?.photoProfil?.isNotEmpty() == true) {
                            // TODO: Charger l'image depuis l'URL
                            Text("📷", fontSize = 48.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = DarkBlue,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nom complet
                    Text(
                        text = "${user?.prenom ?: ""} ${user?.nom ?: ""}".trim().ifEmpty { "Utilisateur" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rôle
                    Text(
                        text = user?.role?.name ?: "Non spécifié",
                        fontSize = 16.sp,
                        color = White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Contenu du profil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Erreur",
                            color = Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = userRepository.getCurrentUser()
                                isLoading = false
                                result.onSuccess { userProfile ->
                                    user = userProfile
                                }
                                result.onFailure { exception ->
                                    errorMessage = "Erreur: ${exception.message}"
                                }
                            }
                        }) {
                            Text("Réessayer")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Informations personnelles
                    ProfileSection(
                        title = "Informations personnelles",
                        items = listOf(
                            ProfileItem("Email", user?.email ?: "Non spécifié", Icons.Default.Email),
                            ProfileItem("Bio", user?.bio ?: "Aucune bio", Icons.Default.Info)
                        )
                    )

                    // CV
                    if (user?.cvUrl != null && user?.cvUrl?.isNotEmpty() == true) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LightBlue),
                            onClick = { /* Ouvrir le CV */ }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "CV",
                                        tint = DarkBlue
                                    )
                                    Text(
                                        text = "Voir mon CV",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = DarkBlue
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open",
                                    tint = DarkBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bouton de déconnexion
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Logout",
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
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        items.forEach { item ->
            ProfileItemRow(item = item)
        }
    }
}

data class ProfileItem(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ProfileItemRow(item: ProfileItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = DarkBlue
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = GrayBorder,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.value,
                    fontSize = 16.sp,
                    color = Black,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    Projecct_MobileTheme {
        ProfileScreen()
    }
}

