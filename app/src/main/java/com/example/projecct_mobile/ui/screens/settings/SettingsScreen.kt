package com.example.projecct_mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.LightBlue
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.White

@Composable
fun SettingsScreen(
    role: String,
    onBackClick: () -> Unit = {},
    onMyProfileClick: (() -> Unit)? = null,
    onFavoritesClick: (() -> Unit)? = null,
    onPortfolioClick: (() -> Unit)? = null,
    onLocationClick: (() -> Unit)? = null,
    onApplicationSettingsClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null
) {
    var isActive by remember { mutableStateOf(true) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F5FB))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Surface(
                modifier = Modifier
                    .matchParentSize(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(DarkBlue, DarkBlueLight)
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }

                    Text(
                        text = if (role.equals("agency", ignoreCase = true)) "Paramètres agence" else "Paramètres",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(46.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(
                                elevation = 18.dp,
                                shape = CircleShape,
                                spotColor = White.copy(alpha = 0.25f)
                            )
                            .clip(CircleShape),
                        color = White.copy(alpha = 0.2f)
                    ) {}

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = DarkBlue,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (role.equals("agency", ignoreCase = true)) "Agence active" else "Utilisateur actif",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(White.copy(alpha = 0.18f))
                        .clickable { isActive = !isActive }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isActive) White else White.copy(alpha = 0.4f))
                        )
                        Text(
                            text = if (isActive) "Active" else "Inactive",
                            color = White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Changer le statut",
                        tint = White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingOptionRow(
                    label = "Mon profil",
                    subtitle = "Voir et modifier mes informations",
                    icon = Icons.Default.Person,
                    iconBackground = LightBlue.copy(alpha = 0.4f),
                    onClick = onMyProfileClick ?: { showComingSoon = "Mon profil" }
                )

                SettingOptionRow(
                    label = "Favoris",
                    subtitle = "Castings enregistrés",
                    icon = Icons.Default.Favorite,
                    iconBackground = Color(0xFFFFE2E7),
                    onClick = onFavoritesClick ?: { showComingSoon = "Favoris" }
                )

                SettingOptionRow(
                    label = "Mon portfolio",
                    subtitle = "Projets, photos et vidéos",
                    icon = Icons.Default.Work,
                    iconBackground = Color(0xFFE6ECFF),
                    onClick = onPortfolioClick ?: { showComingSoon = "Portfolio" }
                )

                SettingOptionRow(
                    label = "Localisation",
                    subtitle = "Gérer les lieux disponibles",
                    icon = Icons.Default.LocationOn,
                    iconBackground = Color(0xFFE5F8F1),
                    onClick = onLocationClick ?: { showComingSoon = "Localisation" }
                )

                SettingOptionRow(
                    label = "Réglages",
                    subtitle = "Notifications et préférences",
                    icon = Icons.Default.Settings,
                    iconBackground = Color(0xFFF6F0FF),
                    onClick = onApplicationSettingsClick ?: { showComingSoon = "Réglages" }
                )

                SettingOptionRow(
                    label = "Déconnexion",
                    subtitle = "Quitter mon compte CastMate",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconBackground = Color(0xFFFFECEE),
                    contentColor = Color(0xFFEA4D4D),
                    onClick = onLogoutClick ?: { showComingSoon = "Déconnexion" }
                )
            }
        }
    }

    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
}

@Composable
private fun SettingOptionRow(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackground: Color,
    contentColor: Color = DarkBlue,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = DarkBlue.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = GrayBorder,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ouvrir",
                tint = GrayBorder
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Projecct_MobileTheme {
        SettingsScreen(
            role = "actor"
        )
    }
}

