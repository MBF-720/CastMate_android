package com.example.projecct_mobile.ui.screens.agence.casting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.model.Casting
import com.example.projecct_mobile.ui.theme.*

/**
 * Screen dédiée pour le chatbot d'agence pour un casting spécifique
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyChatbotScreen(
    casting: Casting,
    onBackClick: () -> Unit,
    onViewActorProfile: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chatbot IA",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Text(
                            text = casting.titre ?: "",
                            fontSize = 14.sp,
                            color = White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue
                )
            )
        },
        containerColor = White
    ) { paddingValues ->
        ChatbotContent(
            castingId = casting.actualId ?: "",
            castingTitle = casting.titre ?: "",
            casting = casting,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onViewActorProfile = onViewActorProfile
        )
    }
}

