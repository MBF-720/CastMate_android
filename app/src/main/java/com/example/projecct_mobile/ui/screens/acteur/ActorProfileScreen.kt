package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Page de profil éditable pour les acteurs
 */
@Composable
fun ActorProfileScreen(
    onBackClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    loadData: Boolean = true,
    initialNom: String = "",
    initialPrenom: String = "",
    initialEmail: String = "",
    initialTelephone: String = "",
    initialAge: String = "",
    initialGouvernorat: String = "",
    initialExperience: String = "",
    initialInstagram: String = "",
    initialYoutube: String = "",
    initialTiktok: String = ""
) {
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showComingSoon by remember { mutableStateOf<String?>(null) }
    
    var nom by remember { mutableStateOf(initialNom) }
    var prenom by remember { mutableStateOf(initialPrenom) }
    var email by remember { mutableStateOf(initialEmail) }
    var telephone by remember { mutableStateOf(initialTelephone) }
    var age by remember { mutableStateOf(initialAge) }
    var gouvernorat by remember { mutableStateOf(initialGouvernorat) }
    var experience by remember { mutableStateOf(initialExperience) }
    var instagram by remember { mutableStateOf(initialInstagram) }
    var youtube by remember { mutableStateOf(initialYoutube) }
    var tiktok by remember { mutableStateOf(initialTiktok) }
    
    // État pour stocker le profil acteur complet et la photo
    var acteurProfile by remember { mutableStateOf<ActeurProfile?>(null) }
    var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedPhotoFile by remember { mutableStateOf<File?>(null) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    
    val acteurRepository = remember(loadData) {
        if (loadData) ActeurRepository() else null
    }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // File picker pour choisir une nouvelle photo
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        
        val copiedFile = copyUriToCache(context, uri, "profile_photo")
        if (copiedFile != null) {
            selectedPhotoFile = copiedFile
            // Prévisualiser la nouvelle photo
            val bitmap = BitmapFactory.decodeFile(copiedFile.absolutePath)
            if (bitmap != null) {
                profileImage = bitmap.asImageBitmap()
                android.util.Log.e("ActorProfileScreen", "✅ Nouvelle photo sélectionnée: ${copiedFile.name}")
            }
        }
    }
    
    LaunchedEffect(Unit, loadData) {
        if (!loadData) return@LaunchedEffect
        if (!isEditing) {
            isLoading = true
            errorMessage = null
            
            try {
                android.util.Log.e("ActorProfileScreen", "=== Début du chargement du profil acteur ===")
                val result = acteurRepository?.getCurrentActeur()
                
                result?.onSuccess { acteur ->
                    android.util.Log.e("ActorProfileScreen", "✅ Profil acteur chargé: ${acteur.nom} ${acteur.prenom}")
                    android.util.Log.e("ActorProfileScreen", "✅ photoFileId: '${acteur.media?.photoFileId}'")
                    
                    // Stocker le profil acteur complet
                    acteurProfile = acteur
                    
                    // Mettre à jour les champs individuels
                    nom = acteur.nom ?: ""
                    prenom = acteur.prenom ?: ""
                    email = acteur.email ?: ""
                    telephone = acteur.tel ?: ""
                    age = acteur.age?.toString() ?: ""
                    gouvernorat = acteur.gouvernorat ?: ""
                    experience = acteur.experience?.toString() ?: ""
                    instagram = acteur.socialLinks?.instagram ?: ""
                    youtube = acteur.socialLinks?.youtube ?: ""
                    tiktok = acteur.socialLinks?.tiktok ?: ""
                    isLoading = false
                }
                
                result?.onFailure { exception ->
                    android.util.Log.e("ActorProfileScreen", "❌ Impossible de charger le profil acteur: ${exception.message}")
                    try {
                        val tokenManager = TokenManager(context)
                        val emailValue = tokenManager.getUserEmailSync()
                        if (emailValue != null) {
                            email = emailValue
                        }
                        errorMessage = null
                    } catch (e: Exception) {
                        errorMessage = null
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                android.util.Log.e("ActorProfileScreen", "❌ Erreur lors du chargement: ${e.message}", e)
                try {
                    val tokenManager = TokenManager(context)
                    val emailValue = tokenManager.getUserEmailSync()
                    if (emailValue != null) {
                        email = emailValue
                    }
                } catch (ex: Exception) {
                    // Ignorer
                }
                errorMessage = null
                isLoading = false
            }
        }
    }
    
    // Télécharger la photo quand acteurProfile est disponible
    // Utiliser un état pour suivre le dernier photoFileId téléchargé
    var lastDownloadedPhotoFileId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(acteurProfile?.media?.photoFileId) {
        val photoFileId = acteurProfile?.media?.photoFileId
        android.util.Log.e("ActorProfileScreen", "🔵 LaunchedEffect(photoFileId) déclenché: '$photoFileId', dernier téléchargé: '$lastDownloadedPhotoFileId'")
        
        // Télécharger la photo si elle n'a pas encore été téléchargée ou si le photoFileId a changé
        if (!photoFileId.isNullOrBlank() && photoFileId != lastDownloadedPhotoFileId && selectedPhotoFile == null) {
            try {
                android.util.Log.e("ActorProfileScreen", "🚀🚀🚀 Téléchargement de la photo: $photoFileId 🚀🚀🚀")
                val mediaResult = acteurRepository?.downloadMedia(photoFileId)
                android.util.Log.e("ActorProfileScreen", "📥 Résultat téléchargement: ${mediaResult?.isSuccess}")
                
                if (mediaResult?.isSuccess == true) {
                    val bytes = mediaResult.getOrNull()
                    android.util.Log.e("ActorProfileScreen", "📦 Bytes reçus: ${bytes?.size ?: 0} bytes")
                    
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                        
                        if (bitmap != null) {
                            android.util.Log.e("ActorProfileScreen", "🖼️ Bitmap décodé: ${bitmap.width}x${bitmap.height}")
                            profileImage = bitmap.asImageBitmap()
                            lastDownloadedPhotoFileId = photoFileId
                            android.util.Log.e("ActorProfileScreen", "✅✅✅ Photo chargée avec succès! ✅✅✅")
                        } else {
                            android.util.Log.e("ActorProfileScreen", "❌ Bitmap est null après décodage")
                        }
                    } else {
                        android.util.Log.e("ActorProfileScreen", "⚠️ Bytes vides ou null")
                    }
                } else {
                    val exception = mediaResult?.exceptionOrNull()
                    android.util.Log.e("ActorProfileScreen", "❌ Erreur téléchargement: ${exception?.message}", exception)
                }
            } catch (e: CancellationException) {
                android.util.Log.e("ActorProfileScreen", "⚠️ Téléchargement annulé")
            } catch (e: Exception) {
                android.util.Log.e("ActorProfileScreen", "❌ Exception téléchargement: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(DarkBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Mon Profil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            android.util.Log.d("ActorProfileScreen", "Bouton cliqué, isEditing actuel: $isEditing")
                            if (isEditing) {
                                // Sauvegarder les modifications
                                android.util.Log.d("ActorProfileScreen", "Sauvegarde en cours...")
                                
                                // Valider l'email si fourni
                                if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMessage = "L'email n'est pas valide"
                                    return@clickable
                                }
                                
                                scope.launch {
                                    if (acteurRepository == null) return@launch
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    
                                    try {
                                        val updateRequest = com.example.projecct_mobile.data.model.UpdateActeurRequest(
                                            nom = nom.takeIf { it.isNotBlank() },
                                            prenom = prenom.takeIf { it.isNotBlank() },
                                            email = email.takeIf { it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() },
                                            tel = telephone.takeIf { it.isNotBlank() },
                                            age = age.toIntOrNull(),
                                            gouvernorat = gouvernorat.takeIf { it.isNotBlank() },
                                            experience = experience.toIntOrNull(),
                                            socialLinks = com.example.projecct_mobile.data.model.SocialLinks(
                                                instagram = instagram.takeIf { it.isNotBlank() },
                                                youtube = youtube.takeIf { it.isNotBlank() },
                                                tiktok = tiktok.takeIf { it.isNotBlank() }
                                            )
                                        )
                                        
                                        // IMPORTANT: Ne pas passer l'ID de l'acteur depuis le profil chargé
                                        // Le backend vérifie que l'ID dans le token (sub) correspond à l'ID de l'acteur dans l'URL
                                        // Utiliser null pour que updateCurrentActeur utilise l'ID du token (sub) directement
                                        // Cela garantit que l'ID utilisé correspond exactement à celui dans le token
                                        android.util.Log.e("ActorProfileScreen", "📞 Mise à jour du profil (utilisation de l'ID du token)")
                                        
                                        // 1. Mettre à jour les informations du profil
                                        // Passer null pour utiliser l'ID du token (sub) directement
                                        val result = acteurRepository?.updateCurrentActeur(updateRequest, null)
                                        
                                        result?.onSuccess { updatedProfile ->
                                            android.util.Log.e("ActorProfileScreen", "✅ Profil mis à jour avec succès")
                                            android.util.Log.e("ActorProfileScreen", "✅ Profil mis à jour - ID depuis réponse: ${updatedProfile.actualId}")
                                            android.util.Log.e("ActorProfileScreen", "✅ Profil mis à jour - ID depuis id: ${updatedProfile.id}")
                                            android.util.Log.e("ActorProfileScreen", "✅ Profil mis à jour - ID depuis idAlt: ${updatedProfile.idAlt}")
                                            
                                            // Mettre à jour le profil avec la réponse
                                            acteurProfile = updatedProfile
                                            
                                            // IMPORTANT: Utiliser l'ID de l'acteur depuis le profil mis à jour
                                            // Utiliser le même ID que celui utilisé pour la mise à jour du profil
                                            // Le backend vérifie que l'ID dans le token (sub) correspond à l'ID de l'acteur dans l'URL
                                            val uploadId = updatedProfile.actualId
                                            android.util.Log.e("ActorProfileScreen", "✅✅✅ ID pour l'upload photo depuis le profil: $uploadId ✅✅✅")
                                            android.util.Log.e("ActorProfileScreen", "📤 ID depuis updatedProfile.actualId: ${updatedProfile.actualId}")
                                            android.util.Log.e("ActorProfileScreen", "📤 ID depuis updatedProfile.id: ${updatedProfile.id}")
                                            android.util.Log.e("ActorProfileScreen", "📤 ID depuis updatedProfile.idAlt: ${updatedProfile.idAlt}")
                                            
                                            // 2. Si une nouvelle photo a été sélectionnée, l'uploader
                                            val photoFile = selectedPhotoFile
                                            if (photoFile != null && uploadId != null && uploadId.isNotBlank()) {
                                                isUploadingPhoto = true
                                                try {
                                                    android.util.Log.e("ActorProfileScreen", "📤📤📤 Upload de la nouvelle photo avec l'ID depuis le profil: $uploadId 📤📤📤")
                                                    android.util.Log.e("ActorProfileScreen", "📤 ID utilisé pour l'upload: $uploadId")
                                                    android.util.Log.e("ActorProfileScreen", "📤 Fichier photo: ${photoFile.absolutePath}")
                                                    android.util.Log.e("ActorProfileScreen", "📤 Taille du fichier: ${photoFile.length()} bytes")
                                                    android.util.Log.e("ActorProfileScreen", "📤 Fichier existe: ${photoFile.exists()}")
                                                    
                                                    if (!photoFile.exists()) {
                                                        android.util.Log.e("ActorProfileScreen", "❌❌❌ Le fichier photo n'existe pas! ❌❌❌")
                                                        errorMessage = "Le fichier photo n'existe plus. Veuillez sélectionner une nouvelle photo."
                                                        isUploadingPhoto = false
                                                        isLoading = false
                                                        return@launch
                                                    }
                                                    
                                                    // Utiliser l'ID de l'acteur depuis le profil mis à jour
                                                    // Ce doit être le même ID que celui utilisé pour la mise à jour du profil
                                                    android.util.Log.e("ActorProfileScreen", "🚀🚀🚀 Upload avec l'ID depuis le profil: $uploadId 🚀🚀🚀")
                                                    
                                                    val photoResult = acteurRepository?.updateProfileMedia(
                                                        id = uploadId,
                                                        photoFile = photoFile,
                                                        documentFile = null
                                                    )
                                                    
                                                    photoResult?.onSuccess { profileWithPhoto ->
                                                        android.util.Log.e("ActorProfileScreen", "✅✅✅ Photo uploadée avec succès! ✅✅✅")
                                                        // Mettre à jour le profil avec la nouvelle photo
                                                        // Cela déclenchera automatiquement le LaunchedEffect pour recharger la photo
                                                        acteurProfile = profileWithPhoto
                                                        selectedPhotoFile = null
                                                        lastDownloadedPhotoFileId = null // Forcer le rechargement
                                                        isUploadingPhoto = false
                                                        
                                                        successMessage = "Profil et photo mis à jour avec succès"
                                                        isLoading = false
                                                        isEditing = false
                                                    }
                                                    
                                                    photoResult?.onFailure { photoException ->
                                                        android.util.Log.e("ActorProfileScreen", "❌ Erreur upload photo: ${photoException.message}")
                                                        isUploadingPhoto = false
                                                        // Le profil a été mis à jour, mais la photo n'a pas pu être uploadée
                                                        successMessage = "Profil mis à jour, mais erreur lors de l'upload de la photo: ${getErrorMessage(photoException)}"
                                                        isLoading = false
                                                        isEditing = false
                                                    }
                                                } catch (e: Exception) {
                                                    android.util.Log.e("ActorProfileScreen", "❌ Exception upload photo: ${e.message}", e)
                                                    isUploadingPhoto = false
                                                    successMessage = "Profil mis à jour, mais erreur lors de l'upload de la photo: ${e.message}"
                                                    isLoading = false
                                                    isEditing = false
                                                }
                                            } else {
                                                // Pas de nouvelle photo, juste mettre à jour le profil
                                                acteurProfile = updatedProfile
                                                successMessage = "Profil mis à jour avec succès"
                                                isLoading = false
                                                isEditing = false
                                            }
                                        }
                                        
                                        result?.onFailure { exception ->
                                            errorMessage = getErrorMessage(exception)
                                            isLoading = false
                                            android.util.Log.e("ActorProfileScreen", "Erreur lors de la sauvegarde: ${exception.message}")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = getErrorMessage(e)
                                        isLoading = false
                                        android.util.Log.e("ActorProfileScreen", "Exception lors de la sauvegarde: ${e.message}", e)
                                    }
                                }
                            } else {
                                // Activer le mode édition
                                android.util.Log.d("ActorProfileScreen", "Activation du mode édition")
                                isEditing = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Sauvegarder" else "Modifier",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        // Contenu
        if (isLoading && !isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else if (errorMessage != null && !isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ErrorMessage(
                    message = errorMessage ?: "Erreur",
                    onRetry = {
                        scope.launch {
                            if (acteurRepository == null) return@launch
                            isLoading = true
                            errorMessage = null
                            try {
                                val result = acteurRepository.getCurrentActeur()
                                result.onSuccess { acteur ->
                                    nom = acteur.nom ?: ""
                                    prenom = acteur.prenom ?: ""
                                    email = acteur.email ?: ""
                                    telephone = acteur.tel ?: ""
                                    age = acteur.age?.toString() ?: ""
                                    gouvernorat = acteur.gouvernorat ?: ""
                                    experience = acteur.experience?.toString() ?: ""
                                    instagram = acteur.socialLinks?.instagram ?: ""
                                    youtube = acteur.socialLinks?.youtube ?: ""
                                    tiktok = acteur.socialLinks?.tiktok ?: ""
                                    isLoading = false
                                }
                                result.onFailure { exception ->
                                    errorMessage = getErrorMessage(exception)
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = getErrorMessage(e)
                                isLoading = false
                            }
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(White)
            ) {
                // Contenu scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Message de succès
                successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(3000)
                        successMessage = null
                    }
                }
                
                // Message informatif si le profil n'a pas pu être chargé
                if (errorMessage == null && nom.isEmpty() && prenom.isEmpty() && email.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Remplissez vos informations pour compléter votre profil",
                            modifier = Modifier.padding(16.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Photo de profil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(LightGray)
                            .then(
                                if (isEditing) {
                                    Modifier.clickable {
                                        photoPicker.launch("image/*")
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            profileImage != null -> {
                                Image(
                                    bitmap = profileImage!!,
                                    contentDescription = "Photo de profil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                // Badge pour indiquer qu'on peut changer la photo en mode édition
                                if (isEditing) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DarkBlue)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Changer la photo",
                                            tint = White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.CameraAlt else Icons.Default.Person,
                                    contentDescription = "Photo de profil",
                                    tint = DarkBlue,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                    if (isEditing && selectedPhotoFile != null) {
                        Text(
                            text = "Nouvelle photo sélectionnée",
                            fontSize = 12.sp,
                            color = DarkBlue,
                            modifier = Modifier.padding(top = 108.dp)
                        )
                    }
                }
                
                // Message d'édition
                if (isEditing) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Mode édition activé - Modifiez vos informations",
                            modifier = Modifier.padding(12.dp),
                            color = DarkBlue,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Informations personnelles
                Text(
                    text = "Informations personnelles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = nom,
                    onValueChange = { if (isEditing) nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { if (isEditing) prenom = it },
                    label = { Text("Prénom") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (isEditing) email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = telephone,
                    onValueChange = { if (isEditing) telephone = it },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = age,
                    onValueChange = { if (isEditing) age = it },
                    label = { Text("Âge") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = gouvernorat,
                    onValueChange = { if (isEditing) gouvernorat = it },
                    label = { Text("Gouvernorat") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = experience,
                    onValueChange = { if (isEditing) experience = it },
                    label = { Text("Années d'expérience") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                // Liens sociaux
                Text(
                    text = "Réseaux sociaux",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                OutlinedTextField(
                    value = instagram,
                    onValueChange = { if (isEditing) instagram = it },
                    label = { Text("Instagram") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.Photo, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = youtube,
                    onValueChange = { if (isEditing) youtube = it },
                    label = { Text("YouTube") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.VideoLibrary, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                OutlinedTextField(
                    value = tiktok,
                    onValueChange = { if (isEditing) tiktok = it },
                    label = { Text("TikTok") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                    readOnly = !isEditing,
                    leadingIcon = { Icon(Icons.Default.MusicNote, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        unfocusedBorderColor = if (isEditing) DarkBlue else GrayBorder,
                        focusedTextColor = Black,
                        unfocusedTextColor = Black,
                        disabledTextColor = Black,
                        focusedLabelColor = DarkBlue,
                        unfocusedLabelColor = if (isEditing) DarkBlue else GrayBorder,
                        disabledLabelColor = GrayBorder,
                        disabledContainerColor = White
                    )
                )
                
                // Bouton de déconnexion
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Déconnexion",
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Déconnexion",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                
                // Espace en bas pour le scroll
                Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Navbar en bas
            ProfileBottomNavigationBar(
                onHomeClick = onHomeClick,
                onAgendaClick = onAgendaClick,
                onHistoryClick = { showComingSoon = "Historique" },
                onProfileClick = { /* Déjà sur le profil */ },
                onAdvancedClick = { showComingSoon = "Fonctionnalité avancée" }
            )
        }
    }
    
    // Alerte Coming Soon
    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
}

/**
 * Copie un URI vers le cache de l'application
 */
private fun copyUriToCache(context: Context, uri: Uri, prefix: String, forcedExtension: String? = null): File? {
    return try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val extension = forcedExtension ?: when {
            mimeType?.contains("png") == true -> ".png"
            mimeType?.contains("jpg") == true -> ".jpg"
            mimeType?.contains("jpeg") == true -> ".jpg"
            else -> ".tmp"
        }

        val inputStream: InputStream = resolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "$prefix-${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        file
    } catch (e: Exception) {
        android.util.Log.e("ActorProfileScreen", "Erreur lors de la copie du fichier: ${e.message}", e)
        null
    }
}

/**
 * Résout le nom de fichier depuis un URI
 */
private fun resolveFileName(context: Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else {
            null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActorProfileScreenPreview() {
    Projecct_MobileTheme {
        ActorProfileScreen(
            onBackClick = {},
            onLogoutClick = {},
            onHomeClick = {},
            onAgendaClick = {},
            onHistoryClick = {},
            loadData = false,
            initialNom = "Doe",
            initialPrenom = "Jane",
            initialEmail = "jane.doe@example.com",
            initialTelephone = "+33 6 12 34 56 78",
            initialAge = "28",
            initialGouvernorat = "Tunis",
            initialExperience = "5",
            initialInstagram = "@janedoe",
            initialYoutube = "youtube.com/janedoe",
            initialTiktok = "tiktok.com/@janedoe"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileBottomNavigationBarPreview() {
    Projecct_MobileTheme {
        ProfileBottomNavigationBar(
            onHomeClick = {},
            onAgendaClick = {},
            onHistoryClick = {},
            onProfileClick = {},
            onAdvancedClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileNavigationItemPreview() {
    Projecct_MobileTheme {
        ProfileNavigationItem(
            icon = Icons.Default.Home,
            label = "Accueil",
            onClick = {},
            isSelected = false
        )
    }
}


@Composable
private fun ProfileBottomNavigationBar(
    onHomeClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdvancedClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Navbar flottante avec transparence
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = DarkBlue.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                ProfileNavigationItem(
                    icon = Icons.Default.Home,
                    label = "Accueil",
                    onClick = onHomeClick,
                    isSelected = false
                )
                
                // Bouton + au milieu
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .clickable { onAdvancedClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Fonctionnalité avancée",
                        tint = White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Profile
                ProfileNavigationItem(
                    icon = Icons.Default.Person,
                    label = "Profil",
                    onClick = onProfileClick,
                    isSelected = true
                )
            }
        }
    }
}

@Composable
private fun ProfileNavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DarkBlue else GrayBorder,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) DarkBlue else GrayBorder,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

