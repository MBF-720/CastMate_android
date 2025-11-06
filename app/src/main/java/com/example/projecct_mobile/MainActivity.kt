package com.example.projecct_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projecct_mobile.data.api.ApiClient
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.screens.auth.*
import com.example.projecct_mobile.ui.screens.auth.signup.*
import com.example.projecct_mobile.ui.screens.casting.*
import com.example.projecct_mobile.ui.screens.agenda.*
import com.example.projecct_mobile.ui.screens.map.*
import com.example.projecct_mobile.ui.screens.profile.*
import kotlinx.coroutines.launch

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
                    navController.navigate("home")
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                onActorClick = {
                    // Naviguer vers SignIn avec indication que c'est un acteur
                    navController.navigate("signIn?role=actor")
                },
                onAgencyClick = {
                    // Naviguer vers SignIn avec indication que c'est une agence
                    navController.navigate("signIn?role=agency")
                }
            )
        }
        
        composable(
            route = "signIn?role={role}",
            arguments = listOf(
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "actor"
                }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "actor"
            
            SignInScreen(
                onSignInClick = {
                    // Navigation vers la liste des castings après connexion
                    navController.navigate("castingList") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    // Naviguer vers l'inscription selon le rôle
                    if (role == "actor") {
                        navController.navigate("signUpActorStep1")
                    } else {
                        navController.navigate("signUp")
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate("forgotPassword")
                },
                onGoogleSignInClick = {
                    // Simulation Google SignIn - pré-remplir les données
                    // En production, ici vous récupéreriez les données Google
                    // Pour l'instant, on navigue vers l'étape 1 avec des valeurs par défaut
                    navController.navigate("signUpActorStep1")
                }
            )
        }
        
        composable("signUpActorStep1") {
            // TODO: En production, récupérer les données Google depuis un ViewModel ou un état partagé
            // Pour l'instant, on utilise des valeurs vides (sera pré-rempli si Google SignIn)
            SignUpActorStep1Screen(
                initialNom = "",
                initialPrenom = "",
                initialEmail = "",
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = { nom, prenom, age, email, telephone, gouvernorat, photoUrl ->
                    // Sauvegarder les données de l'étape 1 (vous pouvez utiliser un ViewModel ou un état partagé)
                    navController.navigate("signUpActorStep2")
                }
            )
        }
        
        composable("signUpActorStep2") {
            SignUpActorStep2Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = { anneesExperience, cvUrl, instagram, youtube, tiktok ->
                    // Sauvegarder les données de l'étape 2
                    navController.navigate("signUpActorStep3")
                }
            )
        }
        
        composable("signUpActorStep3") {
            SignUpActorStep3Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFinishClick = { centresInteret ->
                    // TODO: Envoyer toutes les données au backend pour créer le compte ACTEUR
                    // Après succès, naviguer vers la liste des castings
                    navController.navigate("castingList") {
                        popUpTo("home") { inclusive = true }
                    }
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
                    // Navigation simple vers les détails du casting
                    navController.navigate("castingDetail/${casting.id}")
                },
                onHomeClick = {
                    // Rester sur la liste (pas de navigation)
                },
                onHistoryClick = {
                    // TODO: Navigation vers l'historique (à implémenter)
                },
                onProfileClick = {
                    // Navigation vers le profil
                    navController.navigate("profile")
                },
                onAgendaClick = {
                    android.util.Log.d("MainActivity", "Navigation vers agenda déclenchée")
                    try {
                        navController.navigate("agenda")
                        android.util.Log.d("MainActivity", "Navigation vers agenda réussie")
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Erreur navigation agenda: ${e.message}", e)
                    }
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("agenda") {
            AgendaScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemClick = { casting ->
                    // Navigation simple vers les détails du casting
                    navController.navigate("castingDetail/${casting.id}")
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("filter") {
            FilterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onApplyFilter = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = "castingDetail/{castingId}",
            arguments = listOf(
                navArgument("castingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val castingId = backStackEntry.arguments?.getString("castingId") ?: ""
            var casting by remember { mutableStateOf<CastingItem?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorOccurred by remember { mutableStateOf(false) }
            val castingRepository = remember { CastingRepository() }
            val scope = rememberCoroutineScope()
            
            // Castings d'exemple pour les tests
            val exampleCastings = remember {
                mapOf(
                    "example_1" to CastingItem(
                        id = "example_1",
                        title = "Dune : Part 3",
                        date = "19,20,21/10/2026",
                        description = "Arven Talo, un jeune guerrier Fremen qui guide Paul et Chani à travers les défis émotionnels et physiques du désert. Ce rôle exige une présence intense et une capacité à exprimer la détermination et la vulnérabilité.",
                        role = "Arven",
                        age = "20-30 ans",
                        compensation = "20$",
                        isFavorite = true
                    ),
                    "example_2" to CastingItem(
                        id = "example_2",
                        title = "Keeper",
                        date = "25/11/2025",
                        description = "Un thriller intense sur un jeune garde de sécurité qui découvre un secret sombre dans le bâtiment qu'il surveille.",
                        role = "Garde de sécurité",
                        age = "18-25 ans",
                        compensation = "20$",
                        isFavorite = false
                    ),
                    "example_3" to CastingItem(
                        id = "example_3",
                        title = "Mutiny",
                        date = "15/12/2025",
                        description = "Un drame historique se déroulant lors d'une rébellion navale. Recherche d'acteurs pour des rôles de marins et d'officiers.",
                        role = "Marin",
                        age = "25-40 ans",
                        compensation = "20$",
                        isFavorite = true
                    ),
                    // Castings d'exemple de l'agenda
                    "1" to CastingItem(
                        id = "1",
                        title = "Dune : Part 3",
                        date = "30/10/2025",
                        description = "Paul Atreides faces new political and spiritual challenges...",
                        role = "Arven",
                        age = "20+",
                        compensation = "20$",
                        isFavorite = true
                    ),
                    "2" to CastingItem(
                        id = "2",
                        title = "Keeper",
                        date = "25/11/2025",
                        description = "An intense thriller about a young security guard...",
                        role = "men",
                        age = "18+",
                        compensation = "20$",
                        isFavorite = false
                    ),
                    "3" to CastingItem(
                        id = "3",
                        title = "Mutiny",
                        date = "15/12/2025",
                        description = "A historical drama set during a naval rebellion...",
                        role = "spy",
                        age = "30+",
                        compensation = "20$",
                        isFavorite = true
                    )
                )
            }
            
            // Charger le casting depuis l'API ou les données d'exemple
            LaunchedEffect(castingId) {
                if (castingId.isNotEmpty()) {
                    // Vérifier d'abord si c'est un casting d'exemple
                    if (exampleCastings.containsKey(castingId)) {
                        android.util.Log.d("MainActivity", "Utilisation du casting d'exemple: $castingId")
                        casting = exampleCastings[castingId]
                        isLoading = false
                    } else {
                        // Sinon, charger depuis l'API
                        isLoading = true
                        errorOccurred = false
                        try {
                            scope.launch {
                                val result = castingRepository.getCastingById(castingId)
                                result.onSuccess { apiCasting ->
                                    casting = apiCasting.toCastingItem()
                                    isLoading = false
                                }
                                result.onFailure { exception ->
                                    android.util.Log.e("MainActivity", "Erreur chargement casting: ${exception.message}", exception)
                                    // En cas d'erreur, créer un casting par défaut avec les données disponibles
                                    casting = CastingItem(
                                        id = castingId,
                                        title = "Casting non trouvé",
                                        date = "",
                                        description = "Impossible de charger les détails du casting",
                                        role = "",
                                        age = "",
                                        compensation = ""
                                    )
                                    isLoading = false
                                    errorOccurred = true
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Exception lors du chargement: ${e.message}", e)
                            // En cas d'exception, créer un casting par défaut
                            casting = CastingItem(
                                id = castingId,
                                title = "Casting non trouvé",
                                date = "",
                                description = "Erreur lors du chargement: ${e.message}",
                                role = "",
                                age = "",
                                compensation = ""
                            )
                            isLoading = false
                            errorOccurred = true
                        }
                    }
                } else {
                    // Si castingId est vide, on crée un casting par défaut
                    casting = CastingItem(
                        id = "",
                        title = "Casting non trouvé",
                        date = "",
                        description = "ID de casting invalide",
                        role = "",
                        age = "",
                        compensation = ""
                    )
                    isLoading = false
                }
            }
            
            // Afficher l'écran de chargement pendant le chargement
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = com.example.projecct_mobile.ui.theme.DarkBlue)
                        Text(
                            text = "Chargement...",
                            color = com.example.projecct_mobile.ui.theme.GrayBorder
                        )
                    }
                }
            } else {
                // Afficher l'écran de détails seulement si le casting est chargé
                val currentCasting = casting
                if (currentCasting != null) {
                    CastingDetailScreen(
                        casting = currentCasting,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onMapClick = {
                            navController.navigate("map")
                        },
                        onSubmitClick = {
                            // Action après soumission
                            navController.popBackStack()
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile")
                        },
                        onNavigateToHome = {
                            navController.navigate("castingList") {
                                popUpTo("castingList") { inclusive = false }
                            }
                        }
                    )
                } else {
                    // Si le casting est null et qu'on n'est plus en chargement, afficher un message d'erreur
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Impossible de charger le casting",
                                color = com.example.projecct_mobile.ui.theme.Red
                            )
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Retour")
                            }
                        }
                    }
                }
            }
        }
        
        composable("map") {
            MapScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemClick = { casting ->
                    // Navigation vers les détails du casting avec l'ID
                    navController.navigate("castingDetail/${casting.id}")
                },
                onHomeClick = {
                    navController.navigate("castingList") {
                        popUpTo("castingList") { inclusive = false }
                    }
                },
                onHistoryClick = {
                    // Action pour l'historique
                },
                onProfileClick = {
                    // Navigation vers le profil
                    navController.navigate("profile")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        composable("profile") {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditProfileClick = {
                    // TODO: Navigation vers l'écran d'édition du profil
                },
                onLogoutClick = {
                    // Déconnexion : supprimer le token et rediriger vers signIn
                    scope.launch {
                        val tokenManager = TokenManager(context)
                        tokenManager.clearToken()
                    }
                    navController.navigate("signIn") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }
    }
}