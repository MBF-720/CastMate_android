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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.projecct_mobile.utils.GroqConfig
import com.google.android.gms.common.api.ApiException as GoogleApiException
import com.example.projecct_mobile.ui.screens.auth.*
import com.example.projecct_mobile.ui.screens.auth.signup.*
import com.example.projecct_mobile.ui.screens.auth.ResetPasswordScreen
import com.example.projecct_mobile.ui.screens.casting.*
import com.example.projecct_mobile.ui.screens.casting.CastingApplicationScreen
import com.example.projecct_mobile.ui.screens.agenda.*
import com.example.projecct_mobile.ui.screens.map.*
import com.example.projecct_mobile.ui.screens.profile.*
import com.example.projecct_mobile.ui.screens.acteur.ActorHomeScreen
import com.example.projecct_mobile.ui.screens.acteur.ActorProfileScreen
import com.example.projecct_mobile.ui.screens.acteur.ActorSettingsScreen
import com.example.projecct_mobile.ui.screens.acteur.ActorTrainingScreen
import com.example.projecct_mobile.ui.screens.acteur.FavoritesScreen
import com.example.projecct_mobile.ui.screens.acteur.MyCandidaturesScreen
import com.example.projecct_mobile.ui.screens.agence.auth.*
import com.example.projecct_mobile.ui.screens.agence.casting.*
import com.example.projecct_mobile.ui.screens.agence.profile.AgencyProfileScreen
import com.example.projecct_mobile.ui.screens.agence.profile.ActorProfileDetails
import com.example.projecct_mobile.ui.screens.settings.SettingsScreen
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.utils.EmailSender
import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.net.Uri

/**
 * Classe de données pour stocker temporairement les informations d'inscription d'un acteur
 */
data class ActorSignupData(
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

data class AgencySignupData(
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialiser l'API client
        ApiClient.initialize(this)
        
        // Initialiser GroqConfig pour charger la clé API depuis .env
        GroqConfig.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            Projecct_MobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationScreen(intent = intent)
                }
            }
        }
    }
}

