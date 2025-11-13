package com.example.projecct_mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.projecct_mobile.data.repository.AuthRepository
import com.example.projecct_mobile.data.repository.CastingRepository
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.utils.GoogleAuthUiClient
import com.google.android.gms.common.api.ApiException as GoogleApiException
import com.example.projecct_mobile.ui.screens.auth.*
import com.example.projecct_mobile.ui.screens.auth.signup.*
import com.example.projecct_mobile.ui.screens.casting.*
import com.example.projecct_mobile.ui.screens.agenda.*
import com.example.projecct_mobile.ui.screens.map.*
import com.example.projecct_mobile.ui.screens.profile.*
import com.example.projecct_mobile.ui.screens.acteur.*
import com.example.projecct_mobile.ui.screens.agence.auth.*
import com.example.projecct_mobile.ui.screens.agence.casting.*
import com.example.projecct_mobile.ui.screens.agence.profile.AgencyProfileScreen
import com.example.projecct_mobile.ui.screens.settings.SettingsScreen
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import kotlinx.coroutines.launch
import java.io.File

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedAuthRepository = remember { AuthRepository() }
    val googleAuthClient = remember { GoogleAuthUiClient(context) }
    var googleSignInLoading by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    
    // Stockage temporaire des données d'inscription acteur
    var actorSignupData by remember {
        mutableStateOf<ActorSignupData?>(null)
    }

    var agencySignupData by remember {
        mutableStateOf<AgencySignupData?>(null)
    }
    
    // Vérifier si l'utilisateur est déjà connecté (Remember Me)
    LaunchedEffect(Unit) {
        val tokenManager = TokenManager(context)
        val hasToken = tokenManager.hasToken()
        if (hasToken) {
            val role = tokenManager.getUserRoleSync()

            if (!role.isNullOrBlank() && role.equals("RECRUTEUR", ignoreCase = true)) {
                navController.navigate("agencyCastingList") {
                    popUpTo("welcome") { inclusive = true }
                }
            } else {
                navController.navigate("actorHome") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
        }
    }
    
    LaunchedEffect(googleSignInError) {
        googleSignInError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            googleSignInError = null
        }
    }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val accountResult = googleAuthClient.getAccountFromIntent(result.data)
        accountResult.onFailure { error ->
            googleSignInLoading = false
            googleSignInError = when (error) {
                is GoogleApiException -> when (error.statusCode) {
                    12501 -> "Connexion Google annulée" // SIGN_IN_CANCELLED
                    7 -> "Impossible de contacter Google. Vérifiez votre connexion."
                    else -> "Erreur Google (${error.statusCode})"
                }
                else -> "Connexion Google annulée"
            }
        }.onSuccess { account ->
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                googleSignInLoading = false
                googleSignInError = "Impossible de récupérer le token Google"
                return@rememberLauncherForActivityResult
            }
            
            scope.launch {
                val resultLogin = sharedAuthRepository.loginWithGoogle(idToken)
                resultLogin.onSuccess {
                    googleSignInLoading = false
                    actorSignupData = null
                    googleSignInError = null
                    navController.navigate("actorHome") {
                        popUpTo("home") { inclusive = true }
                    }
                }
                resultLogin.onFailure { exception ->
                    googleSignInLoading = false
                    when (exception) {
                        is ApiException.NotFoundException,
                        is ApiException.BadRequestException -> {
                            val prenom = account.givenName
                                ?: account.displayName?.split(" ")?.firstOrNull().orEmpty()
                            val nom = account.familyName
                                ?: account.displayName
                                    ?.takeIf { it.contains(" ") }
                                    ?.split(" ")
                                    ?.drop(1)
                                    ?.joinToString(" ")
                                    .orEmpty()
                            
                            actorSignupData = ActorSignupData(
                                nom = nom,
                                prenom = prenom,
                                age = 0,
                                email = account.email.orEmpty(),
                                motDePasse = "",
                                telephone = "",
                                gouvernorat = "",
                                photoProfil = account.photoUrl?.toString()
                            )
                            googleSignInError = "Complétez votre inscription"
                            navController.navigate("signUpActorStep1")
                        }
                        else -> {
                            googleSignInError = getErrorMessage(exception)
                        }
                    }
                }
            }
        }
        
    }
    
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
                    // Mode acteur : redirection vers l'écran de connexion générique
                    navController.navigate("signIn?role=actor")
                },
                onAgencyClick = {
                    // Mode agence : écran de connexion spécifique
                    navController.navigate("agencySignIn")
                }
            )
        }
        
        composable("agencySignIn") {
            SignInAgencyScreen(
                onSignInClick = {
                    // Après login agence (POST /auth/login), on arrive sur la liste des castings agence
                    navController.navigate("agencyCastingList") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    agencySignupData = null
                    navController.navigate("signUpAgencyStep1")
                },
                onForgotPasswordClick = {
                    navController.navigate("forgotPassword")
                },
                onGoogleSignInClick = {
                    // TODO: Implémenter Google Sign-In pour les agences si nécessaire
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("signUpAgencyStep1") {
            SignUpAgencyStep1Screen(
                initialNomAgence = agencySignupData?.nomAgence ?: "",
                initialNomResponsable = agencySignupData?.nomResponsable ?: "",
                initialEmail = agencySignupData?.email ?: "",
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = { nomAgence, nomResponsable, email, telephone, gouvernorat, motDePasse ->
                    // On mémorise les données de l'étape 1 avant de passer à l'étape 2
                    agencySignupData = AgencySignupData(
                        nomAgence = nomAgence,
                        nomResponsable = nomResponsable,
                        email = email,
                        telephone = telephone,
                        gouvernorat = gouvernorat,
                        motDePasse = motDePasse
                    )
                    navController.navigate("signUpAgencyStep2")
                }
            )
        }
        
        composable("signUpAgencyStep2") {
            val hasData = agencySignupData != null
            LaunchedEffect(hasData) {
                if (!hasData) {
                    navController.popBackStack("signUpAgencyStep1", inclusive = false)
                }
            }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            SignUpAgencyStep2Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onFinishClick = { siteWeb, description, logoFile, documentFile, facebook, instagram ->
                    val currentData = agencySignupData
                    if (currentData == null) {
                        navController.popBackStack("signUpAgencyStep1", inclusive = false)
                    } else {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                // Appel AuthRepository.signupAgence -> POST /agence/signup avec multipart
                                val result = sharedAuthRepository.signupAgence(
                                    nomAgence = currentData.nomAgence,
                                    responsable = currentData.nomResponsable,
                                    email = currentData.email,
                                    motDePasse = currentData.motDePasse,
                                    tel = currentData.telephone,
                                    gouvernorat = currentData.gouvernorat,
                                    siteWeb = siteWeb.takeIf { it.isNotBlank() },
                                    description = description,
                                    logoFile = logoFile,
                                    documentFile = documentFile,
                                    facebook = facebook,
                                    instagram = instagram
                                )
                                result.onSuccess {
                                    isLoading = false
                                    agencySignupData = currentData.copy(
                                        siteWeb = siteWeb.takeIf { it.isNotBlank() },
                                        description = description,
                                        logoUrl = null, // Les fichiers sont uploadés, pas besoin d'URL
                                        documentUrl = null // Les fichiers sont uploadés, pas besoin d'URL
                                    )
                                    // Après succès, on enchaîne sur l'écran de confirmation
                                    navController.navigate("agencyConfirmation") {
                                        popUpTo("signUpAgencyStep1") { inclusive = true }
                                    }
                                }
                                result.onFailure { exception ->
                                    isLoading = false
                                    errorMessage = getErrorMessage(exception)
                                    android.util.Log.e("MainActivity", "❌ Erreur inscription agence: ${exception.message}", exception)
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Erreur lors de l'inscription: ${e.message}"
                                android.util.Log.e("MainActivity", "❌ Exception inscription agence: ${e.message}", e)
                            }
                        }
                    }
                }
            )
        }
        
        composable("agencyConfirmation") {
            ConfirmationScreen(
                userRole = "agency",
                onNavigateToDestination = {
                    agencySignupData = null
                    navController.navigate("agencyCastingList") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        
        composable("agencyCastingList") {
            CastingListAgencyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemClick = { casting ->
                    navController.navigate("castingDetail/${casting.id}")
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
                onCreateCastingClick = {
                    navController.navigate("agencyCreateCasting")
                },
                onProfileClick = {
                    navController.navigate("agencyProfile")
                },
                onSettingsClick = {
                    navController.navigate("settings/agency")
                },
                onAgendaClick = {
                    navController.navigate("agenda")
                },
                onLogoutClick = {
                    // Déconnexion agence : on efface token + infos locales
                    scope.launch { sharedAuthRepository.logout() }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }
        
        composable("agencyCreateCasting") {
            val castingRepository = remember { CastingRepository() }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            
            CreateCastingScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                externalErrorMessage = errorMessage,
                onSaveCastingClick = { titre, descriptionRole, synopsis, dateDebut, dateFin, prix, types, age, conditions, lieu, afficheFile ->
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val result = castingRepository.createCasting(
                                titre = titre,
                                descriptionRole = descriptionRole,
                                synopsis = synopsis,
                                lieu = lieu,
                                dateDebut = dateDebut,
                                dateFin = dateFin,
                                prix = prix,
                                types = types,
                                age = age,
                                conditions = conditions,
                                afficheFile = afficheFile
                            )
                            result.onSuccess { casting ->
                                isLoading = false
                                android.util.Log.d("MainActivity", "✅ Casting créé avec succès: ${casting.titre}")
                                navController.popBackStack()
                            }
                            result.onFailure { exception ->
                                isLoading = false
                                errorMessage = getErrorMessage(exception)
                                android.util.Log.e("MainActivity", "❌ Erreur création casting: ${exception.message}", exception)
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Erreur lors de la création: ${e.message}"
                            android.util.Log.e("MainActivity", "❌ Exception création casting: ${e.message}", e)
                        }
                    }
                }
            )
        }
        
        composable("agencyProfile") {
            AgencyProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToCastings = {
                    navController.navigate("agencyCastingList") {
                        popUpTo("agencyCastingList") { inclusive = false }
                    }
                },
                onNavigateToCreateCasting = {
                    navController.navigate("agencyCreateCasting")
                },
                onLogoutClick = {
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = "settings/{role}",
            arguments = listOf(
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "actor"
                }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "actor"

            SettingsScreen(
                role = role,
                onBackClick = { navController.popBackStack() },
                onMyProfileClick = {
                    if (role.equals("agency", ignoreCase = true)) {
                        navController.navigate("agencyProfile")
                    } else {
                        navController.navigate("actorProfile")
                    }
                },
                onLogoutClick = {
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
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
                    if (role == "agency") {
                        navController.navigate("agencyCastingList") {
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        navController.navigate("actorHome") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                },
                onSignUpClick = {
                    // Naviguer vers l'inscription selon le rôle
                    if (role == "actor") {
                        navController.navigate("signUpActorStep1")
                    } else {
                        navController.navigate("signUpAgencyStep1")
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate("forgotPassword")
                },
                onGoogleSignInClick = {
                    if (!googleSignInLoading) {
                        googleSignInError = null
                        googleSignInLoading = true
                        googleAuthClient.signOut()
                        googleSignInLauncher.launch(googleAuthClient.getSignInIntent())
                    }
                },
                isGoogleLoading = googleSignInLoading,
                role = role
            )
        }
        
        composable("signUpActorStep1") {
            SignUpActorStep1Screen(
                initialNom = actorSignupData?.nom ?: "",
                initialPrenom = actorSignupData?.prenom ?: "",
                initialEmail = actorSignupData?.email ?: "",
                initialPhotoUrl = actorSignupData?.photoProfil,
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = { nom, prenom, age, email, motDePasse, telephone, gouvernorat, photoUrl ->
                    // Sauvegarder les données de l'étape 1
                    actorSignupData = ActorSignupData(
                        nom = nom,
                        prenom = prenom,
                        age = age.toIntOrNull() ?: 0,
                        email = email,
                        motDePasse = motDePasse,
                        telephone = telephone,
                        gouvernorat = gouvernorat,
                        photoProfil = photoUrl
                    )
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
                    // Mettre à jour les données de l'étape 2
                    actorSignupData = actorSignupData?.copy(
                        experience = anneesExperience.toIntOrNull() ?: 0,
                        cvPdf = cvUrl,
                        instagram = instagram.takeIf { it.isNotBlank() },
                        youtube = youtube.takeIf { it.isNotBlank() },
                        tiktok = tiktok.takeIf { it.isNotBlank() }
                    )
                    navController.navigate("signUpActorStep3")
                }
            )
        }
        
        composable("signUpActorStep3") {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val authRepository = sharedAuthRepository
            
            SignUpActorStep3Screen(
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onFinishClick = { centresInteret ->
                    val data = actorSignupData
                    if (data == null) {
                        errorMessage = "Données manquantes. Veuillez recommencer."
                        return@SignUpActorStep3Screen
                    }
                    
                    // Vérifier que toutes les données obligatoires sont présentes
                    if (data.nom.isBlank() || data.prenom.isBlank() || data.email.isBlank() 
                        || data.motDePasse.isBlank() || data.telephone.isBlank() 
                        || data.gouvernorat.isBlank() || data.age == 0 || data.experience == 0) {
                        errorMessage = "Veuillez remplir tous les champs obligatoires."
                        return@SignUpActorStep3Screen
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        try {
                            val photoFile = data.photoProfil?.let { File(it) }?.takeIf { it.exists() }
                            val documentFile = data.cvPdf?.let { File(it) }?.takeIf { it.exists() }

                            val result = authRepository.signupActeur(
                                nom = data.nom,
                                prenom = data.prenom,
                                email = data.email,
                                motDePasse = data.motDePasse,
                                tel = data.telephone,
                                age = data.age,
                                gouvernorat = data.gouvernorat,
                                experience = data.experience,
                                centresInteret = centresInteret.takeIf { it.isNotEmpty() },
                                photoFile = photoFile,
                                documentFile = documentFile,
                                instagram = data.instagram,
                                youtube = data.youtube,
                                tiktok = data.tiktok
                            )
                            
                            result.onSuccess {
                                isLoading = false
                                actorSignupData = null // Réinitialiser les données
                                navController.navigate("actorHome") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                            
                            result.onFailure { exception ->
                                isLoading = false
                                val message = com.example.projecct_mobile.ui.components.getErrorMessage(exception)
                                errorMessage = if (message.isNotBlank()) {
                                    message
                                } else {
                                    "Une erreur est survenue. Merci de réessayer."
                                }
                            }
                        } catch (e: com.example.projecct_mobile.data.model.ApiException) {
                            isLoading = false
                            errorMessage = com.example.projecct_mobile.ui.components.getErrorMessage(e)
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Une erreur inattendue est survenue: ${e.message ?: "erreur inconnue"}"
                        }
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
        
        composable("actorHome") {
            ActorHomeScreen(
                onCastingClick = { casting ->
                    navController.navigate("castingDetail/${casting.id}")
                },
                onProfileClick = {
                    navController.navigate("actorProfile")
                },
                onAgendaClick = {
                    navController.navigate("settings/actor")
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
                onHistoryClick = {
                    // Géré par l'alerte "coming soon" dans ActorHomeScreen
                },
                onLogoutClick = {
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }
        
        composable("castingList") {
            CastingListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onItemClick = { casting ->
                    navController.navigate("castingDetail/${casting.id}")
                },
                onHomeClick = {
                    navController.navigate("actorHome") {
                        popUpTo("actorHome") { inclusive = false }
                    }
                },
                onHistoryClick = {
                    // Géré par l'alerte "coming soon" dans CastingListScreen
                },
                onProfileClick = {
                    navController.navigate("actorProfile")
                },
                onSettingsClick = {
                    navController.navigate("settings/actor")
                },
                onFilterClick = {
                    // Géré par l'alerte "coming soon" dans CastingListScreen
                },
                onNavigateToProfile = {
                    navController.navigate("actorProfile")
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
        
        composable("actorProfile") {
            ActorProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                onHomeClick = {
                    navController.navigate("actorHome") {
                        popUpTo("actorHome") { inclusive = false }
                    }
                },
                onAgendaClick = {
                    navController.navigate("agenda")
                },
                onHistoryClick = {
                    // Géré par l'alerte "coming soon" dans la navbar
                }
            )
        }
        
        composable("profile") {
            val scope = rememberCoroutineScope()
            
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditProfileClick = {
                    navController.navigate("actorProfile")
                },
                onLogoutClick = {
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
            )
        }
    }
}

/**
 * Classe de données pour stocker temporairement les informations d'inscription d'un acteur
 */
private data class ActorSignupData(
    val nom: String,
    val prenom: String,
    val age: Int,
    val email: String,
    val motDePasse: String,
    val telephone: String,
    val gouvernorat: String,
    val photoProfil: String? = null,
    val experience: Int = 0,
    val cvPdf: String? = null,
    val instagram: String? = null,
    val youtube: String? = null,
    val tiktok: String? = null
)

private data class AgencySignupData(
    val nomAgence: String,
    val nomResponsable: String,
    val email: String,
    val telephone: String,
    val gouvernorat: String,
    val motDePasse: String,
    val siteWeb: String? = null,
    val description: String = "",
    val logoUrl: String? = null,
    val documentUrl: String? = null
)