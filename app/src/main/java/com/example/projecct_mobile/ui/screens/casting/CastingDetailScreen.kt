package com.example.projecct_mobile.ui.screens.casting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*

@Composable
fun CastingDetailScreen(
    casting: CastingItem,
    onBackClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null
) {
    var isFavorite by remember { mutableStateOf(casting.isFavorite) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("overview", "Film", "production")

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête avec image/poster
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF8B4513), Color(0xFF6B3410))
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
                        text = casting.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    IconButton(onClick = {
                        isFavorite = !isFavorite
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) RedHeart else White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Contenu avec onglets
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Onglets
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = White,
                    contentColor = DarkBlue
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) DarkBlue else GrayBorder
                                )
                            }
                        )
                    }
                }

                // Contenu selon l'onglet sélectionné
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> OverviewContent(casting, onMapClick)
                        1 -> FilmContent(casting)
                        2 -> ProductionContent(casting)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton Submit
                    Button(
                        onClick = onSubmitClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkBlue
                        )
                    ) {
                        Text(
                            text = "Submit",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Barre de navigation du bas (Favorite, Home, Profile)
        DetailBottomNavigationBar(
            onFavoriteClick = { isFavorite = !isFavorite },
            onHomeClick = { onNavigateToHome?.invoke() },
            onProfileClick = { onNavigateToProfile?.invoke() },
            isFavorite = isFavorite
        )
    }
}

@Composable
fun OverviewContent(casting: CastingItem, onMapClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Description du rôle
        Text(
            text = "(Arabic) Special Training: Combat choreography provided",
            fontSize = 14.sp,
            color = GrayBorder,
            lineHeight = 20.sp
        )

        // Costume / Look
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Costume / Look",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "• Clothing: Neutral, fitted athletic wear for movement; bring one casual outfit.",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Shoes: Comfortable for running/jumping (sneakers or boots)",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Grooming: Hair tidy, minimal makeup, natural look",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Optional props: None required unless specified by casting",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }

        // Location
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Location",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Button(
                onClick = onMapClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                )
            ) {
                Text(
                    text = "Map",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = White
                )
            }
        }

        // Dates
        if (casting.date.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "dates",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrayBorder
                )
                Text(
                    text = casting.date,
                    fontSize = 16.sp,
                    color = Black
                )
            }
        }

        // Rôle
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "rôle",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GrayBorder
            )
            Text(
                text = "• Character Type: Supporting",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Gender/Age: Male, 20-30 years old",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Physical Traits: Athletic build, expressive eyes",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun FilmContent(casting: CastingItem) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Synopsis
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "synopsis",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe, he must prevent a terrible future only he can foresee.",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }

        // Réalisateur
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "realisateur",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "Denis Villeneuve - Known for \"Arrival\" and \"Blade Runner 2049\", brings his visionary storytelling to this epic continuation.",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }

        // Scénariste
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "scénariste",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "Jon Spaihts & Denis Villeneuve - Blending myth, politics, and emotion into a compelling sci-fi narrative.",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ProductionContent(casting: CastingItem) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Description
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "Warner Bros. Pictures is a leading film studio, founded in 1923 in Burbank, California. Known for producing blockbuster films and operating globally.",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }

        // Notable Productions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Notable Productions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Text(
                text = "• Dune (2021-2024)",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• The Dark Knight Trilogy",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Harry Potter & Fantastic Beasts series",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Inception",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
            Text(
                text = "• Joker",
                fontSize = 14.sp,
                color = GrayBorder,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun DetailBottomNavigationBar(
    onFavoriteClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    isFavorite: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(DarkBlue),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "Favorite",
                onClick = onFavoriteClick,
                tint = if (isFavorite) RedHeart else White
            )
            
            NavigationItem(
                icon = Icons.Default.Home,
                label = "Home",
                onClick = onHomeClick
            )
            
            NavigationItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = White
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CastingDetailScreenPreview() {
    Projecct_MobileTheme {
        CastingDetailScreen(
            casting = CastingItem(
                id = "1",
                title = "Dune : Part 3",
                date = "30/10/2025",
                description = "Paul Atreides faces new political and spiritual challenges as...",
                role = "Arven",
                age = "20+",
                compensation = "20$"
            )
        )
    }
}

