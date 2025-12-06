package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.TrainingRepository
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.White
import com.example.projecct_mobile.utils.BiometricAuthHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashPackSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val acteurRepository = remember { ActeurRepository() }
    val trainingRepository = remember { TrainingRepository() }
    
    var hashpackAddress by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Vérifier le support biométrique
    val biometricSupport = remember {
        val support = BiometricAuthHelper.checkBiometricSupport(context)
        android.util.Log.d("HashPackSettings", "🔍 Support biométrique: $support (${when(support) {
            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> "SUCCESS"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "NO_HARDWARE"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "NONE_ENROLLED"
            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "HW_UNAVAILABLE"
            else -> "UNKNOWN"
        }})")
        support
    }
    
    val isBiometricAvailable = remember {
        biometricSupport == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    }
    
    // Demander l'authentification biométrique IMMÉDIATEMENT au chargement de l'écran
    LaunchedEffect(Unit) {
        android.util.Log.d("HashPackSettings", "🚀 LaunchedEffect déclenché, isAuthenticated: $isAuthenticated")
        
        if (!isAuthenticated) {
            // Vérifier que l'activity est bien un FragmentActivity
            val fragmentActivity = context as? FragmentActivity
            android.util.Log.d("HashPackSettings", "🔍 FragmentActivity: ${fragmentActivity != null}, isBiometricAvailable: $isBiometricAvailable, support: $biometricSupport")
            
            if (fragmentActivity != null && isBiometricAvailable) {
                // Délai pour s'assurer que l'activity est complètement initialisée et que l'UI est prête
                kotlinx.coroutines.delay(500)
                
                // Authentification requise avant d'afficher quoi que ce soit
                android.util.Log.d("HashPackSettings", "🔐 Démarrage de l'authentification biométrique")
                try {
                    BiometricAuthHelper.authenticate(
                        activity = fragmentActivity,
                        title = "Authentification requise",
                        subtitle = "Veuillez utiliser votre empreinte digitale pour accéder à votre wallet HashPack",
                        negativeButtonText = "Annuler",
                        onSuccess = {
                            android.util.Log.d("HashPackSettings", "✅ Authentification réussie")
                            isAuthenticated = true
                        },
                        onError = { error ->
                            android.util.Log.e("HashPackSettings", "❌ Erreur d'authentification: $error")
                            errorMessage = "Erreur d'authentification: $error"
                            // Retourner à l'écran précédent en cas d'erreur
                            scope.launch {
                                kotlinx.coroutines.delay(500)
                                onBackClick()
                            }
                        },
                        onCancel = {
                            android.util.Log.d("HashPackSettings", "🚫 Authentification annulée")
                            // Retourner à l'écran précédent si l'utilisateur annule
                            scope.launch {
                                kotlinx.coroutines.delay(500)
                                onBackClick()
                            }
                        }
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HashPackSettings", "❌ Exception lors de l'authentification: ${e.message}", e)
                    errorMessage = "Erreur: ${e.message}"
                    // Ne pas autoriser l'accès en cas d'erreur, retourner en arrière
                    scope.launch {
                        kotlinx.coroutines.delay(500)
                        onBackClick()
                    }
                }
            } else {
                when (biometricSupport) {
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        android.util.Log.w("HashPackSettings", "⚠️ Aucun matériel biométrique disponible")
                    }
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        android.util.Log.w("HashPackSettings", "⚠️ Aucune empreinte digitale enregistrée")
                    }
                    else -> {
                        android.util.Log.e("HashPackSettings", "❌ FragmentActivity non disponible (context: ${context.javaClass.simpleName})")
                    }
                }
                // Si pas de biométrie disponible, autoriser l'accès (moins sécurisé mais fonctionnel)
                isAuthenticated = true
            }
        }
    }
    
    // Charger l'adresse HashPack après authentification
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            isLoading = true
            val result = acteurRepository.getCurrentActeur()
            result.onSuccess { profile ->
                hashpackAddress = profile.hashpackAccountId
                isLoading = false
            }.onFailure {
                errorMessage = "Impossible de charger l'adresse HashPack"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Wallet HashPack",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5FB))
                .padding(paddingValues)
        ) {
            // Ne rien afficher tant que l'authentification n'est pas réussie
            if (!isAuthenticated) {
                // Afficher un écran de chargement pendant l'authentification
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isBiometricAvailable) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Authentification biométrique non disponible",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Veuillez configurer une empreinte digitale ou une reconnaissance faciale dans les paramètres de votre appareil.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    } else {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                }
            } else if (isLoading) {
                // Chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else {
                // Contenu principal
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Message d'information
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF1976D2)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Votre wallet HashPack",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Cette adresse est utilisée pour recevoir automatiquement vos NFTs lors des entraînements.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }
                    
                    // Card avec l'adresse HashPack
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = DarkBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            "Adresse HashPack",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            hashpackAddress ?: "Non renseigné",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (hashpackAddress != null) Color.Black else Color.Gray
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        // Demander authentification avant modification
                                        android.util.Log.d("HashPackSettings", "✏️ Clic sur modifier, isBiometricAvailable: $isBiometricAvailable")
                                        val fragmentActivity = context as? FragmentActivity
                                        android.util.Log.d("HashPackSettings", "✏️ FragmentActivity: ${fragmentActivity != null}")
                                        
                                        if (isBiometricAvailable && fragmentActivity != null) {
                                            android.util.Log.d("HashPackSettings", "🔐 Démarrage authentification pour modification")
                                            try {
                                                BiometricAuthHelper.authenticate(
                                                    activity = fragmentActivity,
                                                    title = "Authentification requise",
                                                    subtitle = "Veuillez vous authentifier pour modifier votre wallet HashPack",
                                                    onSuccess = {
                                                        android.util.Log.d("HashPackSettings", "✅ Authentification réussie pour modification")
                                                        showEditDialog = true
                                                    },
                                                    onError = { error ->
                                                        android.util.Log.e("HashPackSettings", "❌ Erreur authentification modification: $error")
                                                        errorMessage = "Erreur d'authentification: $error"
                                                    },
                                                    onCancel = {
                                                        android.util.Log.d("HashPackSettings", "🚫 Authentification annulée pour modification")
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.e("HashPackSettings", "❌ Exception authentification modification: ${e.message}", e)
                                                errorMessage = "Erreur: ${e.message}"
                                            }
                                        } else {
                                            android.util.Log.w("HashPackSettings", "⚠️ Pas de biométrie, ouverture directe du dialogue")
                                            showEditDialog = true
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = DarkBlue
                                    )
                                }
                            }
                        }
                    }
                    
                    // Messages d'erreur/succès
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F)
                                )
                                Text(
                                    error,
                                    fontSize = 14.sp,
                                    color = Color(0xFFD32F2F),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    successMessage?.let { success ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF388E3C)
                                )
                                Text(
                                    success,
                                    fontSize = 14.sp,
                                    color = Color(0xFF388E3C),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogue pour modifier l'adresse HashPack
    if (showEditDialog) {
        HashpackEditDialog(
            initialAddress = hashpackAddress,
            onDismiss = { showEditDialog = false },
            onConfirm = { newAddress ->
                scope.launch {
                    val acteurId = acteurRepository.getCurrentActeurId()
                    if (acteurId != null) {
                        isUpdating = true
                        errorMessage = null
                        successMessage = null
                        
                        val result = trainingRepository.updateHashpackAddress(acteurId, newAddress.trim())
                        result.onSuccess { updatedProfile ->
                            hashpackAddress = updatedProfile.hashpackAccountId
                            successMessage = "Adresse HashPack mise à jour avec succès !"
                            showEditDialog = false
                            isUpdating = false
                            
                            // Effacer le message de succès après 3 secondes
                            scope.launch {
                                kotlinx.coroutines.delay(3000)
                                successMessage = null
                            }
                        }.onFailure { error ->
                            errorMessage = "Erreur: ${error.message}"
                            isUpdating = false
                        }
                    } else {
                        errorMessage = "Impossible de récupérer votre ID. Veuillez vous reconnecter."
                        isUpdating = false
                    }
                }
            },
            isUpdating = isUpdating
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HashpackEditDialog(
    initialAddress: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isUpdating: Boolean = false
) {
    var address by remember { mutableStateOf(initialAddress ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Modifier l'adresse HashPack",
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        },
        text = {
            Column {
                Text(
                    "Entrez votre adresse HashPack (format: 0.0.xxxxx)",
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse HashPack") },
                    placeholder = { Text("0.0.xxxxx") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUpdating
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(address) },
                enabled = address.isNotBlank() && !isUpdating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enregistrement...")
                } else {
                    Text("Enregistrer")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Annuler")
            }
        }
    )
}

