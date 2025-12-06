package com.example.projecct_mobile.ui.screens.acteur

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ai.GeminiTrainingService
import com.example.projecct_mobile.data.model.*
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.data.repository.TrainingRepository
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.launch

/**
 * Écran d'entraînement avec niveaux et stages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorTrainingLevelsScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // États pour la navigation entre niveaux/stages
    var selectedNiveau by remember { mutableStateOf<Int?>(null) }
    var selectedStage by remember { mutableStateOf<Int?>(null) }
    
    // États pour la vidéo et l'analyse
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<TrainingFeedback?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var nftRewarded by remember { mutableStateOf(false) }
    var nftReward by remember { mutableStateOf<NFTReward?>(null) }
    var isTransferringNFT by remember { mutableStateOf(false) }
    var showHashpackDialog by remember { mutableStateOf(false) }
    var showHashpackInfoDialog by remember { mutableStateOf(false) }
    var hashpackTransferInfo by remember { mutableStateOf<NFTTransferData?>(null) }
    var pendingNftId by remember { mutableStateOf<String?>(null) }
    var hasHashpackAccount by remember { mutableStateOf<Boolean?>(null) }
    
    // Services
    val geminiService = remember { GeminiTrainingService(context) }
    val trainingRepository = remember { TrainingRepository() }
    val acteurRepository = remember { ActeurRepository() }
    
    // Fonction pour transférer le NFT
    fun transferNFT(nftId: String) {
        scope.launch {
            isTransferringNFT = true
            errorMessage = null
            try {
                val result = trainingRepository.transferNFT(nftId)
                result.onSuccess { transferResponse ->
                    isTransferringNFT = false
                    hashpackTransferInfo = transferResponse.transactionData
                    // Ouvrir HashPack avec les données de transaction
                    openHashPackForTransfer(context, transferResponse.transactionData)
                    // Afficher le dialogue informatif après un court délai
                    scope.launch {
                        kotlinx.coroutines.delay(500) // Attendre que HashPack s'ouvre
                        showHashpackInfoDialog = true
                    }
                }.onFailure { error ->
                    isTransferringNFT = false
                    // Vérifier si l'erreur indique que l'adresse HashPack n'est pas enregistrée
                    if (error.message?.contains("Vous devez d'abord enregistrer votre adresse HashPack", ignoreCase = true) == true) {
                        showHashpackDialog = true
                    } else {
                        errorMessage = "Erreur lors du transfert: ${error.message}"
                    }
                }
            } catch (e: Exception) {
                isTransferringNFT = false
                errorMessage = "Erreur: ${e.message}"
            }
        }
    }
    
    // Launcher pour sélectionner une vidéo
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        errorMessage = null
        feedback = null
    }
    
    // Fonction pour obtenir le prompt selon le niveau et stage
    fun getStagePrompt(niveau: Int, stage: Int): String {
        return when (niveau) {
            1 -> when (stage) {
                1 -> "SCÈNE À JOUER:\n\n" +
                    "Vous êtes dans un café, vous venez de recevoir une nouvelle qui vous rend très heureux. " +
                    "Vous devez exprimer cette joie de manière authentique et naturelle. " +
                    "Montrez votre réaction immédiate à cette bonne nouvelle."
                2 -> "SCRIPT À JOUER:\n\n" +
                    "PERSONNAGE: Vous êtes un étudiant qui vient de réussir un examen important.\n\n" +
                    "DIALOGUE:\n" +
                    "\"Je ne peux pas y croire ! J'ai réussi ! Après toutes ces heures d'étude, " +
                    "tous ces doutes... Et maintenant, c'est fait. Je suis tellement heureux !\"\n\n" +
                    "Jouez cette scène avec émotion et authenticité."
                3 -> "IMPROVISATION:\n\n" +
                    "SITUATION: Vous êtes dans un ascenseur qui vient de tomber en panne. " +
                    "Vous êtes seul et vous réalisez que vous êtes coincé. " +
                    "Vous commencez à paniquer mais vous essayez de vous calmer.\n\n" +
                    "IMPROVISEZ cette scène sans dialogue, uniquement avec vos expressions et votre langage corporel."
                else -> ""
            }
            else -> ""
        }
    }
    
    // Fonction pour soumettre au backend (définie avant analyzeVideo pour être accessible)
    fun submitTrainingToBackend(feedback: TrainingFeedback, niveau: Int) {
        scope.launch {
            isSubmitting = true
            errorMessage = null
            
            try {
                // Récupérer l'ID de l'acteur
                val acteurId = acteurRepository.getCurrentActeurId()
                if (acteurId == null) {
                    errorMessage = "Impossible de récupérer votre ID. Veuillez vous reconnecter."
                    isSubmitting = false
                    return@launch
                }
                
                // Créer la requête
                val request = SubmitTrainingRequest(
                    acteurId = acteurId,
                    niveau = niveau,
                    globalScore = feedback.globalScore,
                    emotions = TrainingEmotions(
                        detected = feedback.emotions.detected,
                        coherence = feedback.emotions.coherence,
                        intensity = feedback.emotions.intensity
                    ),
                    strengths = feedback.strengths,
                    recommendations = feedback.recommendations
                )
                
                // Vérifier si l'utilisateur a un compte HashPack associé (avant soumission)
                var userHasHashpack = false
                try {
                    val profileResult = acteurRepository.getCurrentActeur()
                    profileResult.onSuccess { profile ->
                        userHasHashpack = !profile.hashpackAccountId.isNullOrBlank()
                        hasHashpackAccount = userHasHashpack
                    }
                } catch (e: Exception) {
                    android.util.Log.w("Training", "Impossible de vérifier HashPack: ${e.message}")
                }
                
                // Soumettre
                val result = trainingRepository.submitTraining(request)
                
                result.onSuccess { response ->
                    isSubmitting = false
                    if (response.nftReward != null) {
                        nftRewarded = true
                        nftReward = response.nftReward
                        
                        // Le backend transfère automatiquement si hashpackAccountId existe
                        if (userHasHashpack) {
                            // Transfert automatique effectué par le backend
                            successMessage = "🎉 Félicitations ! Vous avez obtenu un NFT pour avoir atteint un score de ${feedback.globalScore}/100 !\n\n" +
                                "✅ Votre NFT a été automatiquement transféré vers votre wallet HashPack.\n" +
                                "Token ID: ${response.nftReward.tokenId}\n" +
                                "Numéro de série: ${response.nftReward.serialNumber}"
                        } else {
                            // Pas de HashPack associé
                            successMessage = "🎉 Félicitations ! Vous avez obtenu un NFT pour avoir atteint un score de ${feedback.globalScore}/100 !\n\n" +
                                "Token ID: ${response.nftReward.tokenId}\n" +
                                "Numéro de série: ${response.nftReward.serialNumber}\n\n" +
                                "💡 Associez votre wallet HashPack dans votre profil pour recevoir automatiquement vos NFTs à l'avenir."
                            // Proposer d'associer HashPack
                            showHashpackDialog = true
                        }
                    } else {
                        nftReward = null
                        successMessage = "Session enregistrée avec succès ! Score: ${feedback.globalScore}/100. Continuez pour obtenir un NFT (score >= 70) !"
                    }
                }.onFailure { error ->
                    isSubmitting = false
                    errorMessage = "Erreur lors de l'enregistrement: ${error.message}"
                    nftReward = null
                }
            } catch (e: Exception) {
                isSubmitting = false
                errorMessage = "Erreur: ${e.message}"
            }
        }
    }
    
    // Fonction d'analyse de vidéo
    fun analyzeVideo(uri: Uri, niveau: Int, stage: Int) {
        scope.launch {
            isAnalyzing = true
            errorMessage = null
            feedback = null
            successMessage = null
            nftRewarded = false
            nftReward = null
            
            val stagePrompt = getStagePrompt(niveau, stage)
            
            val result = geminiService.analyzeActingVideo(uri)
            
            result.onSuccess { feedbackResult ->
                feedback = feedbackResult
                isAnalyzing = false
                
                // Soumettre au backend
                submitTrainingToBackend(feedbackResult, niveau)
            }.onFailure { error ->
                errorMessage = error.message ?: "Erreur lors de l'analyse"
                isAnalyzing = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🎭 Entraînement par Niveaux",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF3F5FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Si aucun niveau n'est sélectionné, afficher la sélection de niveau
            if (selectedNiveau == null) {
                LevelSelectionView(
                    onNiveauSelected = { niveau ->
                        selectedNiveau = niveau
                        selectedStage = null
                        selectedVideoUri = null
                        feedback = null
                        errorMessage = null
                        successMessage = null
                        nftReward = null
                    }
                )
            }
            // Si un niveau est sélectionné mais pas de stage
            else if (selectedStage == null) {
                StageSelectionView(
                    niveau = selectedNiveau!!,
                    onStageSelected = { stage ->
                        selectedStage = stage
                        selectedVideoUri = null
                        feedback = null
                        errorMessage = null
                        successMessage = null
                        nftReward = null
                    },
                    onBackClick = {
                        selectedNiveau = null
                    }
                )
            }
            // Si un stage est sélectionné, afficher l'interface d'entraînement
            else {
                TrainingStageView(
                    niveau = selectedNiveau!!,
                    stage = selectedStage!!,
                    stagePrompt = getStagePrompt(selectedNiveau!!, selectedStage!!),
                    selectedVideoUri = selectedVideoUri,
                    isAnalyzing = isAnalyzing,
                    isSubmitting = isSubmitting,
                    feedback = feedback,
                    errorMessage = errorMessage,
                    successMessage = successMessage,
                    nftRewarded = nftRewarded,
                    nftReward = nftReward,
                    hasHashpackAccount = hasHashpackAccount ?: false,
                    isTransferringNFT = isTransferringNFT,
                    trainingRepository = trainingRepository,
                    onVideoSelected = { videoPickerLauncher.launch("video/*") },
                    onTransferNFT = { nftId ->
                        pendingNftId = nftId
                        transferNFT(nftId)
                    },
                    onAnalyzeClick = { uri ->
                        analyzeVideo(uri, selectedNiveau!!, selectedStage!!)
                    },
                    onResetClick = {
                        selectedVideoUri = null
                        feedback = null
                        errorMessage = null
                        successMessage = null
                        nftRewarded = false
                        nftReward = null
                    },
                    onBackClick = {
                        selectedStage = null
                        selectedVideoUri = null
                        feedback = null
                        errorMessage = null
                        successMessage = null
                        nftReward = null
                    }
                )
            }
        }
        
        // Dialogue pour enregistrer l'adresse HashPack
        if (showHashpackDialog) {
            HashpackAddressDialog(
                onDismiss = { showHashpackDialog = false },
                onConfirm = { address ->
                    scope.launch {
                        val acteurId = acteurRepository.getCurrentActeurId()
                        if (acteurId != null) {
                            isTransferringNFT = true
                            val result = trainingRepository.updateHashpackAddress(acteurId, address)
                            result.onSuccess {
                                showHashpackDialog = false
                                isTransferringNFT = false
                                // Relancer le transfert
                                pendingNftId?.let { nftId ->
                                    transferNFT(nftId)
                                }
                            }.onFailure { error ->
                                isTransferringNFT = false
                                errorMessage = "Erreur lors de l'enregistrement: ${error.message}"
                            }
                        } else {
                            errorMessage = "Impossible de récupérer votre ID. Veuillez vous reconnecter."
                            showHashpackDialog = false
                        }
                    }
                }
            )
        }
        
        // Dialogue informatif pour le transfert NFT (testnet)
        if (showHashpackInfoDialog && hashpackTransferInfo != null) {
            HashpackTransferInfoDialog(
                transferData = hashpackTransferInfo!!,
                onDismiss = { showHashpackInfoDialog = false }
            )
        }
    }
}

/**
 * Dialogue informatif pour le transfert NFT sur testnet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashpackTransferInfoDialog(
    transferData: NFTTransferData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "📱 Récupération de votre NFT",
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "📱 Ouvrez HashPack manuellement :",
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF155724)
                )
                
                Text(
                    "1. Ouvrez l'application HashPack sur votre téléphone\n" +
                    "2. Assurez-vous d'être connecté au réseau TESTNET\n" +
                    "   (Paramètres → Réseau → Testnet)\n\n" +
                    "⚠️ Si le NFT n'apparaît pas automatiquement :\n\n" +
                    "3. Allez dans HashPack → Onglet 'NFTs'\n" +
                    "4. Cliquez sur 'Associate NFT' (en bas ou dans le menu)\n" +
                    "5. Entrez le Token ID ci-dessous\n" +
                    "6. Confirmez l'association\n\n" +
                    "💡 L'association coûte ~0.05 USD en HBAR (frais réseau)",
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "Informations du NFT :",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontSize = 13.sp
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF3F5FB)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Token ID:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            transferData.tokenId,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            "Numéro de série:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            transferData.serialNumber,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                )
            ) {
                Text("J'ai compris")
            }
        }
    )
}

/**
 * Dialogue pour enregistrer l'adresse HashPack
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashpackAddressDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var address by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Enregistrer votre adresse HashPack",
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        },
        text = {
            Column {
                Text(
                    "Pour recevoir vos NFTs, vous devez d'abord enregistrer votre adresse HashPack.\n\n" +
                    "✅ Une fois associé, vos futurs NFTs seront automatiquement transférés vers votre wallet HashPack.\n\n" +
                    "💡 Ce NFT pourra être transféré manuellement après l'association.",
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = Color.Gray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse HashPack") },
                    placeholder = { Text("0.0.xxxxx") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (address.isNotBlank()) {
                        onConfirm(address.trim())
                    }
                },
                enabled = address.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                )
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Ouvre HashPack avec les données de transaction NFT
 */
