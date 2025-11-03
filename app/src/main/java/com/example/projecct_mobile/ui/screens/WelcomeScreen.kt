package com.example.projecct_mobile.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.R
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit = {}
) {
    // Animation pour le logo (apparition)
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    
    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "logoAlpha"
    )
    
    // Animation pour la rotation du logo (plus douce)
    val logoRotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logoRotation"
    )
    
    // Animation de pulsation pour le logo (effet de respiration)
    val logoPulse by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoPulse"
    )
    
    // Animation pour le texte "Welcome to CasteMate"
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "textAlpha"
    )
    
    val textScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "textScale"
    )
    
    // Gestion de l'animation et navigation
    LaunchedEffect(Unit) {
        delay(6000) // Écran visible pendant 6 secondes
        onNavigateToSignIn() // Navigation vers SignIn
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Logo avec texte centré
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(logoAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo avec animation de rotation et pulsation (taille augmentée)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .scale(logoScale * logoPulse),
                contentAlignment = Alignment.Center
            ) {
                LogoWithAnimation(rotation = logoRotation)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Texte "Welcome to CasteMate"
            androidx.compose.material3.Text(
                text = "Welcome to CasteMate",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier
                    .scale(textScale)
                    .alpha(textAlpha)
            )
        }
    }
}

@Composable
fun LogoWithAnimation(rotation: Float) {
    // Logo avec rotation uniquement
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo",
        modifier = Modifier
            .size(300.dp)
            .rotate(rotation)
    )
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    Projecct_MobileTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen()
        }
    }
}

