package com.example.projecct_mobile.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.ui.screens.casting.CastingItem
import com.example.projecct_mobile.ui.screens.casting.CastingItemCard
import com.example.projecct_mobile.ui.screens.casting.BottomNavigationBar

@Composable
fun MapScreen(
    onBackClick: () -> Unit = {},
    onItemClick: (CastingItem) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToProfile: (() -> Unit)? = null
) {
    // Liste de castings pour la carte (exemple)
    val castings = remember {
        listOf(
            CastingItem(
                id = "1",
                title = "Dune : Part 3",
                date = "30/10/2025",
                description = "Paul Atreides faces new political and spiritual challenges...",
                role = "Arven",
                age = "20+",
                compensation = "20$"
            ),
            CastingItem(
                id = "2",
                title = "Keeper",
                date = "25/11/2025",
                description = "An intense thriller about a young security guard...",
                role = "men",
                age = "18+",
                compensation = "20$"
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                        text = "Carte",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    IconButton(onClick = { /* Filtres */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Carte placeholder et liste
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Placeholder pour la carte
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map",
                            tint = DarkBlue,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Carte interactive",
                            fontSize = 16.sp,
                            color = GrayBorder,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Liste des castings à proximité
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Castings à proximité",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(castings) { casting ->
                            CastingItemCard(
                                casting = casting,
                                onFavoriteClick = { /* Toggle favorite */ },
                                onItemClick = onItemClick
                            )
                        }
                    }
                }
            }
        }

        // Barre de navigation du bas
        BottomNavigationBar(
            onHomeClick = onHomeClick,
            onHistoryClick = onHistoryClick,
            onProfileClick = {
                onNavigateToProfile?.invoke() ?: onProfileClick()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    Projecct_MobileTheme {
        MapScreen()
    }
}

