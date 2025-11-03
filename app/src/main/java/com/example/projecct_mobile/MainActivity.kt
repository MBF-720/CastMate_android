package com.example.projecct_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialiser l'API client
        ApiClient.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            Projecct_MobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationScreen()
                }
            }
        }
    }
}

@Composable
fun NavigationScreen() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToSignIn = {
                    navController.navigate("signIn")
                }
            )
        }
        
        composable("signIn") {
            SignInScreen(
                onSignInClick = {
                    // Navigation vers la liste des castings après connexion
                    navController.navigate("castingList")
                },
                onSignUpClick = {
                    navController.navigate("signUp")
                },
                onForgotPasswordClick = {
                    navController.navigate("forgotPassword")
                }
            )
        }
        
        composable("signUp") {
            SignUpScreen(
                onSignUpClick = {
                    // Navigation vers la liste des castings après inscription
                    navController.navigate("castingList")
                },
                onLoginClick = {
                    navController.navigate("signIn")
                }
            )
        }
        
        composable("forgotPassword") {
            ForgotPasswordScreen(
                onBackClick = {
                    navController.navigate("signIn")
                },
                onSubmitClick = {
                    // Après la soumission, retour à la connexion
                    navController.navigate("signIn")
                }
            )
        }
        
        composable("castingList") {
            CastingListScreen(
                onBackClick = {
                    navController.navigate("signIn")
                },
                onItemClick = { casting ->
                    // Action à définir selon les besoins
                    // Par exemple: naviguer vers les détails du casting
                },
                onHomeClick = {
                    // Rester sur la liste
                },
                onHistoryClick = {
                    // Action pour l'historique
                },
                onProfileClick = {
                    // Action pour le profil
                }
            )
        }
    }
}