@Composable
fun NavigationScreen(intent: android.content.Intent? = null) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedAuthRepository = remember { AuthRepository() }
    val googleAuthClient = remember { GoogleAuthUiClient(context) }
    var googleSignInLoading by remember { mutableStateOf(false) }
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var isGoogleSignInForAgency by remember { mutableStateOf(false) }
    
    // Gérer les deep links (réinitialisation de mot de passe)
    LaunchedEffect(intent) {
        val data = intent?.data
        if (data != null) {
            android.util.Log.d("DeepLink", "🔗 URI reçu: $data")
            if (data.scheme == "castmate" && data.host == "reset-password") {
                val token = data.getQueryParameter("token") ?: ""
                val email = data.getQueryParameter("email") ?: ""
                val type = data.getQueryParameter("type") ?: "actor"
                
                android.util.Log.d("DeepLink", "🔗 Paramètres: token=$token, email=$email, type=$type")
                
                if (token.isNotBlank() && email.isNotBlank()) {
                    android.util.Log.d("DeepLink", "✅ Navigation vers resetPassword")
                    navController.navigate("resetPassword/$token/$email/$type") {
                        popUpTo("home") { inclusive = false }
                    }
                } else {
                    android.util.Log.e("DeepLink", "❌ Token ou email manquant")
                }
            }
        }
    }
    
    // Stockage temporaire des données d'inscription acteur
    var actorSignupData by remember {
        mutableStateOf<ActorSignupData?>(null)
    }

    var agencySignupData by remember {
        mutableStateOf<AgencySignupData?>(null)
    }
    
    // Clé partagée pour forcer le rafraîchissement de la liste des castings après création
    var castingListRefreshKey by remember { mutableStateOf(0) }
    
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
    
    // Dialogue d'erreur pour Google Sign-In
    googleSignInError?.let { error ->
        AlertDialog(
            onDismissRequest = {
                googleSignInError = null
            },
            title = {
                Text(
                    text = "Erreur de connexion Google",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = error,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        googleSignInError = null
                    }
                ) {
                    Text(
                        "OK",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val accountResult = googleAuthClient.getAccountFromIntent(result.data)
        accountResult.onFailure { error ->
            googleSignInLoading = false
            googleSignInError = when (error) {
                is GoogleApiException -> {
                    android.util.Log.e("GoogleSignIn", "❌ Erreur Google API: ${error.statusCode}")
                    android.util.Log.e("GoogleSignIn", "Message: ${error.message}")
                    when (error.statusCode) {
                        10 -> {
                            // DEVELOPER_ERROR - Problème de configuration
                            android.util.Log.e("GoogleSignIn", "❌ Erreur 10: Configuration OAuth incorrecte")
                            android.util.Log.e("GoogleSignIn", "Vérifiez dans Google Cloud Console:")
                            android.util.Log.e("GoogleSignIn", "1. Client OAuth Android créé avec package: com.example.projecct_mobile")
                            android.util.Log.e("GoogleSignIn", "2. Client OAuth Web configuré")
                            android.util.Log.e("GoogleSignIn", "3. API Google Sign-In activée")
                            "Erreur de configuration Google Sign-In (10).\n\n" +
                            "Vérifiez dans Google Cloud Console:\n" +
                            "• Client OAuth Android avec package: com.example.projecct_mobile\n" +
                            "• Client OAuth Web configuré\n" +
                            "• API Google Sign-In activée"
                        }
                        12501 -> "Connexion Google annulée" // SIGN_IN_CANCELLED
                        7 -> "Impossible de contacter Google. Vérifiez votre connexion."
                        else -> "Erreur Google (${error.statusCode}): ${error.message}"
                    }
                }
                else -> {
                    android.util.Log.e("GoogleSignIn", "❌ Erreur inconnue: ${error.message}", error)
                    "Connexion Google annulée: ${error.message}"
                }
            }
        }.onSuccess { account ->
            val idToken = account.idToken
            // Si l'ID token n'est pas disponible, rediriger directement vers l'inscription
            // (Fallback si la configuration OAuth n'est pas complète)
            if (idToken.isNullOrBlank()) {
                googleSignInLoading = false
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
                googleSignInError = null
                navController.navigate("signUpActorStep1")
                return@rememberLauncherForActivityResult
            }
            
            scope.launch {
                try {
                    val resultLogin = sharedAuthRepository.loginWithGoogle(idToken)
                    resultLogin.onSuccess { authResponse ->
                        // Vérifier le rôle du compte
                        val userRole = authResponse.user?.role?.name
                        android.util.Log.d("GoogleSignIn", "🔍 Rôle récupéré: $userRole (user: ${authResponse.user}, role enum: ${authResponse.user?.role})")
                        
                        // Vérifier que le rôle est ACTEUR (acteur) et non RECRUTEUR (agence)
                        var finalRole = userRole
                        
                        // Si le rôle n'est pas disponible dans la réponse, vérifier depuis le TokenManager
                        if (finalRole.isNullOrBlank()) {
                            android.util.Log.w("GoogleSignIn", "⚠️ Rôle non disponible dans la réponse, vérification depuis TokenManager...")
                            val tokenManager = TokenManager(context)
                            finalRole = tokenManager.getUserRoleSync()
                            android.util.Log.d("GoogleSignIn", "🔍 Rôle depuis TokenManager: $finalRole")
                        }
                        
                        if (!finalRole.isNullOrBlank()) {
                            val isActeur = finalRole.equals("ACTEUR", ignoreCase = true)
                            val isRecruteur = finalRole.equals("RECRUTEUR", ignoreCase = true)
                            
                            android.util.Log.d("GoogleSignIn", "🔍 isActeur: $isActeur, isRecruteur: $isRecruteur")
                            
                            if (!isActeur) {
                                // Le compte connecté n'est pas un compte acteur
                                // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                                val tokenManager = TokenManager(context)
                                tokenManager.clearToken()
                                android.util.Log.d("GoogleSignIn", "🧹 TokenManager nettoyé car rôle incorrect: '$finalRole'")
                                
                                googleSignInLoading = false
                                val errorMsg = if (isRecruteur) {
                                    "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence ou créer un nouveau compte acteur."
                                } else {
                                    "Ce compte Google n'est pas associé à un compte acteur. Veuillez créer un nouveau compte acteur."
                                }
                                googleSignInError = errorMsg
                                android.util.Log.e("GoogleSignIn", "❌ ERREUR: $errorMsg - Rôle: '$finalRole'")
                                // Ne pas naviguer, juste afficher l'erreur
                                return@onSuccess
                            }
                        } else {
                            // Si le rôle n'est toujours pas disponible après vérification, bloquer la connexion
                            googleSignInLoading = false
                            val errorMsg = "Impossible de déterminer le type de compte. Veuillez réessayer."
                            googleSignInError = errorMsg
                            android.util.Log.e("GoogleSignIn", "❌ ERREUR: Rôle non disponible - $errorMsg")
                            return@onSuccess
                        }
                        
                        // Seulement naviguer si le rôle est ACTEUR
                        googleSignInLoading = false
                        actorSignupData = null
                        googleSignInError = null
                        android.util.Log.d("GoogleSignIn", "✅ Connexion réussie - Rôle ACTEUR confirmé")
                        navController.navigate("actorHome") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                    resultLogin.onFailure { exception ->
                        // Si le compte n'existe pas, créer automatiquement le compte
                        when (exception) {
                            is ApiException.NotFoundException,
                            is ApiException.BadRequestException -> {
                                try {
                                    // Vérifier que l'email est disponible (obligatoire)
                                    val email = account.email
                                    if (email.isNullOrBlank()) {
                                        googleSignInLoading = false
                                        googleSignInError = "Email Google non disponible. Veuillez utiliser un compte Google avec email."
                                        return@onFailure
                                    }
                                    
                                    android.util.Log.d("GoogleSignIn", "📧 Email Google: $email")
                                    
                                    // IMPORTANT: Vérifier d'abord si un mot de passe a été stocké pour ce compte Google
                                    // Si OUI, cela signifie qu'un compte existe déjà (acteur OU agence)
                                    // Dans ce cas, NE PAS créer un nouveau compte, mais essayer de se connecter
                                    val tokenManager = TokenManager(context)
                                    val existingPassword = tokenManager.getGoogleAccountPassword(email)
                                    
                                    if (!existingPassword.isNullOrBlank()) {
                                        // Un compte existe déjà avec cet email Google
                                        android.util.Log.d("GoogleSignIn", "⚠️ Un compte existe déjà avec cet email. Tentative de connexion avec mot de passe stocké...")
                                        
                                        // Essayer de se connecter avec le mot de passe stocké
                                        val resultLoginWithPassword = sharedAuthRepository.login(email, existingPassword, expectedRole = "ACTEUR")
                                        resultLoginWithPassword.onSuccess { authResponse ->
                                            // Vérifier le rôle du compte
                                            val userRole = authResponse.user?.role?.name
                                            android.util.Log.d("GoogleSignIn", "🔍 Rôle du compte existant: $userRole")
                                            
                                            if (userRole != null && !userRole.equals("ACTEUR", ignoreCase = true)) {
                                                // Le compte existe mais avec un autre rôle (probablement RECRUTEUR)
                                                val tokenManager = TokenManager(context)
                                                tokenManager.clearToken()
                                                android.util.Log.e("GoogleSignIn", "❌ ERREUR: Ce compte a le rôle '$userRole' au lieu de 'ACTEUR'")
                                                
                                                googleSignInLoading = false
                                                val errorMsg = if (userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                    "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence."
                                                } else {
                                                    "Ce compte Google est associé à un compte avec un rôle différent ($userRole). Veuillez utiliser la page de connexion appropriée."
                                                }
                                                googleSignInError = errorMsg
                                                android.util.Log.e("GoogleSignIn", "❌ $errorMsg")
                                                return@onFailure
                                            }
                                            
                                            // Le compte existe et le rôle est correct (ACTEUR)
                                            googleSignInLoading = false
                                            actorSignupData = null
                                            googleSignInError = null
                                            android.util.Log.d("GoogleSignIn", "✅ Connexion réussie avec le compte existant")
                                            navController.navigate("actorHome") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                        resultLoginWithPassword.onFailure { loginException ->
                                            googleSignInLoading = false
                                            val errorMsg = "Un compte existe avec cet email mais la connexion a échoué. Veuillez vous connecter manuellement avec votre mot de passe."
                                            android.util.Log.e("GoogleSignIn", "❌ $errorMsg", loginException)
                                            googleSignInError = errorMsg
                                        }
                                        
                                        // Ne pas continuer vers la création du compte
                                        return@onFailure
                                    }
                                    
                                    // Aucun compte n'existe avec cet email, on peut créer un nouveau compte
                                    android.util.Log.d("GoogleSignIn", "✅ Aucun compte existant détecté, création d'un nouveau compte acteur...")
                                    
                                    // Extraire les données de Google
                                    val prenom = account.givenName
                                        ?: account.displayName?.split(" ")?.firstOrNull()
                                        ?: "Utilisateur"
                                    val nom = account.familyName
                                        ?: account.displayName
                                            ?.takeIf { it.contains(" ") }
                                            ?.split(" ")
                                            ?.drop(1)
                                            ?.joinToString(" ")
                                        ?: "Google"
                                    
                                    android.util.Log.d("GoogleSignIn", "👤 Nom: $nom, Prénom: $prenom")
                                    
                                    // Générer un nouveau mot de passe déterministe
                                    val emailHash = email.hashCode().toString()
                                    val randomPassword = "Google_${emailHash}_CastMate"
                                    // Stocker le mot de passe pour les futures connexions
                                    tokenManager.saveGoogleAccountPassword(email, randomPassword)
                                    android.util.Log.d("GoogleSignIn", "🔑 Nouveau mot de passe généré et stocké pour: $email")
                                    
                                    // Télécharger la photo de profil si disponible (sur un thread IO)
                                    var photoFile: File? = null
                                    account.photoUrl?.toString()?.let { photoUrl ->
                                        try {
                                            android.util.Log.d("GoogleSignIn", "📷 Téléchargement photo depuis: $photoUrl")
                                            photoFile = withContext(Dispatchers.IO) {
                                                try {
                                                    // Télécharger l'image depuis l'URL
                                                    val url = java.net.URL(photoUrl)
                                                    val connection = url.openConnection() as java.net.HttpURLConnection
                                                    connection.connectTimeout = 10000 // 10 secondes
                                                    connection.readTimeout = 10000 // 10 secondes
                                                    connection.connect()
                                                    val inputStream = connection.inputStream
                                                    val photoCacheFile = File(context.cacheDir, "google_photo_${System.currentTimeMillis()}.jpg")
                                                    photoCacheFile.outputStream().use { output ->
                                                        inputStream.copyTo(output)
                                                    }
                                                    inputStream.close()
                                                    connection.disconnect()
                                                    android.util.Log.d("GoogleSignIn", "✅ Photo téléchargée: ${photoCacheFile.absolutePath}")
                                                    photoCacheFile
                                                } catch (e: Exception) {
                                                    android.util.Log.e("GoogleSignIn", "❌ Erreur téléchargement photo: ${e.message}", e)
                                                    throw e
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Si le téléchargement échoue, continuer sans photo
                                            android.util.Log.e("GoogleSignIn", "⚠️ Téléchargement photo échoué, continuation sans photo: ${e.message}")
                                            photoFile = null
                                        }
                                    }
                                    
                                    android.util.Log.d("GoogleSignIn", "🔄 Création du compte acteur...")
                                    
                                    // Créer automatiquement le compte avec des valeurs par défaut
                                    val resultSignup = sharedAuthRepository.signupActeur(
                                        nom = nom,
                                        prenom = prenom,
                                        email = email,
                                        motDePasse = randomPassword,
                                        tel = "00000000", // Valeur par défaut
                                        age = 18, // Valeur par défaut
                                        gouvernorat = "Tunis", // Valeur par défaut
                                        experience = 0, // Valeur par défaut
                                        centresInteret = null,
                                        photoFile = photoFile,
                                        documentFile = null,
                                        instagram = null,
                                        youtube = null,
                                        tiktok = null
                                    )
                                    
                                    resultSignup.onSuccess { authResponse ->
                                        // IMPORTANT: Vérifier le rôle après la création du compte
                                        val userRole = authResponse.user?.role?.name
                                        android.util.Log.d("GoogleSignIn", "🔍 Rôle retourné après création: $userRole (user: ${authResponse.user}, role enum: ${authResponse.user?.role})")
                                        
                                        // Vérifier que le rôle est ACTEUR (acteur) et non RECRUTEUR (agence)
                                        if (!userRole.isNullOrBlank() && !userRole.equals("ACTEUR", ignoreCase = true)) {
                                            // Le backend a créé le compte avec un mauvais rôle
                                            val tokenManager = TokenManager(context)
                                            tokenManager.clearToken()
                                            android.util.Log.e("GoogleSignIn", "❌ ERREUR: Backend a créé le compte avec le mauvais rôle: '$userRole' au lieu de 'ACTEUR'")
                                            
                                            googleSignInLoading = false
                                            val errorMsg = if (userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence ou créer un nouveau compte acteur."
                                            } else {
                                                "Erreur: Le compte a été créé avec un rôle incorrect ($userRole). Veuillez contacter le support ou créer le compte manuellement."
                                            }
                                            googleSignInError = errorMsg
                                            return@onSuccess
                                        }
                                        
                                        // Le compte a été créé avec succès
                                        // Vérifier si un token a été retourné dans la réponse
                                        if (!authResponse.accessToken.isNullOrBlank()) {
                                            // Si un token est retourné, on est déjà connecté
                                            android.util.Log.d("GoogleSignIn", "✅ Compte créé et connecté avec succès (token reçu, rôle: $userRole)")
                                            googleSignInLoading = false
                                            actorSignupData = null
                                            googleSignInError = null
                                            navController.navigate("actorHome") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        } else {
                                            // Si aucun token n'est retourné, essayer de se connecter avec email/mot de passe
                                            android.util.Log.d("GoogleSignIn", "⚠️ Compte créé sans token, tentative de connexion avec email/mot de passe...")
                                            val resultLoginAfterSignup = sharedAuthRepository.login(email, randomPassword, expectedRole = "ACTEUR")
                                            resultLoginAfterSignup.onSuccess { loginAuthResponse ->
                                                // Vérifier aussi le rôle après la connexion
                                                val loginRole = loginAuthResponse.user?.role?.name
                                                android.util.Log.d("GoogleSignIn", "🔍 Rôle retourné après login: $loginRole")
                                                
                                                if (!loginRole.isNullOrBlank() && !loginRole.equals("ACTEUR", ignoreCase = true)) {
                                                    // Le rôle n'est pas correct après la connexion
                                                    val tokenManager = TokenManager(context)
                                                    tokenManager.clearToken()
                                                    android.util.Log.e("GoogleSignIn", "❌ ERREUR: Rôle incorrect après login: '$loginRole' au lieu de 'ACTEUR'")
                                                    
                                                    googleSignInLoading = false
                                                    val errorMsg = if (loginRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                        "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence ou créer un nouveau compte acteur."
                                                    } else {
                                                        "Erreur: Le compte a le mauvais rôle ($loginRole). Veuillez contacter le support."
                                                    }
                                                    googleSignInError = errorMsg
                                                    return@onSuccess
                                                }
                                                
                                                googleSignInLoading = false
                                                actorSignupData = null
                                                googleSignInError = null
                                                android.util.Log.d("GoogleSignIn", "✅ Connexion réussie après création du compte (rôle: $loginRole)")
                                                navController.navigate("actorHome") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                            resultLoginAfterSignup.onFailure { loginException ->
                                                // Si la connexion échoue, informer l'utilisateur qu'il doit se connecter manuellement
                                                googleSignInLoading = false
                                                val errorMsg = "Compte créé avec succès ! Veuillez vous connecter avec votre email et mot de passe. Note : ce compte n'est pas encore lié à Google."
                                                android.util.Log.e("GoogleSignIn", "⚠️ $errorMsg", loginException)
                                                googleSignInError = errorMsg
                                            }
                                        }
                                    }
                                    
                                    resultSignup.onFailure { signupException ->
                                        val errorMsg = getErrorMessage(signupException)
                                        // Vérifier si c'est une erreur 409 (Conflict) - compte existe déjà
                                        val isConflict = signupException is ApiException.ConflictException || 
                                                       errorMsg.contains("409", ignoreCase = true) ||
                                                       errorMsg.contains("Conflict", ignoreCase = true) ||
                                                       errorMsg.contains("existe déjà", ignoreCase = true) ||
                                                       errorMsg.contains("already exists", ignoreCase = true) ||
                                                       (signupException.message?.contains("409", ignoreCase = true) == true) ||
                                                       (signupException.message?.contains("Conflict", ignoreCase = true) == true) ||
                                                       (signupException.message?.contains("existe déjà", ignoreCase = true) == true)
                                        
                                        if (isConflict) {
                                            android.util.Log.d("GoogleSignIn", "⚠️ Compte existe déjà (409), tentative de connexion automatique avec Google...")
                                            // Le compte existe déjà, essayer de se connecter directement avec Google
                                            // Ne pas réinitialiser googleSignInLoading ici, le garder en loading pendant la tentative
                                            val resultLoginExisting = sharedAuthRepository.loginWithGoogle(idToken)
                                            resultLoginExisting.onSuccess { authResponse ->
                                                // Vérifier le rôle du compte
                                                val userRole = authResponse.user?.role?.name
                                                if (userRole != null && !userRole.equals("ACTEUR", ignoreCase = true)) {
                                                    // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                                                    val tokenManager = TokenManager(context)
                                                    tokenManager.clearToken()
                                                    android.util.Log.d("GoogleSignIn", "🧹 TokenManager nettoyé car rôle incorrect: '$userRole'")
                                                    
                                                    googleSignInLoading = false
                                                    val errorMsg = if (userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                        "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence ou créer un nouveau compte acteur."
                                                    } else {
                                                        "Ce compte Google n'est pas associé à un compte acteur. Veuillez créer un nouveau compte acteur."
                                                    }
                                                    googleSignInError = errorMsg
                                                    android.util.Log.e("GoogleSignIn", "❌ $errorMsg - Rôle: $userRole")
                                                    return@onSuccess
                                                }
                                                
                                                googleSignInLoading = false
                                                actorSignupData = null
                                                googleSignInError = null
                                                android.util.Log.d("GoogleSignIn", "✅ Connexion réussie avec compte existant lié à Google")
                                                navController.navigate("actorHome") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                            resultLoginExisting.onFailure { loginException ->
                                                // Si la connexion Google échoue, vérifier si c'est une erreur 404 (compte non lié à Google)
                                                val loginErrorMsg = getErrorMessage(loginException)
                                                val isNotFound = loginException is ApiException.NotFoundException ||
                                                                loginErrorMsg.contains("404", ignoreCase = true) ||
                                                                loginErrorMsg.contains("non trouvé", ignoreCase = true) ||
                                                                loginErrorMsg.contains("not found", ignoreCase = true)
                                                
                                                if (isNotFound) {
                                                    // Le compte existe mais n'est pas lié à Google
                                                    // Essayer de se connecter avec email/mot de passe en récupérant le mot de passe stocké
                                                    // (cas où le compte a été créé via ce flow Google précédemment)
                                                    android.util.Log.d("GoogleSignIn", "⚠️ Compte non lié à Google (404), tentative de connexion avec email/mot de passe...")
                                                    
                                                    // Récupérer le mot de passe stocké pour ce compte Google
                                                    val tokenManager = TokenManager(context)
                                                    val storedPassword = tokenManager.getGoogleAccountPassword(email)
                                                    
                                                    if (storedPassword.isNullOrBlank()) {
                                                        // Aucun mot de passe stocké, le compte a probablement été créé manuellement
                                                        googleSignInLoading = false
                                                        val finalErrorMsg = "Un compte existe déjà avec cet email. Ce compte n'est pas encore lié à Google. Veuillez vous connecter avec votre mot de passe, puis liez votre compte Google dans les paramètres."
                                                        android.util.Log.e("GoogleSignIn", "❌ $finalErrorMsg")
                                                        googleSignInError = finalErrorMsg
                                                        return@onFailure
                                                    }
                                                    
                                                    android.util.Log.d("GoogleSignIn", "🔑 Utilisation du mot de passe stocké pour la connexion...")
                                                    val resultLoginWithPassword = sharedAuthRepository.login(email, storedPassword, expectedRole = "ACTEUR")
                                                    resultLoginWithPassword.onSuccess { authResponse ->
                                                        // Vérifier le rôle du compte
                                                        val userRole = authResponse.user?.role?.name
                                                        if (userRole != null && !userRole.equals("ACTEUR", ignoreCase = true)) {
                                                            // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                                                            val tokenManager = TokenManager(context)
                                                            tokenManager.clearToken()
                                                            android.util.Log.d("GoogleSignIn", "🧹 TokenManager nettoyé car rôle incorrect: '$userRole'")
                                                            
                                                            googleSignInLoading = false
                                                            val errorMsg = if (userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                                "Ce compte Google est associé à un compte agence. Veuillez vous connecter depuis la page agence ou créer un nouveau compte acteur."
                                                            } else {
                                                                "Ce compte Google n'est pas associé à un compte acteur. Veuillez créer un nouveau compte acteur."
                                                            }
                                                            googleSignInError = errorMsg
                                                            android.util.Log.e("GoogleSignIn", "❌ $errorMsg - Rôle: $userRole")
                                                            return@onSuccess
                                                        }
                                                        
                                                        googleSignInLoading = false
                                                        actorSignupData = null
                                                        googleSignInError = null
                                                        android.util.Log.d("GoogleSignIn", "✅ Connexion réussie avec email/mot de passe (compte créé via Google)")
                                                        navController.navigate("actorHome") {
                                                            popUpTo("home") { inclusive = true }
                                                        }
                                                    }
                                                    resultLoginWithPassword.onFailure { passwordLoginException ->
                                                        // Si la connexion avec mot de passe échoue aussi, le compte a probablement été créé manuellement
                                                        googleSignInLoading = false
                                                        val finalErrorMsg = "Un compte existe déjà avec cet email. Ce compte n'est pas encore lié à Google. Veuillez vous connecter avec votre mot de passe, puis liez votre compte Google dans les paramètres."
                                                        android.util.Log.e("GoogleSignIn", "❌ $finalErrorMsg", passwordLoginException)
                                                        googleSignInError = finalErrorMsg
                                                    }
                                                } else {
                                                    // Autre erreur lors de la connexion Google
                                                    googleSignInLoading = false
                                                    val finalErrorMsg = "Un compte existe déjà avec cet email. Erreur de connexion Google: $loginErrorMsg"
                                                    android.util.Log.e("GoogleSignIn", "❌ $finalErrorMsg", loginException)
                                                    googleSignInError = finalErrorMsg
                                                }
                                            }
                                        } else {
                                            googleSignInLoading = false
                                            val finalErrorMsg = "Erreur lors de la création du compte: $errorMsg"
                                            android.util.Log.e("GoogleSignIn", "❌ $finalErrorMsg", signupException)
                                            googleSignInError = finalErrorMsg
                                        }
                                    }
                                } catch (e: Exception) {
                                    googleSignInLoading = false
                                    val errorMsg = "Erreur lors de la création automatique du compte: ${e.message}"
                                    android.util.Log.e("GoogleSignIn", "❌ $errorMsg", e)
                                    googleSignInError = errorMsg
                                }
                            }
                            else -> {
                                googleSignInLoading = false
                                val errorMsg = getErrorMessage(exception)
                                android.util.Log.e("GoogleSignIn", "❌ Erreur de connexion Google: $errorMsg", exception)
                                googleSignInError = errorMsg
                            }
                        }
                    }
                } catch (e: Exception) {
                    googleSignInLoading = false
                    val errorMsg = "Erreur inattendue lors de la connexion Google: ${e.message}"
                    android.util.Log.e("GoogleSignIn", "❌ $errorMsg", e)
                    googleSignInError = errorMsg
                }
            }
        }
    }
    
    // Launcher séparé pour Google Sign-In des agences
    val agencyGoogleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val accountResult = googleAuthClient.getAccountFromIntent(result.data)
        accountResult.onFailure { error ->
            googleSignInLoading = false
            isGoogleSignInForAgency = false
            googleSignInError = when (error) {
                is GoogleApiException -> {
                    android.util.Log.e("GoogleSignIn", "❌ Erreur Google API (Agence): ${error.statusCode}")
                    android.util.Log.e("GoogleSignIn", "Message: ${error.message}")
                    when (error.statusCode) {
                        10 -> {
                            // DEVELOPER_ERROR - Problème de configuration
                            android.util.Log.e("GoogleSignIn", "❌ Erreur 10: Configuration OAuth incorrecte")
                            android.util.Log.e("GoogleSignIn", "Vérifiez dans Google Cloud Console:")
                            android.util.Log.e("GoogleSignIn", "1. Client OAuth Android créé avec package: com.example.projecct_mobile")
                            android.util.Log.e("GoogleSignIn", "2. Client OAuth Web configuré")
                            android.util.Log.e("GoogleSignIn", "3. API Google Sign-In activée")
                            "Erreur de configuration Google Sign-In (10).\n\n" +
                            "Vérifiez dans Google Cloud Console:\n" +
                            "• Client OAuth Android avec package: com.example.projecct_mobile\n" +
                            "• Client OAuth Web configuré\n" +
                            "• API Google Sign-In activée"
                        }
                        12501 -> "Connexion Google annulée"
                        7 -> "Impossible de contacter Google. Vérifiez votre connexion."
                        else -> "Erreur Google (${error.statusCode}): ${error.message}"
                    }
                }
                else -> {
                    android.util.Log.e("GoogleSignIn", "❌ Erreur inconnue (Agence): ${error.message}", error)
                    "Connexion Google annulée: ${error.message}"
                }
            }
        }.onSuccess { account ->
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                googleSignInLoading = false
                isGoogleSignInForAgency = false
                val nomResponsable = account.givenName
                    ?: account.displayName?.split(" ")?.firstOrNull().orEmpty()
                val nomAgence = account.familyName
                    ?: account.displayName
                        ?.takeIf { it.contains(" ") }
                        ?.split(" ")
                        ?.drop(1)
                        ?.joinToString(" ")
                        .orEmpty()
                    .takeIf { it.isNotBlank() } ?: account.displayName ?: "Agence"
                
                agencySignupData = AgencySignupData(
                    nomAgence = nomAgence,
                    nomResponsable = nomResponsable,
                    email = account.email.orEmpty(),
                    telephone = "",
                    gouvernorat = "",
                    motDePasse = "",
                    siteWeb = null,
                    description = "",
                    logoUrl = account.photoUrl?.toString(),
                    documentUrl = null
                )
                googleSignInError = null
                navController.navigate("signUpAgencyStep1")
                return@rememberLauncherForActivityResult
            }
            
            scope.launch {
                try {
                    val resultLogin = sharedAuthRepository.loginWithGoogle(idToken)
                    resultLogin.onSuccess { authResponse ->
                        // Vérifier le rôle du compte
                        val userRole = authResponse.user?.role?.name
                        if (userRole != null && !userRole.equals("RECRUTEUR", ignoreCase = true)) {
                            // Le compte connecté n'est pas un compte agence (RECRUTEUR)
                            // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                            val tokenManager = TokenManager(context)
                            tokenManager.clearToken()
                            android.util.Log.d("GoogleSignInAgency", "🧹 TokenManager nettoyé car rôle incorrect: '$userRole'")
                            
                            googleSignInLoading = false
                            isGoogleSignInForAgency = false
                            val errorMsg = if (userRole.equals("ACTEUR", ignoreCase = true)) {
                                "Ce compte Google est associé à un compte acteur. Veuillez vous connecter depuis la page acteur ou créer un nouveau compte agence."
                            } else {
                                "Ce compte Google n'est pas associé à un compte agence. Veuillez créer un nouveau compte agence."
                            }
                            googleSignInError = errorMsg
                            android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg - Rôle: $userRole")
                            return@onSuccess
                        }
                        
                        googleSignInLoading = false
                        isGoogleSignInForAgency = false
                        agencySignupData = null
                        googleSignInError = null
                        navController.navigate("agencyCastingList") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                    resultLogin.onFailure { exception ->
                        when (exception) {
                            is ApiException.NotFoundException,
                            is ApiException.BadRequestException -> {
                                try {
                                    val email = account.email
                                    if (email.isNullOrBlank()) {
                                        googleSignInLoading = false
                                        isGoogleSignInForAgency = false
                                        googleSignInError = "Email Google non disponible. Veuillez utiliser un compte Google avec email."
                                        return@onFailure
                                    }
                                    
                                    android.util.Log.d("GoogleSignInAgency", "📧 Email Google: $email")
                                    
                                    // IMPORTANT: Vérifier d'abord si un mot de passe a été stocké pour ce compte Google
                                    // Si OUI, cela signifie qu'un compte existe déjà (acteur OU agence)
                                    // Dans ce cas, NE PAS créer un nouveau compte, mais essayer de se connecter
                                    val tokenManager = TokenManager(context)
                                    val existingPassword = tokenManager.getGoogleAccountPassword(email)
                                    
                                    if (!existingPassword.isNullOrBlank()) {
                                        // Un compte existe déjà avec cet email Google
                                        android.util.Log.d("GoogleSignInAgency", "⚠️ Un compte existe déjà avec cet email. Tentative de connexion avec mot de passe stocké...")
                                        
                                        // Essayer de se connecter avec le mot de passe stocké
                                        val resultLoginWithPassword = sharedAuthRepository.login(email, existingPassword, expectedRole = "RECRUTEUR")
                                        resultLoginWithPassword.onSuccess { authResponse ->
                                            // Vérifier le rôle du compte
                                            val userRole = authResponse.user?.role?.name
                                            android.util.Log.d("GoogleSignInAgency", "🔍 Rôle du compte existant: $userRole")
                                            
                                            if (userRole != null && !userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                // Le compte existe mais avec un autre rôle (probablement ACTEUR)
                                                val tokenManager = TokenManager(context)
                                                tokenManager.clearToken()
                                                android.util.Log.e("GoogleSignInAgency", "❌ ERREUR: Ce compte a le rôle '$userRole' au lieu de 'RECRUTEUR'")
                                                
                                                googleSignInLoading = false
                                                isGoogleSignInForAgency = false
                                                val errorMsg = if (userRole.equals("ACTEUR", ignoreCase = true)) {
                                                    "Ce compte Google est associé à un compte acteur. Veuillez vous connecter depuis la page acteur."
                                                } else {
                                                    "Ce compte Google est associé à un compte avec un rôle différent ($userRole). Veuillez utiliser la page de connexion appropriée."
                                                }
                                                googleSignInError = errorMsg
                                                android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg")
                                                return@onFailure
                                            }
                                            
                                            // Le compte existe et le rôle est correct (RECRUTEUR)
                                            googleSignInLoading = false
                                            isGoogleSignInForAgency = false
                                            agencySignupData = null
                                            googleSignInError = null
                                            android.util.Log.d("GoogleSignInAgency", "✅ Connexion réussie avec le compte existant")
                                            navController.navigate("agencyCastingList") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                        resultLoginWithPassword.onFailure { loginException ->
                                            googleSignInLoading = false
                                            isGoogleSignInForAgency = false
                                            val errorMsg = "Un compte existe avec cet email mais la connexion a échoué. Veuillez vous connecter manuellement avec votre mot de passe."
                                            android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg", loginException)
                                            googleSignInError = errorMsg
                                        }
                                        
                                        // Ne pas continuer vers la création du compte
                                        return@onFailure
                                    }
                                    
                                    // Aucun compte n'existe avec cet email, on peut créer un nouveau compte
                                    android.util.Log.d("GoogleSignInAgency", "✅ Aucun compte existant détecté, création d'un nouveau compte agence...")
                                    
                                    val nomResponsable = account.givenName
                                        ?: account.displayName?.split(" ")?.firstOrNull()
                                        ?: "Responsable"
                                    val nomAgence = account.familyName
                                        ?: account.displayName
                                            ?.takeIf { it.contains(" ") }
                                            ?.split(" ")
                                            ?.drop(1)
                                            ?.joinToString(" ")
                                        ?: account.displayName
                                        ?: "Agence Google"
                                    
                                    android.util.Log.d("GoogleSignInAgency", "👤 Agence: $nomAgence, Responsable: $nomResponsable")
                                    
                                    // Générer un nouveau mot de passe déterministe
                                    val emailHash = email.hashCode().toString()
                                    val randomPassword = "Google_${emailHash}_CastMate"
                                    // Stocker le mot de passe pour les futures connexions
                                    tokenManager.saveGoogleAccountPassword(email, randomPassword)
                                    android.util.Log.d("GoogleSignInAgency", "🔑 Nouveau mot de passe généré et stocké pour: $email")
                                    
                                    var logoFile: File? = null
                                    account.photoUrl?.toString()?.let { photoUrl ->
                                        try {
                                            android.util.Log.d("GoogleSignInAgency", "📷 Téléchargement logo depuis: $photoUrl")
                                            logoFile = withContext(Dispatchers.IO) {
                                                try {
                                                    val url = java.net.URL(photoUrl)
                                                    val connection = url.openConnection() as java.net.HttpURLConnection
                                                    connection.connectTimeout = 10000
                                                    connection.readTimeout = 10000
                                                    connection.connect()
                                                    val inputStream = connection.inputStream
                                                    val logoCacheFile = File(context.cacheDir, "google_logo_${System.currentTimeMillis()}.jpg")
                                                    logoCacheFile.outputStream().use { output ->
                                                        inputStream.copyTo(output)
                                                    }
                                                    inputStream.close()
                                                    connection.disconnect()
                                                    android.util.Log.d("GoogleSignInAgency", "✅ Logo téléchargé: ${logoCacheFile.absolutePath}")
                                                    logoCacheFile
                                                } catch (e: Exception) {
                                                    android.util.Log.e("GoogleSignInAgency", "❌ Erreur téléchargement logo: ${e.message}", e)
                                                    throw e
                                                }
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("GoogleSignInAgency", "⚠️ Téléchargement logo échoué, continuation sans logo: ${e.message}")
                                            logoFile = null
                                        }
                                    }
                                    
                                    android.util.Log.d("GoogleSignInAgency", "🔄 Création du compte agence...")
                                    
                                    val resultSignup = sharedAuthRepository.signupAgence(
                                        nomAgence = nomAgence,
                                        responsable = nomResponsable,
                                        email = email,
                                        motDePasse = randomPassword,
                                        tel = "00000000",
                                        gouvernorat = "Tunis",
                                        siteWeb = null,
                                        description = "Agence créée via Google Sign-In",
                                        logoFile = logoFile,
                                        documentFile = null,
                                        facebook = null,
                                        instagram = null
                                    )
                                    
                                    resultSignup.onSuccess { authResponse ->
                                        // IMPORTANT: Vérifier le rôle après la création du compte
                                        val userRole = authResponse.user?.role?.name
                                        android.util.Log.d("GoogleSignInAgency", "🔍 Rôle retourné après création: $userRole (user: ${authResponse.user}, role enum: ${authResponse.user?.role})")
                                        
                                        // Vérifier que le rôle est RECRUTEUR (agence) et non ACTEUR (acteur)
                                        if (!userRole.isNullOrBlank() && !userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                            // Le backend a créé le compte avec un mauvais rôle
                                            val tokenManager = TokenManager(context)
                                            tokenManager.clearToken()
                                            android.util.Log.e("GoogleSignInAgency", "❌ ERREUR: Backend a créé le compte avec le mauvais rôle: '$userRole' au lieu de 'RECRUTEUR'")
                                            
                                            googleSignInLoading = false
                                            isGoogleSignInForAgency = false
                                            val errorMsg = "Erreur: Le compte a été créé avec un rôle incorrect ($userRole). Veuillez contacter le support ou créer le compte manuellement."
                                            googleSignInError = errorMsg
                                            return@onSuccess
                                        }
                                        
                                        if (!authResponse.accessToken.isNullOrBlank()) {
                                            android.util.Log.d("GoogleSignInAgency", "✅ Compte créé et connecté avec succès (token reçu, rôle: $userRole)")
                                            googleSignInLoading = false
                                            isGoogleSignInForAgency = false
                                            agencySignupData = null
                                            googleSignInError = null
                                            navController.navigate("agencyCastingList") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        } else {
                                            android.util.Log.d("GoogleSignInAgency", "⚠️ Compte créé sans token, tentative de connexion avec email/mot de passe...")
                                            val resultLoginAfterSignup = sharedAuthRepository.login(email, randomPassword, expectedRole = "RECRUTEUR")
                                            resultLoginAfterSignup.onSuccess { loginAuthResponse ->
                                                // Vérifier aussi le rôle après la connexion
                                                val loginRole = loginAuthResponse.user?.role?.name
                                                android.util.Log.d("GoogleSignInAgency", "🔍 Rôle retourné après login: $loginRole")
                                                
                                                if (!loginRole.isNullOrBlank() && !loginRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                    // Le rôle n'est pas correct après la connexion
                                                    val tokenManager = TokenManager(context)
                                                    tokenManager.clearToken()
                                                    android.util.Log.e("GoogleSignInAgency", "❌ ERREUR: Rôle incorrect après login: '$loginRole' au lieu de 'RECRUTEUR'")
                                                    
                                                    googleSignInLoading = false
                                                    isGoogleSignInForAgency = false
                                                    val errorMsg = if (loginRole.equals("ACTEUR", ignoreCase = true)) {
                                                        "Ce compte Google est associé à un compte acteur. Veuillez vous connecter depuis la page acteur ou créer un nouveau compte agence."
                                                    } else {
                                                        "Erreur: Le compte a le mauvais rôle ($loginRole). Veuillez contacter le support."
                                                    }
                                                    googleSignInError = errorMsg
                                                    return@onSuccess
                                                }
                                                
                                                googleSignInLoading = false
                                                isGoogleSignInForAgency = false
                                                agencySignupData = null
                                                googleSignInError = null
                                                android.util.Log.d("GoogleSignInAgency", "✅ Connexion réussie après création du compte (rôle: $loginRole)")
                                                navController.navigate("agencyCastingList") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                            resultLoginAfterSignup.onFailure { loginException ->
                                                googleSignInLoading = false
                                                isGoogleSignInForAgency = false
                                                val errorMsg = "Compte créé avec succès ! Veuillez vous connecter avec votre email et mot de passe. Note : ce compte n'est pas encore lié à Google."
                                                android.util.Log.e("GoogleSignInAgency", "⚠️ $errorMsg", loginException)
                                                googleSignInError = errorMsg
                                            }
                                        }
                                    }
                                    
                                    resultSignup.onFailure { signupException ->
                                        val errorMsg = getErrorMessage(signupException)
                                        val isConflict = signupException is ApiException.ConflictException || 
                                                       errorMsg.contains("409", ignoreCase = true) ||
                                                       errorMsg.contains("Conflict", ignoreCase = true) ||
                                                       errorMsg.contains("existe déjà", ignoreCase = true) ||
                                                       errorMsg.contains("already exists", ignoreCase = true) ||
                                                       (signupException.message?.contains("409", ignoreCase = true) == true) ||
                                                       (signupException.message?.contains("Conflict", ignoreCase = true) == true) ||
                                                       (signupException.message?.contains("existe déjà", ignoreCase = true) == true)
                                        
                                        if (isConflict) {
                                            android.util.Log.d("GoogleSignInAgency", "⚠️ Compte existe déjà (409), tentative de connexion automatique avec Google...")
                                            val resultLoginExisting = sharedAuthRepository.loginWithGoogle(idToken)
                                            resultLoginExisting.onSuccess { authResponse ->
                                                // Vérifier le rôle du compte
                                                val userRole = authResponse.user?.role?.name
                                                if (userRole != null && !userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                    // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                                                    val tokenManager = TokenManager(context)
                                                    tokenManager.clearToken()
                                                    android.util.Log.d("GoogleSignInAgency", "🧹 TokenManager nettoyé car rôle incorrect: '$userRole'")
                                                    
                                                    googleSignInLoading = false
                                                    isGoogleSignInForAgency = false
                                                    val errorMsg = if (userRole.equals("ACTEUR", ignoreCase = true)) {
                                                        "Ce compte Google est associé à un compte acteur. Veuillez vous connecter depuis la page acteur ou créer un nouveau compte agence."
                                                    } else {
                                                        "Ce compte Google n'est pas associé à un compte agence. Veuillez créer un nouveau compte agence."
                                                    }
                                                    googleSignInError = errorMsg
                                                    android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg - Rôle: $userRole")
                                                    return@onSuccess
                                                }
                                                
                                                googleSignInLoading = false
                                                isGoogleSignInForAgency = false
                                                agencySignupData = null
                                                googleSignInError = null
                                                android.util.Log.d("GoogleSignInAgency", "✅ Connexion réussie avec compte existant lié à Google")
                                                navController.navigate("agencyCastingList") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                            resultLoginExisting.onFailure { loginException ->
                                                val loginErrorMsg = getErrorMessage(loginException)
                                                val isNotFound = loginException is ApiException.NotFoundException ||
                                                                loginErrorMsg.contains("404", ignoreCase = true) ||
                                                                loginErrorMsg.contains("non trouvé", ignoreCase = true) ||
                                                                loginErrorMsg.contains("not found", ignoreCase = true)
                                                
                                                if (isNotFound) {
                                                    android.util.Log.d("GoogleSignInAgency", "⚠️ Compte non lié à Google (404), tentative de connexion avec email/mot de passe...")
                                                    
                                                    val tokenManager = TokenManager(context)
                                                    val storedPassword = tokenManager.getGoogleAccountPassword(email)
                                                    
                                                    if (storedPassword.isNullOrBlank()) {
                                                        googleSignInLoading = false
                                                        isGoogleSignInForAgency = false
                                                        val finalErrorMsg = "Un compte existe déjà avec cet email. Ce compte n'est pas encore lié à Google. Veuillez vous connecter avec votre mot de passe, puis liez votre compte Google dans les paramètres."
                                                        android.util.Log.e("GoogleSignInAgency", "❌ $finalErrorMsg")
                                                        googleSignInError = finalErrorMsg
                                                        return@onFailure
                                                    }
                                                    
                                                    android.util.Log.d("GoogleSignInAgency", "🔑 Utilisation du mot de passe stocké pour la connexion...")
                                                    val resultLoginWithPassword = sharedAuthRepository.login(email, storedPassword, expectedRole = "RECRUTEUR")
                                                    resultLoginWithPassword.onSuccess { authResponse ->
                                                        // Vérifier le rôle du compte
                                                        val userRole = authResponse.user?.role?.name
                                                        if (userRole != null && !userRole.equals("RECRUTEUR", ignoreCase = true)) {
                                                            // IMPORTANT: Nettoyer le TokenManager car le rôle a été sauvegardé avant la vérification
                                                            val tokenManager = TokenManager(context)
                                                            tokenManager.clearToken()
                                                            android.util.Log.d("GoogleSignInAgency", "🧹 TokenManager nettoyé car rôle incorrect: '$userRole'")
                                                            
                                                            googleSignInLoading = false
                                                            isGoogleSignInForAgency = false
                                                            val errorMsg = if (userRole.equals("ACTEUR", ignoreCase = true)) {
                                                                "Ce compte Google est associé à un compte acteur. Veuillez vous connecter depuis la page acteur ou créer un nouveau compte agence."
                                                            } else {
                                                                "Ce compte Google n'est pas associé à un compte agence. Veuillez créer un nouveau compte agence."
                                                            }
                                                            googleSignInError = errorMsg
                                                            android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg - Rôle: $userRole")
                                                            return@onSuccess
                                                        }
                                                        
                                                        googleSignInLoading = false
                                                        isGoogleSignInForAgency = false
                                                        agencySignupData = null
                                                        googleSignInError = null
                                                        android.util.Log.d("GoogleSignInAgency", "✅ Connexion réussie avec email/mot de passe (compte créé via Google)")
                                                        navController.navigate("agencyCastingList") {
                                                            popUpTo("home") { inclusive = true }
                                                        }
                                                    }
                                                    resultLoginWithPassword.onFailure { passwordLoginException ->
                                                        googleSignInLoading = false
                                                        isGoogleSignInForAgency = false
                                                        val finalErrorMsg = "Un compte existe déjà avec cet email. Ce compte n'est pas encore lié à Google. Veuillez vous connecter avec votre mot de passe, puis liez votre compte Google dans les paramètres."
                                                        android.util.Log.e("GoogleSignInAgency", "❌ $finalErrorMsg", passwordLoginException)
                                                        googleSignInError = finalErrorMsg
                                                    }
                                                } else {
                                                    googleSignInLoading = false
                                                    isGoogleSignInForAgency = false
                                                    val finalErrorMsg = "Un compte existe déjà avec cet email. Erreur de connexion Google: $loginErrorMsg"
                                                    android.util.Log.e("GoogleSignInAgency", "❌ $finalErrorMsg", loginException)
                                                    googleSignInError = finalErrorMsg
                                                }
                                            }
                                        } else {
                                            googleSignInLoading = false
                                            isGoogleSignInForAgency = false
                                            val finalErrorMsg = "Erreur lors de la création du compte: $errorMsg"
                                            android.util.Log.e("GoogleSignInAgency", "❌ $finalErrorMsg", signupException)
                                            googleSignInError = finalErrorMsg
                                        }
                                    }
                                } catch (e: Exception) {
                                    googleSignInLoading = false
                                    isGoogleSignInForAgency = false
                                    val errorMsg = "Erreur lors de la création automatique du compte: ${e.message}"
                                    android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg", e)
                                    googleSignInError = errorMsg
                                }
                            }
                            else -> {
                                googleSignInLoading = false
                                isGoogleSignInForAgency = false
                                val errorMsg = getErrorMessage(exception)
                                android.util.Log.e("GoogleSignInAgency", "❌ Erreur de connexion Google: $errorMsg", exception)
                                googleSignInError = errorMsg
                            }
                        }
                    }
                } catch (e: Exception) {
                    googleSignInLoading = false
                    isGoogleSignInForAgency = false
                    val errorMsg = "Erreur inattendue lors de la connexion Google: ${e.message}"
                    android.util.Log.e("GoogleSignInAgency", "❌ $errorMsg", e)
                    googleSignInError = errorMsg
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
                    navController.navigate("forgotPassword/agency")
                },
                onGoogleSignInClick = {
                    if (!googleSignInLoading) {
                        googleSignInError = null
                        googleSignInLoading = true
                        isGoogleSignInForAgency = true
                        googleAuthClient.signOut()
                        agencyGoogleSignInLauncher.launch(googleAuthClient.getSignInIntent())
                    }
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
                },
                refreshTrigger = castingListRefreshKey
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
                onSaveCastingClick = { titre, descriptionRole, synopsis, dateDebut, dateFin, prix, types, age, ouvert, conditions, lieu, afficheFile ->
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
                                ouvert = ouvert,
                                conditions = conditions,
                                afficheFile = afficheFile
                            )
                            result.onSuccess { casting ->
                                isLoading = false
                                android.util.Log.d("MainActivity", "✅ Casting créé avec succès: ${casting.titre}")
                                android.util.Log.d("MainActivity", "🔄 Rafraîchissement de la liste des castings...")
                                
                                // Incrémenter la clé partagée pour forcer le rafraîchissement de la liste
                                castingListRefreshKey++
                                android.util.Log.d("MainActivity", "🔄 Clé de rafraîchissement incrémentée: $castingListRefreshKey")
                                
                                // Retourner à la liste des castings
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

        // Route pour éditer un casting
        composable(
            route = "agencyEditCasting/{castingId}",
            arguments = listOf(
                navArgument("castingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val castingId = backStackEntry.arguments?.getString("castingId")
            android.util.Log.d("MainActivity", "📝 Édition du casting ID: $castingId")
            
            val castingRepository = remember { CastingRepository() }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var casting by remember { mutableStateOf<com.example.projecct_mobile.data.model.Casting?>(null) }
            var isLoadingCasting by remember { mutableStateOf(true) }
            
            // Charger le casting existant
            LaunchedEffect(castingId) {
                if (castingId != null) {
                    isLoadingCasting = true
                    try {
                        val result = castingRepository.getCastingById(castingId)
                        result.onSuccess { loadedCasting ->
                            casting = loadedCasting
                            android.util.Log.d("MainActivity", "✅ Casting chargé pour édition: ${loadedCasting.titre}")
                            isLoadingCasting = false
                        }
                        result.onFailure { exception ->
                            android.util.Log.e("MainActivity", "❌ Erreur chargement casting: ${exception.message}", exception)
                            errorMessage = "Erreur lors du chargement du casting: ${exception.message}"
                            isLoadingCasting = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "❌ Exception chargement casting: ${e.message}", e)
                        errorMessage = "Erreur lors du chargement: ${e.message}"
                        isLoadingCasting = false
                    }
                }
            }
            
            if (isLoadingCasting) {
                // Afficher un indicateur de chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else if (casting != null) {
                // Afficher l'écran de modification avec le casting pré-rempli
                CreateCastingScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    externalErrorMessage = errorMessage,
                    existingCasting = casting,
                    onSaveCastingClick = { titre, descriptionRole, synopsis, dateDebut, dateFin, prix, types, age, ouvert, conditions, lieu, afficheFile ->
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val result = castingRepository.updateCasting(
                                    id = castingId!!,
                                    titre = titre,
                                    descriptionRole = descriptionRole,
                                    synopsis = synopsis,
                                    lieu = lieu,
                                    dateDebut = dateDebut,
                                    dateFin = dateFin,
                                    prix = prix,
                                    types = types,
                                    age = age,
                                    ouvert = ouvert,
                                    conditions = conditions,
                                    afficheFile = afficheFile
                                )
                                result.onSuccess { updatedCasting ->
                                    isLoading = false
                                    android.util.Log.d("MainActivity", "✅ Casting modifié avec succès: ${updatedCasting.titre}")
                                    android.util.Log.d("MainActivity", "🔄 Rafraîchissement de la liste des castings...")
                                    
                                    // Incrémenter la clé partagée pour forcer le rafraîchissement de la liste
                                    castingListRefreshKey++
                                    android.util.Log.d("MainActivity", "🔄 Clé de rafraîchissement incrémentée: $castingListRefreshKey")
                                    
                                    // Retourner à la liste des castings
                                    navController.popBackStack()
                                }
                                result.onFailure { exception ->
                                    isLoading = false
                                    errorMessage = getErrorMessage(exception)
                                    android.util.Log.e("MainActivity", "❌ Erreur modification casting: ${exception.message}", exception)
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Erreur lors de la modification: ${e.message}"
                                android.util.Log.e("MainActivity", "❌ Exception modification casting: ${e.message}", e)
                            }
                        }
                    }
                )
            } else {
                // Afficher un message d'erreur si le casting n'a pas pu être chargé
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Casting introuvable",
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                        ) {
                            Text("Retour")
                        }
                    }
                }
            }
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

            if (role.equals("agency", ignoreCase = true)) {
                // Paramètres pour les agences
            SettingsScreen(
                role = role,
                onBackClick = { navController.popBackStack() },
                onMyProfileClick = {
                        navController.navigate("agencyProfile")
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
                    } else {
                // Paramètres pour les acteurs
                ActorSettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onMyProfileClick = {
                        navController.navigate("actorProfile")
                    },
                    onFavoritesClick = {
                        navController.navigate("favorites")
                    },
                    onMyCandidaturesClick = {
                        navController.navigate("myCandidatures")
                    },
                    onTrainingClick = {
                        navController.navigate("actorTraining")
                    },
                    onSettingsClick = {
                        // TODO: Naviguer vers les réglages de l'application
                        android.util.Log.d("MainActivity", "Réglages - À implémenter")
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
                            popUpTo("actorHome") { inclusive = true }
                        }
                    },
                    onAgendaClick = {
                        navController.navigate("agenda")
                    },
                    onProfileClick = {
                        // Déjà sur la page de profil
                    }
                )
            }
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
                    // Naviguer vers forgotPassword avec le rôle approprié
                    navController.navigate("forgotPassword/${role}")
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
        
        composable(
            route = "resetPassword/{token}/{email}/{type}",
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                },
                navArgument("email") {
                    type = NavType.StringType
                },
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "actor"
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val userType = backStackEntry.arguments?.getString("type") ?: "actor"
            
            ResetPasswordScreen(
                token = token,
                email = email,
                userType = userType,
                onBackClick = {
                    navController.popBackStack()
                },
                onSuccess = {
                    // Après succès, retourner à la page de connexion appropriée
                    val isAgency = userType.equals("RECRUTEUR", ignoreCase = true) || 
                                  userType.equals("agency", ignoreCase = true) ||
                                  userType.equals("agence", ignoreCase = true)
                    
                    if (isAgency) {
                        navController.navigate("agencySignIn") {
                            popUpTo("home") { inclusive = false }
                        }
                    } else {
                        navController.navigate("signIn?role=actor") {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                }
            )
        }
        
        composable(
            route = "forgotPassword/{role}",
            arguments = listOf(
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = "actor"
                }
            )
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "actor"
            val isAgency = role.equals("agency", ignoreCase = true) || 
                          role.equals("recruteur", ignoreCase = true) ||
                          role.equals("agence", ignoreCase = true)
            
            // États pour gérer l'envoi d'email
            var forgotPasswordLoading by remember { mutableStateOf(false) }
            var forgotPasswordError by remember { mutableStateOf<String?>(null) }
            var forgotPasswordException by remember { mutableStateOf<Throwable?>(null) }
            var forgotPasswordSuccess by remember { mutableStateOf<String?>(null) }
            var showErrorDialog by remember { mutableStateOf(false) }
            
            // Afficher le dialogue d'erreur détaillé
            if (showErrorDialog && forgotPasswordError != null) {
                com.example.projecct_mobile.ui.components.ErrorDetailDialog(
                    title = if (isAgency) "Erreur" else "Error",
                    message = forgotPasswordError ?: "",
                    exception = forgotPasswordException,
                    isAgency = isAgency,
                    onDismiss = {
                        showErrorDialog = false
                        forgotPasswordError = null
                        forgotPasswordException = null
                    }
                )
            }
            
            ForgotPasswordScreen(
                onBackClick = {
                    // Retourner vers la bonne page de connexion selon le rôle
                    if (isAgency) {
                        navController.navigate("agencySignIn") {
                            popUpTo("agencySignIn") { inclusive = false }
                        }
                    } else {
                        navController.navigate("signIn?role=actor") {
                            popUpTo("signIn") { inclusive = false }
                        }
                    }
                },
                onSubmitClick = {
                    // Après la soumission, retour à la connexion appropriée
                    if (isAgency) {
                        navController.navigate("agencySignIn") {
                            popUpTo("agencySignIn") { inclusive = false }
                        }
                    } else {
                        navController.navigate("signIn?role=actor") {
                            popUpTo("signIn") { inclusive = false }
                        }
                    }
                },
                userRole = role,
                onForgotPassword = { email ->
                    // ⚠️ OPTION A : Envoi depuis Android (NON RECOMMANDÉ - credentials exposés)
                    // ✅ OPTION B : Appel API backend (RECOMMANDÉ - sécurisé)
                    
                    // Changez USE_ANDROID_EMAIL_SENDER selon votre choix
                    val USE_ANDROID_EMAIL_SENDER = true // false = utilise le backend
                    
                    scope.launch {
                        try {
                            forgotPasswordLoading = true
                            forgotPasswordError = null
                            forgotPasswordException = null
                            
                            android.util.Log.d("ForgotPassword", "📧 Envoi de l'email de réinitialisation à: $email")
                            
                            if (USE_ANDROID_EMAIL_SENDER) {
                                // ⚠️ OPTION A : Envoi direct depuis Android (NON SÉCURISÉ)
                                // L'application génère le token et envoie l'email
                                // Le backend stocke le token pour pouvoir le valider lors du reset-password
                                android.util.Log.w("ForgotPassword", "⚠️ Utilisation de l'envoi direct depuis Android - NON RECOMMANDÉ EN PRODUCTION")
                                android.util.Log.d("ForgotPassword", "📧 Email destinataire: $email")
                                android.util.Log.d("ForgotPassword", "🎭 Type d'utilisateur: ${if (isAgency) "RECRUTEUR" else "ACTEUR"}")
                                
                                val userType = if (isAgency) "RECRUTEUR" else "ACTEUR"
                                
                                // Générer un token localement
                                val resetToken = EmailSender.generateResetToken()
                                android.util.Log.d("ForgotPassword", "🔑 Token généré localement par Android: ${resetToken.take(10)}...")
                                
                                // Stocker le token localement avec l'email pour vérification ultérieure
                                val tokenManager = TokenManager(context)
                                tokenManager.saveResetToken(email, resetToken)
                                
                                android.util.Log.d("ForgotPassword", "💾 Token stocké localement pour $email")
                                
                                // IMPORTANT : Essayer d'envoyer le token au backend pour qu'il le stocke (non bloquant)
                                // Si le backend ne supporte pas encore le champ token, on envoie quand même l'email
                                android.util.Log.d("ForgotPassword", "📤 Tentative d'envoi du token au backend pour stockage...")
                                try {
                                    val forgotPasswordResult = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        // Utiliser AuthRepository pour envoyer le token au backend
                                        sharedAuthRepository.forgotPassword(email, userType, resetToken)
                                    }
                                    
                                    forgotPasswordResult.onSuccess { forgotPasswordResponse ->
                                        android.util.Log.d("ForgotPassword", "✅ Token envoyé au backend avec succès")
                                        android.util.Log.d("ForgotPassword", "📝 Réponse backend: ${forgotPasswordResponse.message}")
                                    }
                                    
                                    forgotPasswordResult.onFailure { exception ->
                                        // Le backend n'a pas accepté le token (peut-être qu'il ne supporte pas encore ce champ)
                                        android.util.Log.w("ForgotPassword", "⚠️ Backend n'a pas accepté le token (${exception.message}), mais on continue quand même")
                                        // On continue quand même pour envoyer l'email
                                    }
                                } catch (e: Exception) {
                                    // Erreur réseau ou autre - on continue quand même pour envoyer l'email
                                    android.util.Log.w("ForgotPassword", "⚠️ Erreur lors de l'envoi du token au backend: ${e.message}, mais on continue quand même")
                                }
                                
                                // TOUJOURS envoyer l'email, même si le backend a échoué
                                android.util.Log.d("ForgotPassword", "📧 Envoi de l'email depuis Android...")
                                val emailResult = EmailSender.sendPasswordResetEmail(email, userType, resetToken)
                                
                                forgotPasswordLoading = false
                                
                                emailResult.onSuccess { message ->
                                    android.util.Log.d("ForgotPassword", "✅ Email envoyé avec succès depuis Android")
                                    forgotPasswordSuccess = if (isAgency) {
                                        "Un email de réinitialisation a été envoyé à $email. Vérifiez votre boîte de réception (et les spams)."
                                    } else {
                                        "A reset email has been sent to $email. Check your inbox (and spam folder)."
                                    }
                                }
                                
                                emailResult.onFailure { exception ->
                                    forgotPasswordException = exception
                                    forgotPasswordError = if (isAgency) {
                                        "Erreur lors de l'envoi de l'email: ${exception.message}"
                                    } else {
                                        "Error sending email: ${exception.message}"
                                    }
                                    showErrorDialog = true
                                    android.util.Log.e("ForgotPassword", "❌ Erreur: ${exception.message}")
                                }
                            } else {
                                // ✅ OPTION B : Appel API backend uniquement (RECOMMANDÉ)
                                android.util.Log.d("ForgotPassword", "✅ Utilisation de l'API backend uniquement - RECOMMANDÉ")
                                
                                val userType = if (isAgency) "RECRUTEUR" else "ACTEUR"
                                
                                // Utiliser AuthRepository pour appeler l'API backend
                                val forgotPasswordResult = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    sharedAuthRepository.forgotPassword(email, userType)
                                }
                                
                                forgotPasswordLoading = false
                                
                                forgotPasswordResult.onSuccess { forgotPasswordResponse ->
                                    android.util.Log.d("ForgotPassword", "✅ Email envoyé avec succès par le backend")
                                    forgotPasswordSuccess = if (isAgency) {
                                        "Un email de réinitialisation a été envoyé à $email. Vérifiez votre boîte de réception (et les spams)."
                                    } else {
                                        "A reset email has been sent to $email. Check your inbox (and spam folder)."
                                    }
                                }
                                
                                forgotPasswordResult.onFailure { exception ->
                                    forgotPasswordException = exception
                                    android.util.Log.e("ForgotPassword", "❌ Erreur: ${exception.message}", exception)
                                    
                                    when (exception) {
                                        is ApiException.NotFoundException -> {
                                            forgotPasswordError = if (isAgency) {
                                                "Aucun compte trouvé avec cet email."
                                            } else {
                                                "No account found with this email."
                                            }
                                        }
                                        is ApiException.BadRequestException -> {
                                            forgotPasswordError = if (isAgency) {
                                                "Trop de demandes. Veuillez réessayer plus tard."
                                            } else {
                                                "Too many requests. Please try again later."
                                            }
                                        }
                                        else -> {
                                            forgotPasswordError = if (isAgency) {
                                                "Erreur lors de la demande de réinitialisation: ${exception.message}"
                                            } else {
                                                "Error requesting password reset: ${exception.message}"
                                            }
                                        }
                                    }
                                    showErrorDialog = true
                                }
                            }
                        } catch (e: Exception) {
                            forgotPasswordLoading = false
                            forgotPasswordException = e
                            forgotPasswordError = if (isAgency) {
                                "Erreur de connexion. Vérifiez votre internet"
                            } else {
                                "Connection error. Check your internet"
                            }
                            showErrorDialog = true
                            android.util.Log.e("ForgotPassword", "❌ Exception: ${e.message}", e)
                        }
                    }
                }
            )
        }
        
        composable("actorHome") {
            ActorHomeScreen(
                onCastingClick = { casting ->
                    if (casting.id.isNotBlank()) {
                        android.util.Log.d("MainActivity", "🎬 Navigation vers castingDetail avec ID: '${casting.id}'")
                    navController.navigate("castingDetail/${casting.id}")
                    } else {
                        android.util.Log.e("MainActivity", "❌ Impossible de naviguer: ID de casting vide")
                        // Afficher un message d'erreur à l'utilisateur
                    }
                },
                onProfileClick = {
                    navController.navigate("settings/actor")
                },
                onAgendaClick = {
                    navController.navigate("agenda")
                },
                onFilterClick = {
                    navController.navigate("filter")
                },
                onHistoryClick = {
                    // Géré par l'alerte "coming soon" dans ActorHomeScreen
                },
                onMyCandidaturesClick = {
                    navController.navigate("myCandidatures")
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
            val context = LocalContext.current
            val tokenManager = remember { com.example.projecct_mobile.data.local.TokenManager(context) }
            var userRole by remember { mutableStateOf<String?>(null) }
            
            LaunchedEffect(Unit) {
                userRole = withContext(Dispatchers.IO) {
                    tokenManager.getUserRoleSync()
                }
            }
            
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
                    // Navigation vers le profil selon le rôle
                    when (userRole?.uppercase()) {
                        "ACTEUR" -> navController.navigate("settings/actor")
                        "RECRUTEUR", "ADMIN" -> navController.navigate("agencyProfile")
                        else -> navController.navigate("settings/actor") // Par défaut pour les acteurs
                    }
                },
                onInterviewClick = { interview ->
                    // Seuls les acteurs peuvent sélectionner une date parmi celles proposées par l'agence
                    // Si l'interview est en attente et que l'utilisateur est un acteur, ouvrir l'écran de sélection
                    if (interview.statusEnum == com.example.projecct_mobile.data.model.InterviewStatus.PENDING 
                        && userRole?.uppercase() == "ACTEUR") {
                        navController.navigate("selectInterviewDate/${interview.id}")
                    }
                    // Les agences ne peuvent pas sélectionner de date - elles proposent les dates
                },
                onHomeClick = {
                    // Navigation vers le home selon le rôle
                    when (userRole?.uppercase()) {
                        "ACTEUR" -> {
                            navController.navigate("actorHome") {
                                popUpTo("actorHome") { inclusive = false }
                            }
                        }
                        "RECRUTEUR", "ADMIN" -> {
                            navController.navigate("agencyCastingList") {
                                popUpTo("agencyCastingList") { inclusive = false }
                            }
                        }
                        else -> {
                            navController.navigate("actorHome") {
                                popUpTo("actorHome") { inclusive = false }
                            }
                        }
                    }
                },
                onCandidaturesClick = {
                    navController.navigate("myCandidatures")
                },
                onAgendaClick = {
                    // Déjà sur l'agenda
                },
                onCreateCastingClick = {
                    navController.navigate("agencyCreateCasting")
                }
            )
        }
        
        composable(
            route = "selectInterviewDate/{interviewId}",
            arguments = listOf(navArgument("interviewId") { type = NavType.StringType })
        ) { backStackEntry ->
            val interviewId = backStackEntry.arguments?.getString("interviewId") ?: ""
            val context = LocalContext.current
            val tokenManager = remember { com.example.projecct_mobile.data.local.TokenManager(context) }
            val scope = rememberCoroutineScope()
            val interviewRepository = remember { com.example.projecct_mobile.data.repository.InterviewRepository() }
            var interview by remember { mutableStateOf<com.example.projecct_mobile.data.model.InterviewResponse?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var userRole by remember { mutableStateOf<String?>(null) }
            
            // Vérifier le rôle de l'utilisateur - seuls les acteurs peuvent sélectionner une date
            LaunchedEffect(Unit) {
                userRole = withContext(Dispatchers.IO) {
                    tokenManager.getUserRoleSync()
                }
                
                // Si l'utilisateur n'est pas un acteur, rediriger
                if (userRole?.uppercase() != "ACTEUR") {
                    android.util.Log.w("MainActivity", "⚠️ Tentative d'accès à selectInterviewDate par un non-acteur: $userRole")
                    navController.popBackStack()
                    return@LaunchedEffect
                }
                
                isLoading = true
                val result = interviewRepository.getInterviewById(interviewId)
                result.onSuccess {
                    interview = it
                    isLoading = false
                }.onFailure { exception ->
                    errorMessage = exception.message
                    isLoading = false
                }
            }
            
            when {
                userRole?.uppercase() != "ACTEUR" -> {
                    // Redirection si l'utilisateur n'est pas un acteur
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Accès non autorisé",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.projecct_mobile.ui.theme.Red
                            )
                            Text(
                                text = "Seuls les acteurs peuvent sélectionner une date d'interview.",
                                fontSize = 14.sp,
                                color = com.example.projecct_mobile.ui.theme.GrayBorder,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Retour")
                            }
                        }
                    }
                }
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = com.example.projecct_mobile.ui.theme.DarkBlue)
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Erreur: $errorMessage",
                            color = com.example.projecct_mobile.ui.theme.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Retour")
                        }
                    }
                }
                interview != null -> {
                    com.example.projecct_mobile.ui.screens.interview.SelectInterviewDateScreen(
                        interview = interview!!,
                        onDateSelected = {
                            // Revenir à l'agenda après sélection
                            navController.popBackStack()
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
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
            var casting by remember { mutableStateOf<com.example.projecct_mobile.data.model.Casting?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val castingRepository = remember { CastingRepository() }
            val scope = rememberCoroutineScope()
            
            // Charger le casting depuis l'API
            LaunchedEffect(castingId) {
                android.util.Log.d("MainActivity", "🔍 Chargement du casting avec ID: '$castingId'")
                if (castingId.isNotEmpty()) {
                    isLoading = true
                    errorMessage = null
                    try {
                    scope.launch {
                        val result = castingRepository.getCastingById(castingId)
                        result.onSuccess { apiCasting ->
                                casting = apiCasting
                            isLoading = false
                                errorMessage = null
                        }
                            result.onFailure { exception ->
                                android.util.Log.e("MainActivity", "Erreur chargement casting: ${exception.message}", exception)
                            isLoading = false
                                errorMessage = getErrorMessage(exception)
                        }
                    }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Exception lors du chargement: ${e.message}", e)
                        isLoading = false
                        errorMessage = getErrorMessage(e)
                    }
                } else {
                    isLoading = false
                    errorMessage = "ID de casting invalide"
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
            } else if (errorMessage != null) {
                // Afficher un message d'erreur
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Erreur",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.projecct_mobile.ui.theme.Red
                        )
                        Text(
                            text = errorMessage ?: "Impossible de charger le casting",
                            fontSize = 16.sp,
                            color = com.example.projecct_mobile.ui.theme.GrayBorder,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Retour")
                            }
                            Button(
                                onClick = {
                                    // Réessayer le chargement
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = castingRepository.getCastingById(castingId)
                                        result.onSuccess { apiCasting ->
                                            casting = apiCasting
                                            isLoading = false
                                            errorMessage = null
                                        }
                                        result.onFailure { exception ->
                                            isLoading = false
                                            errorMessage = getErrorMessage(exception)
                                        }
                                    }
                                }
                            ) {
                                Text("Réessayer")
                        }
                    }
                }
            }
            } else {
            // Afficher l'écran de détails seulement si le casting est chargé
            val currentCasting = casting
            if (currentCasting != null) {
                // Détecter le rôle de l'utilisateur pour afficher le bon écran
                var userRole by remember { mutableStateOf<String?>(null) }
                var isLoadingRole by remember { mutableStateOf(true) }
                val tokenManager = remember { TokenManager(context) }

                LaunchedEffect(Unit) {
                    userRole = withContext(Dispatchers.IO) {
                        tokenManager.getUserRoleSync()
                    }
                    android.util.Log.d("MainActivity", "🔍 Rôle utilisateur détecté: '$userRole'")
                    isLoadingRole = false
                }

                // Afficher un écran de chargement pendant la détection du rôle
                if (isLoadingRole) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = com.example.projecct_mobile.ui.theme.DarkBlue)
                    }
                } else {
                    // Afficher l'écran approprié selon le rôle
                    val roleUpper = userRole?.uppercase()?.trim()
                    android.util.Log.d("MainActivity", "🎭 Affichage écran pour rôle: '$roleUpper' (original: '$userRole')")
                    
                    // Vérifier si c'est un rôle d'agence (plus flexible)
                    val isAgencyRole = roleUpper?.let { role ->
                        role == "RECRUTEUR" || 
                        role == "AGENCY" || 
                        role == "AGENCE" ||
                        role.contains("RECRUTEUR", ignoreCase = true) ||
                        role.contains("AGENCY", ignoreCase = true) ||
                        role.contains("AGENCE", ignoreCase = true)
                    } ?: false
                    
                    if (isAgencyRole) {
                        android.util.Log.d("MainActivity", "✅ Affichage AgencyCastingDetailScreen (rôle agence détecté)")
                        // Écran pour les agences (sans bouton Submit, avec options d'édition/suppression)
                        AgencyCastingDetailScreen(
                            casting = currentCasting,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onEditClick = {
                                // Naviguer vers l'écran d'édition du casting
                                android.util.Log.d("MainActivity", "✏️ Édition du casting: ${currentCasting.titre}")
                                navController.navigate("agencyEditCasting/${currentCasting.actualId ?: castingId}")
                            },
                            onDeleteClick = {
                                // Supprimer le casting
                                android.util.Log.d("MainActivity", "🗑️ Suppression du casting: ${currentCasting.titre}")
                                scope.launch {
                                    try {
                                        val result = castingRepository.deleteCasting(currentCasting.actualId ?: castingId)
                                        result.onSuccess {
                                            android.util.Log.d("MainActivity", "✅ Casting supprimé avec succès")
                                            navController.popBackStack()
                                        }
                                        result.onFailure { exception ->
                                            android.util.Log.e("MainActivity", "❌ Erreur suppression: ${exception.message}", exception)
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("MainActivity", "❌ Exception suppression: ${e.message}", e)
                                    }
                                }
                            },
                            onViewActorProfile = { acteurId ->
                                // Naviguer vers le profil de l'acteur
                                android.util.Log.d("MainActivity", "👤 Voir le profil de l'acteur: $acteurId")
                                navController.navigate("actorProfile/$acteurId")
                            },
                            onNavigateToLogin = {
                                // Rediriger vers l'écran de connexion agence si la session expire
                                android.util.Log.d("MainActivity", "🔐 Session expirée - redirection vers connexion agence")
                                navController.navigate("agencySignIn") {
                                    popUpTo("agencySignIn") { inclusive = true }
                                }
                            },
                            onChatbotClick = {
                                // Naviguer vers l'écran chatbot séparé
                                android.util.Log.d("MainActivity", "🤖 Navigation vers le chatbot pour casting: ${currentCasting.titre}")
                                navController.navigate("agencyChatbot/${currentCasting.actualId ?: castingId}")
                            }
                        )
                    } else {
                        android.util.Log.d("MainActivity", "✅ Affichage CastingDetailScreen (acteur par défaut, rôle: '$roleUpper')")
                        // Écran pour les acteurs (avec bouton Submit)
                CastingDetailScreen(
                    casting = currentCasting,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMapClick = {
                        navController.navigate("map")
                    },
                    onSubmitClick = {
                                    // L'appel API est géré directement dans CastingDetailScreen
                                    android.util.Log.d("MainActivity", "Callback onSubmitClick appelé pour le casting: ${currentCasting.titre}")
                    },
                    onNavigateToApplication = { casting ->
                        // Naviguer vers l'écran de candidature avec vidéo
                        android.util.Log.d("MainActivity", "🎬 Navigation vers candidature avec vidéo pour: ${casting.titre}")
                        navController.navigate("castingApplication/${casting.actualId ?: ""}")
                    },
                    onNavigateToProfile = {
                                    // Navigue vers la page settings de l'acteur
                                    navController.navigate("settings/actor")
                                },
                                onNavigateToHome = {
                                    // Retourne à la page d'accueil de l'acteur
                                    navController.navigate("actorHome") {
                                        popUpTo("actorHome") { inclusive = false }
                                    }
                                },
                                onNavigateToCandidatures = {
                                    // Navigue vers la page "Mes candidatures"
                                    navController.navigate("myCandidatures")
                                }
                            )
                    }
                }
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
                            text = "Casting non trouvé",
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
        
        // Route pour le chatbot de l'agence
        composable(
            route = "agencyChatbot/{castingId}",
            arguments = listOf(
                navArgument("castingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val castingId = backStackEntry.arguments?.getString("castingId") ?: ""
            val castingRepository = remember { CastingRepository() }
            var casting by remember { mutableStateOf<com.example.projecct_mobile.data.model.Casting?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            // Charger le casting
            LaunchedEffect(castingId) {
                if (castingId.isNotBlank()) {
                    isLoading = true
                    try {
                        val result = castingRepository.getCastingById(castingId)
                        result.onSuccess { loadedCasting ->
                            casting = loadedCasting
                            isLoading = false
                        }
                        result.onFailure {
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        isLoading = false
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else if (casting != null) {
                AgencyChatbotScreen(
                    castingId = castingId,
                    castingTitle = casting!!.titre ?: "Casting",
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewActorProfile = { acteurId ->
                        navController.navigate("actorProfile/$acteurId")
                    }
                )
            }
        }
        
        // Route pour l'écran de candidature avec vidéo
        composable(
            route = "castingApplication/{castingId}",
            arguments = listOf(
                navArgument("castingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val castingId = backStackEntry.arguments?.getString("castingId") ?: ""
            var casting by remember { mutableStateOf<com.example.projecct_mobile.data.model.Casting?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val castingRepository = remember { com.example.projecct_mobile.data.repository.CastingRepository() }
            
            LaunchedEffect(castingId) {
                if (castingId.isNotBlank()) {
                    val result = castingRepository.getCastingById(castingId)
                    result.onSuccess { 
                        casting = it
                        isLoading = false
                    }.onFailure { exception ->
                        errorMessage = exception.message
                        isLoading = false
                    }
                } else {
                    errorMessage = "ID de casting invalide"
                    isLoading = false
                }
            }
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = com.example.projecct_mobile.ui.theme.DarkBlue)
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Erreur",
                            color = com.example.projecct_mobile.ui.theme.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Retour")
                        }
                    }
                }
                casting != null -> {
                    CastingApplicationScreen(
                        casting = casting!!,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSuccess = {
                            // Retourner à la liste des castings ou au détail
                            navController.popBackStack()
                        }
                    )
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
        
        composable("myCandidatures") {
            MyCandidaturesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCastingClick = { castingId ->
                    if (castingId.isNotBlank()) {
                        android.util.Log.d("MainActivity", "🎬 Navigation vers castingDetail depuis mes candidatures avec ID: '$castingId'")
                        navController.navigate("castingDetail/$castingId")
                    }
                },
                onHomeClick = {
                    navController.navigate("actorHome") {
                        popUpTo("actorHome") { inclusive = true }
                    }
                },
                onAgendaClick = {
                    navController.navigate("agenda")
                },
                onProfileClick = {
                    navController.navigate("settings/actor")
                }
            )
        }
        
        composable("favorites") {
            FavoritesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCastingClick = { casting ->
                    if (casting.id.isNotBlank()) {
                        android.util.Log.d("MainActivity", "🎬 Navigation vers castingDetail depuis favoris avec ID: '${casting.id}'")
                        navController.navigate("castingDetail/${casting.id}")
                    }
                },
                onProfileClick = {
                    navController.navigate("settings/actor")
                },
                onHomeClick = {
                    navController.navigate("actorHome") {
                        popUpTo("actorHome") { inclusive = true }
                    }
                },
                onAgendaClick = {
                    navController.navigate("agenda")
                },
                onMyCandidaturesClick = {
                    navController.navigate("myCandidatures")
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
        
        composable("actorTraining") {
            ActorTrainingScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Route pour afficher le profil d'un acteur spécifique (utilisée par les agences)
        composable(
            route = "actorProfile/{acteurId}",
            arguments = listOf(
                navArgument("acteurId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val acteurId = backStackEntry.arguments?.getString("acteurId")
            android.util.Log.d("MainActivity", "👤 Affichage du profil acteur ID: $acteurId")
            
            ActorProfileDetails(
                acteurId = acteurId,
                loadData = acteurId != null, // S'assurer que les données sont chargées pour les agences
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
                    // Ne devrait pas être accessible en mode lecture seule pour les agences
                    scope.launch {
                        sharedAuthRepository.logout()
                    }
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = false }
                    }
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