fun openHashPackForTransfer(context: android.content.Context, transferData: NFTTransferData) {
    try {
        android.util.Log.d("HashPack", "🔄 Tentative d'ouverture de HashPack avec: tokenId=${transferData.tokenId}, serialNumber=${transferData.serialNumber}, recipient=${transferData.recipientAccountId}")
        
        // Packages possibles pour HashPack Android
        val hashpackPackages = listOf(
            "com.hashpack.app",
            "app.hashpack",
            "com.hashpack",
            "app.hashpack.wallet"
        )
        
        // Vérifier si HashPack est installé
        var hashpackInstalled = false
        var hashpackPackageName: String? = null
        
        for (packageName in hashpackPackages) {
            try {
                context.packageManager.getPackageInfo(packageName, 0)
                hashpackInstalled = true
                hashpackPackageName = packageName
                android.util.Log.d("HashPack", "✅ HashPack trouvé: $packageName")
                break
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                // Package non trouvé, continuer
            }
        }
        
        if (hashpackInstalled && hashpackPackageName != null) {
            // HashPack est installé, essayer d'ouvrir avec deep link en forçant le package
            val deepLinkFormats = listOf(
                "hashpack://transfer?tokenId=${Uri.encode(transferData.tokenId)}&serialNumber=${Uri.encode(transferData.serialNumber)}&recipient=${Uri.encode(transferData.recipientAccountId)}",
                "hashpack://nft/transfer?tokenId=${Uri.encode(transferData.tokenId)}&serialNumber=${Uri.encode(transferData.serialNumber)}&recipient=${Uri.encode(transferData.recipientAccountId)}",
                "hashpack://transaction?type=nft-transfer&tokenId=${Uri.encode(transferData.tokenId)}&serialNumber=${Uri.encode(transferData.serialNumber)}&recipient=${Uri.encode(transferData.recipientAccountId)}"
            )
            
            var hashpackOpened = false
            
            // Essayer chaque format avec le package explicite
            for (linkFormat in deepLinkFormats) {
                try {
                    val hashpackUri = Uri.parse(linkFormat)
                    val intent = Intent(Intent.ACTION_VIEW, hashpackUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage(hashpackPackageName) // Forcer l'utilisation de HashPack
                    }
                    
                    // Vérifier si HashPack peut gérer ce format
                    if (intent.resolveActivity(context.packageManager) != null) {
                        android.util.Log.d("HashPack", "✅ Format supporté: $linkFormat")
                        context.startActivity(intent)
                        hashpackOpened = true
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.w("HashPack", "⚠️ Format non supporté: $linkFormat - ${e.message}")
                }
            }
            
            // Si les deep links ne fonctionnent pas, ouvrir HashPack directement
            if (!hashpackOpened) {
                android.util.Log.w("HashPack", "⚠️ Deep links non supportés, ouverture directe de HashPack")
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(hashpackPackageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        hashpackOpened = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HashPack", "❌ Erreur ouverture directe: ${e.message}", e)
                }
            }
            
            if (!hashpackOpened) {
                android.util.Log.w("HashPack", "⚠️ Impossible d'ouvrir HashPack, affichage du message dans le dialogue")
            }
        } else {
            // HashPack n'est pas installé
            android.util.Log.w("HashPack", "⚠️ HashPack n'est pas installé")
            // Ne pas ouvrir le navigateur automatiquement, juste afficher le message dans le dialogue
            // L'utilisateur pourra installer HashPack depuis le dialogue
        }
    } catch (e: Exception) {
        android.util.Log.e("HashPack", "❌ Erreur lors de l'ouverture de HashPack: ${e.message}", e)
    }
}

@Composable
fun LevelSelectionView(
    onNiveauSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sélectionnez un niveau",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Niveau 1
        LevelCard(
            niveau = 1,
            title = "Niveau 1 - Débutant",
            description = "3 stages d'entraînement pour débuter",
            onSelect = { onNiveauSelected(1) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Niveau 2 (verrouillé pour l'instant)
        LevelCard(
            niveau = 2,
            title = "Niveau 2 - Intermédiaire",
            description = "Bientôt disponible",
            onSelect = null,
            isLocked = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Niveau 3 (verrouillé pour l'instant)
        LevelCard(
            niveau = 3,
            title = "Niveau 3 - Avancé",
            description = "Bientôt disponible",
            onSelect = null,
            isLocked = true
        )
    }
}

@Composable
fun LevelCard(
    niveau: Int,
    title: String,
    description: String,
    onSelect: (() -> Unit)?,
    isLocked: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) Color.LightGray else DarkBlue
        ),
        onClick = { onSelect?.invoke() },
        enabled = !isLocked && onSelect != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = niveau.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }
            if (isLocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Verrouillé",
                    tint = White
                )
            } else {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Sélectionner",
                    tint = White
                )
            }
        }
    }
}

@Composable
fun StageSelectionView(
    niveau: Int,
    onStageSelected: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bouton retour
        TextButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retour aux niveaux")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Niveau $niveau - Sélectionnez un stage",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Stage 1: Scène
        StageCard(
            stage = 1,
            title = "Stage 1: Scène à jouer",
            description = "Une scène vous sera donnée. Jouez-la et envoyez votre vidéo.",
            icon = Icons.Default.TheaterComedy,
            onSelect = { onStageSelected(1) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stage 2: Script
        StageCard(
            stage = 2,
            title = "Stage 2: Script à jouer",
            description = "Un script vous sera donné. Interprétez-le et envoyez votre vidéo.",
            icon = Icons.Default.MenuBook,
            onSelect = { onStageSelected(2) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stage 3: Improvisation
        StageCard(
            stage = 3,
            title = "Stage 3: Improvisation",
            description = "Une description vous sera donnée. Improvisez et envoyez votre vidéo.",
            icon = Icons.Default.Lightbulb,
            onSelect = { onStageSelected(3) }
        )
    }
}

@Composable
fun StageCard(
    stage: Int,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = DarkBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Sélectionner",
                tint = DarkBlue
            )
        }
    }
}

@Composable
fun TrainingStageView(
    niveau: Int,
    stage: Int,
    stagePrompt: String,
    selectedVideoUri: Uri?,
    isAnalyzing: Boolean,
    isSubmitting: Boolean,
    feedback: TrainingFeedback?,
    errorMessage: String?,
    successMessage: String?,
    nftRewarded: Boolean,
    nftReward: NFTReward?,
    hasHashpackAccount: Boolean = false,
    isTransferringNFT: Boolean,
    trainingRepository: TrainingRepository,
    onVideoSelected: () -> Unit,
    onAnalyzeClick: (Uri) -> Unit,
                    onResetClick: () -> Unit,
    onBackClick: () -> Unit,
    onTransferNFT: (String) -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bouton retour
        TextButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retour aux stages")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Titre
        Text(
            "Niveau $niveau - Stage $stage",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Carte avec le prompt
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF9C4)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (stage) {
                            1 -> Icons.Default.TheaterComedy
                            2 -> Icons.Default.MenuBook
                            else -> Icons.Default.Lightbulb
                        },
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        when (stage) {
                            1 -> "Scène à jouer"
                            2 -> "Script à jouer"
                            else -> "Improvisation"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stagePrompt,
                    fontSize = 14.sp,
                    color = Color(0xFF856404),
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Si pas de vidéo sélectionnée
        if (selectedVideoUri == null) {
            Button(
                onClick = onVideoSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.VideoCall, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📹 Choisir une vidéo", fontSize = 16.sp)
            }
        } else {
            // Vidéo sélectionnée
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Vidéo sélectionnée",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            "Prête pour l'analyse",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onResetClick) {
                        Icon(Icons.Default.Close, contentDescription = "Supprimer")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bouton d'analyse
            Button(
                onClick = { onAnalyzeClick(selectedVideoUri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isAnalyzing && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing || isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isAnalyzing) "⏳ Analyse en cours..." else "💾 Enregistrement...",
                        fontSize = 16.sp
                    )
                } else {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🚀 Analyser ma performance", fontSize = 16.sp)
                }
            }
        }
        
        // Message d'information pendant l'analyse
        if (isAnalyzing || isSubmitting) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3CD)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isAnalyzing) {
                            "L'IA Gemini analyse votre vidéo...\nCela peut prendre 30-60 secondes"
                        } else {
                            "Enregistrement de votre session..."
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF856404)
                    )
                }
            }
        }
        
        // Afficher les erreurs
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8D7DA)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFF721C24),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        error,
                        color = Color(0xFF721C24),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Afficher les messages de succès
        successMessage?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (nftRewarded) Color(0xFFD4EDDA) else Color(0xFFD1ECF1)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            if (nftRewarded) Icons.Default.Celebration else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (nftRewarded) Color(0xFF155724) else Color(0xFF0C5460),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                message,
                                color = if (nftRewarded) Color(0xFF155724) else Color(0xFF0C5460),
                                fontSize = 14.sp
                            )
                            
                            // Afficher les informations NFT si disponible
                            nftReward?.let { nft ->
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Token ID
                                Text(
                                    "Token ID: ${nft.tokenId}",
                                    color = Color(0xFF155724),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                // Numéro de série
                                if (nft.serialNumber.isNotEmpty()) {
                                    Text(
                                        "Numéro de série: ${nft.serialNumber}",
                                        color = Color(0xFF155724),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Bouton conditionnel selon si HashPack est associé
                                if (hasHashpackAccount) {
                                    // Si HashPack est associé, le transfert est automatique
                                    // Afficher un bouton pour ouvrir HashPack et voir le NFT
                                    Button(
                                        onClick = {
                                            // Ouvrir HashPack directement
                                            val mockTransferData = NFTTransferData(
                                                tokenId = nft.tokenId,
                                                serialNumber = nft.serialNumber,
                                                recipientAccountId = "0.0.0", // Non utilisé pour l'ouverture simple
                                                treasuryAccountId = "0.0.0"
                                            )
                                            openHashPackForTransfer(context, mockTransferData)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF155724)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.GetApp,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ouvrir HashPack pour voir mon NFT", fontSize = 14.sp)
                                    }
                                } else {
                                    // Si HashPack n'est pas associé, proposer le transfert manuel
                                    Button(
                                        onClick = {
                                            nft.actualId?.let { nftId ->
                                                onTransferNFT(nftId)
                                            }
                                        },
                                        enabled = !isTransferringNFT && nft.actualId != null,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF155724)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (isTransferringNFT) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Transfert en cours...", fontSize = 14.sp)
                                        } else {
                                            Icon(
                                                Icons.Default.GetApp,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Transférer mon NFT dans HashPack", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Afficher le feedback
        feedback?.let { fb ->
            Spacer(modifier = Modifier.height(24.dp))
            // Réutiliser le composant FeedbackResultCard depuis ActorTrainingScreen
            FeedbackResultCard(feedback = fb)
        }
    }
}

