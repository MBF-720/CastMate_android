package com.example.projecct_mobile.ui.screens.acteur

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.border
import com.example.projecct_mobile.R
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.projecct_mobile.data.cache.GalleryPhotoCache
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.components.ComingSoonAlert
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.components.ActorBottomNavigationBar
import com.example.projecct_mobile.ui.components.NavigationItem
import com.example.projecct_mobile.ui.theme.*
import com.example.projecct_mobile.utils.SocialLinkValidator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    onMyCandidaturesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
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
    
    // État pour le CV PDF
    var selectedDocumentFile by remember { mutableStateOf<File?>(null) }
    var isUploadingDocument by remember { mutableStateOf(false) }
    var isDownloadingDocument by remember { mutableStateOf(false) }
    var lastDownloadedDocumentFileId by remember { mutableStateOf<String?>(null) }
    
    // État pour la galerie de photos
    var galleryPhotos by remember { mutableStateOf<List<Pair<String, ImageBitmap>>>(emptyList()) }
    var isUploadingGalleryPhotos by remember { mutableStateOf(false) }
    var isLoadingGallery by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPhotoIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isDeletingPhotos by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) } // Index de la photo affichée en plein écran
    
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
    
    // File picker pour choisir un nouveau CV PDF
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        
        val copiedFile = copyUriToCache(context, uri, "cv_document", ".pdf")
        if (copiedFile != null) {
            selectedDocumentFile = copiedFile
            android.util.Log.e("ActorProfileScreen", "✅ Nouveau CV PDF sélectionné: ${copiedFile.name}")
            android.util.Log.e("ActorProfileScreen", "📄 Taille du fichier: ${copiedFile.length()} bytes")
        } else {
            android.util.Log.e("ActorProfileScreen", "❌ Impossible de copier le fichier PDF")
            errorMessage = "Impossible de copier le fichier PDF. Veuillez réessayer."
        }
    }
    
    // File picker pour sélectionner plusieurs photos pour la galerie
    val galleryPhotosPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        
        scope.launch {
            isUploadingGalleryPhotos = true
            errorMessage = null
            
            try {
                val photoFiles = mutableListOf<File>()
                uris.forEach { uri ->
                    val copiedFile = copyUriToCache(context, uri, "gallery_photo")
                    if (copiedFile != null) {
                        photoFiles.add(copiedFile)
                    }
                }
                
                if (photoFiles.isEmpty()) {
                    errorMessage = "Impossible de copier les photos sélectionnées."
                    isUploadingGalleryPhotos = false
                    return@launch
                }
                
                // Upload des photos
                val acteurId = acteurRepository?.getCurrentActeurId()
                if (acteurId == null) {
                    errorMessage = "Impossible de récupérer l'ID de l'acteur."
                    isUploadingGalleryPhotos = false
                    return@launch
                }
                
                val result = acteurRepository.addGalleryPhotos(acteurId, photoFiles)
                result.onSuccess { updatedProfile ->
                    acteurProfile = updatedProfile
                    successMessage = "${photoFiles.size} photo(s) ajoutée(s) avec succès!"
                    // Le LaunchedEffect se déclenchera automatiquement quand acteurProfile changera
                }
                result.onFailure { exception ->
                    errorMessage = getErrorMessage(exception)
                    android.util.Log.e("ActorProfileScreen", "❌ Erreur upload galerie: ${exception.message}")
                }
            } catch (e: Exception) {
                errorMessage = "Erreur lors de l'ajout des photos: ${e.message}"
                android.util.Log.e("ActorProfileScreen", "❌ Exception upload galerie: ${e.message}", e)
            } finally {
                isUploadingGalleryPhotos = false
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
    
    // Charger la galerie quand le profil est chargé
    LaunchedEffect(acteurProfile?.media?.gallery) {
        val gallery = acteurProfile?.media?.gallery
        if (gallery.isNullOrEmpty()) {
            galleryPhotos = emptyList()
            return@LaunchedEffect
        }
        
        isLoadingGallery = true
        
        // Extraire les fileIds
        val photos = gallery.mapNotNull { mediaRef ->
            mediaRef.fileId?.takeIf { it.isNotBlank() }
        }
        
        // Charger les photos en utilisant le cache (en parallèle)
        val loadedPhotos = photos.map { fileId ->
            async {
                try {
                    // Vérifier d'abord le cache
                    val cachedBitmap = GalleryPhotoCache.get(context, fileId)
                    if (cachedBitmap != null) {
                        android.util.Log.d("ActorProfileScreen", "✅ Photo $fileId chargée depuis le cache")
                        Pair(fileId, cachedBitmap)
                    } else {
                        // Si pas en cache, télécharger
                        android.util.Log.d("ActorProfileScreen", "📥 Téléchargement photo $fileId...")
                        val mediaResult = acteurRepository?.downloadMedia(fileId)
                        mediaResult?.getOrNull()?.let { bytes ->
                            if (bytes.isNotEmpty()) {
                                // Mettre en cache et décoder
                                val imageBitmap = GalleryPhotoCache.put(context, fileId, bytes)
                                imageBitmap?.let { Pair(fileId, it) }
                            } else null
                        } ?: null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ActorProfileScreen", "❌ Erreur chargement photo galerie $fileId: ${e.message}")
                    null
                }
            }
        }.awaitAll().filterNotNull()
        
        galleryPhotos = loadedPhotos
        isLoadingGallery = false
        android.util.Log.d("ActorProfileScreen", "✅ Galerie chargée: ${loadedPhotos.size}/${photos.size} photos")
    }
    
    // Fonction helper pour convertir un nom d'utilisateur ou URL en URL complète
    fun String?.isValidSocialLink(): Boolean {
        if (this == null || this.isBlank()) return false
        val trimmed = this.trim()
        return trimmed != "null" && trimmed.isNotBlank()
    }
    
    fun getInstagramUrl(input: String?): String? {
        if (!input.isValidSocialLink()) return null
        val trimmed = input!!.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("instagram.com/") -> "https://$trimmed"
            trimmed.startsWith("www.instagram.com/") -> "https://$trimmed"
            trimmed.startsWith("@") -> "https://www.instagram.com/${trimmed.substring(1)}"
            else -> "https://www.instagram.com/$trimmed"
        }
    }
    
    fun getYouTubeUrl(input: String?): String? {
        if (!input.isValidSocialLink()) return null
        val trimmed = input!!.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("youtube.com/") || trimmed.startsWith("youtu.be/") -> "https://$trimmed"
            trimmed.startsWith("www.youtube.com/") -> "https://$trimmed"
            trimmed.startsWith("@") -> "https://www.youtube.com/$trimmed"
            else -> "https://www.youtube.com/$trimmed"
        }
    }
    
    fun getTikTokUrl(input: String?): String? {
        if (!input.isValidSocialLink()) return null
        val trimmed = input!!.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("tiktok.com/") -> "https://$trimmed"
            trimmed.startsWith("www.tiktok.com/") -> "https://$trimmed"
            trimmed.startsWith("@") -> "https://www.tiktok.com/$trimmed"
            else -> "https://www.tiktok.com/@$trimmed"
        }
    }
    
    val instagramValue = instagram.takeIf { it.isValidSocialLink() } 
        ?: acteurProfile?.socialLinks?.instagram?.takeIf { it.isValidSocialLink() }
    val youtubeValue = youtube.takeIf { it.isValidSocialLink() } 
        ?: acteurProfile?.socialLinks?.youtube?.takeIf { it.isValidSocialLink() }
    val tiktokValue = tiktok.takeIf { it.isValidSocialLink() } 
        ?: acteurProfile?.socialLinks?.tiktok?.takeIf { it.isValidSocialLink() }
    
    val finalInstagramUrl = getInstagramUrl(instagramValue)
    val finalYouTubeUrl = getYouTubeUrl(youtubeValue)
    val finalTikTokUrl = getTikTokUrl(tiktokValue)
    
    // Statistiques (placeholder - à implémenter plus tard)
    var followersCount by remember { mutableStateOf(0) }
    var followingCount by remember { mutableStateOf(0) }
    var projectsCount by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header avec gradient bleu foncé et forme ondulée blanche
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkBlue, DarkBlueLight)
                        )
                    )
            )
            
            // Forme ondulée blanche en bas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            )
            
            // Contenu du header
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Bouton retour
                Row(
                    modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.18f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                    // Bouton Edit/Save
                Box(
                    modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.18f))
                        .clickable {
                            android.util.Log.d("ActorProfileScreen", "Bouton cliqué, isEditing actuel: $isEditing")
                            if (isEditing) {
                                // Sauvegarder les modifications
                                android.util.Log.d("ActorProfileScreen", "Sauvegarde en cours...")
                                
                                    try {
                                // Valider l'email si fourni
                                if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMessage = "L'email n'est pas valide"
                                            isEditing = true // Garder le mode édition pour afficher l'erreur
                                            return@clickable
                                        }
                                        
                                        // Validation des liens Instagram
                                        android.util.Log.d("ActorProfileScreen", "🔍 Validation Instagram: '$instagram'")
                                        val instagramValidation = SocialLinkValidator.validateInstagram(instagram)
                                        if (instagramValidation is SocialLinkValidator.ValidationResult.Error) {
                                            android.util.Log.e("ActorProfileScreen", "❌ Erreur validation Instagram: ${instagramValidation.message}")
                                            errorMessage = instagramValidation.message
                                            isEditing = true // Garder le mode édition pour afficher l'erreur
                                            return@clickable
                                        }
                                        
                                        // Validation des liens YouTube
                                        android.util.Log.d("ActorProfileScreen", "🔍 Validation YouTube: '$youtube'")
                                        val youtubeValidation = SocialLinkValidator.validateYouTube(youtube)
                                        if (youtubeValidation is SocialLinkValidator.ValidationResult.Error) {
                                            android.util.Log.e("ActorProfileScreen", "❌ Erreur validation YouTube: ${youtubeValidation.message}")
                                            errorMessage = youtubeValidation.message
                                            isEditing = true // Garder le mode édition pour afficher l'erreur
                                            return@clickable
                                        }
                                        
                                        // Validation des liens TikTok
                                        android.util.Log.d("ActorProfileScreen", "🔍 Validation TikTok: '$tiktok'")
                                        val tiktokValidation = SocialLinkValidator.validateTikTok(tiktok)
                                        if (tiktokValidation is SocialLinkValidator.ValidationResult.Error) {
                                            android.util.Log.e("ActorProfileScreen", "❌ Erreur validation TikTok: ${tiktokValidation.message}")
                                            errorMessage = tiktokValidation.message
                                            isEditing = true // Garder le mode édition pour afficher l'erreur
                                            return@clickable
                                        }
                                        
                                        android.util.Log.d("ActorProfileScreen", "✅ Toutes les validations passées, lancement de la sauvegarde...")
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileScreen", "❌ Exception lors de la validation: ${e.message}", e)
                                        errorMessage = "Erreur lors de la validation: ${e.message}"
                                        isEditing = true
                                    return@clickable
                                }
                                
                                scope.launch {
                                    if (acteurRepository == null) return@launch
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    
                                    try {
                                            // Normaliser les liens avant l'envoi
                                            val normalizedInstagram = instagram.takeIf { it.isNotBlank() }?.let { 
                                                SocialLinkValidator.normalizeInstagram(it.trim()) 
                                            }
                                            val normalizedYouTube = youtube.takeIf { it.isNotBlank() }?.let { 
                                                SocialLinkValidator.normalizeYouTube(it.trim()) 
                                            }
                                            val normalizedTikTok = tiktok.takeIf { it.isNotBlank() }?.let { 
                                                SocialLinkValidator.normalizeTikTok(it.trim()) 
                                            }
                                            
                                        val updateRequest = com.example.projecct_mobile.data.model.UpdateActeurRequest(
                                            nom = nom.takeIf { it.isNotBlank() },
                                            prenom = prenom.takeIf { it.isNotBlank() },
                                            email = email.takeIf { it.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() },
                                            tel = telephone.takeIf { it.isNotBlank() },
                                            age = age.toIntOrNull(),
                                            gouvernorat = gouvernorat.takeIf { it.isNotBlank() },
                                            experience = experience.toIntOrNull(),
                                            socialLinks = com.example.projecct_mobile.data.model.SocialLinks(
                                                    instagram = normalizedInstagram,
                                                    youtube = normalizedYouTube,
                                                    tiktok = normalizedTikTok
                                                )
                                            )
                                            
                                            android.util.Log.e("ActorProfileScreen", "📞 Mise à jour du profil (utilisation de l'ID du token)")
                                            
                                            val result = acteurRepository?.updateCurrentActeur(updateRequest, null)
                                            
                                            result?.onSuccess { updatedProfile ->
                                                android.util.Log.e("ActorProfileScreen", "✅ Profil mis à jour avec succès")
                                                
                                                acteurProfile = updatedProfile
                                                
                                                val uploadId = updatedProfile.actualId
                                                
                                                val photoFile = selectedPhotoFile
                                                val documentFile = selectedDocumentFile
                                                
                                                if ((photoFile != null || documentFile != null) && uploadId != null && uploadId.isNotBlank()) {
                                                    if (photoFile != null) isUploadingPhoto = true
                                                    if (documentFile != null) isUploadingDocument = true
                                                    
                                                    try {
                                                        if (photoFile != null && !photoFile.exists()) {
                                                            errorMessage = "Le fichier photo n'existe plus. Veuillez sélectionner une nouvelle photo."
                                                            isUploadingPhoto = false
                                                            isUploadingDocument = false
                                                            isLoading = false
                                                            return@launch
                                                        }
                                                        
                                                        if (documentFile != null && !documentFile.exists()) {
                                                            errorMessage = "Le fichier PDF n'existe plus. Veuillez sélectionner un nouveau fichier."
                                                            isUploadingPhoto = false
                                                            isUploadingDocument = false
                                                            isLoading = false
                                                            return@launch
                                                        }
                                                        
                                                        val mediaResult = acteurRepository?.updateProfileMedia(
                                                            id = uploadId,
                                                            photoFile = photoFile,
                                                            documentFile = documentFile
                                                        )
                                                        
                                                        mediaResult?.onSuccess { profileWithMedia ->
                                                            acteurProfile = profileWithMedia
                                                            selectedPhotoFile = null
                                                            selectedDocumentFile = null
                                                            lastDownloadedPhotoFileId = null
                                                            lastDownloadedDocumentFileId = null
                                                            isUploadingPhoto = false
                                                            isUploadingDocument = false
                                                            
                                                            val filesUpdated = mutableListOf<String>()
                                                            if (photoFile != null) filesUpdated.add("photo")
                                                            if (documentFile != null) filesUpdated.add("CV PDF")
                                                            successMessage = "Profil et ${filesUpdated.joinToString(", ")} mis à jour avec succès"
                                                            
                                                            isLoading = false
                                                            isEditing = false
                                                        }
                                                        
                                                        mediaResult?.onFailure { mediaException ->
                                                            isUploadingPhoto = false
                                                            isUploadingDocument = false
                                                            val filesAttempted = mutableListOf<String>()
                                                            if (photoFile != null) filesAttempted.add("photo")
                                                            if (documentFile != null) filesAttempted.add("CV PDF")
                                                            successMessage = "Profil mis à jour, mais erreur lors de l'upload du/de la ${filesAttempted.joinToString("/")}: ${getErrorMessage(mediaException)}"
                                                            isLoading = false
                                                            isEditing = false
                                                        }
                                                    } catch (e: Exception) {
                                                        isUploadingPhoto = false
                                                        isUploadingDocument = false
                                                        val filesAttempted = mutableListOf<String>()
                                                        if (photoFile != null) filesAttempted.add("photo")
                                                        if (documentFile != null) filesAttempted.add("CV PDF")
                                                        successMessage = "Profil mis à jour, mais erreur lors de l'upload du/de la ${filesAttempted.joinToString("/")}: ${e.message}"
                                                        isLoading = false
                                                        isEditing = false
                                                    }
                                                } else {
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
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Row avec nom/Castings à gauche et photo à droite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colonne à gauche : Nom et Castings
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Nom
                        Text(
                            text = "${prenom.ifBlank { "" }} ${nom.ifBlank { "" }}".trim().ifBlank { "Acteur" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            textAlign = TextAlign.Start
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Castings
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = projectsCount.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = "Castings",
                                fontSize = 16.sp,
                                color = White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    
                    // Photo de profil à droite
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f))
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                spotColor = White.copy(alpha = 0.25f)
                            )
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
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.9f)),
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
                                    if (isEditing) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(DarkBlue)
                                                .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Changer la photo",
                                                tint = White,
                                                modifier = Modifier.size(18.dp)
                                            )
            }
                                    }
                                }
                                else -> {
                                    Icon(
                                        imageVector = if (isEditing) Icons.Default.CameraAlt else Icons.Default.Person,
                                        contentDescription = "Photo de profil",
                                        tint = DarkBlue,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Contenu principal scrollable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                .weight(1f)
                .background(White)
        ) {
            if (isLoading && !isEditing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else if (errorMessage != null && !isEditing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icônes de réseaux sociaux
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (instagramValue != null) {
                            SocialMediaIcon(
                                painter = painterResource(id = R.drawable.instagram),
                                onClick = {
                                    try {
                                        val url = finalInstagramUrl ?: "https://www.instagram.com"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileScreen", "Erreur ouverture Instagram: ${e.message}", e)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        // Note: YouTube et TikTok n'ont pas d'icônes dans drawables pour l'instant
                        // On utilise des icônes Material pour l'instant
                        if (youtubeValue != null) {
                            SocialMediaIconWithVector(
                                imageVector = Icons.Default.PlayCircle,
                                onClick = {
                                    try {
                                        val url = finalYouTubeUrl ?: "https://www.youtube.com"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileScreen", "Erreur ouverture YouTube: ${e.message}", e)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        if (tiktokValue != null) {
                            SocialMediaIconWithVector(
                                imageVector = Icons.Default.MusicNote,
                                onClick = {
                                    try {
                                        val url = finalTikTokUrl ?: "https://www.tiktok.com"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileScreen", "Erreur ouverture TikTok: ${e.message}", e)
                                    }
                                }
                            )
                        }
                    }
                    
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
                    
                    // Message d'erreur
                    errorMessage?.let { message ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(16.dp),
                                color = Red,
                                fontWeight = FontWeight.Medium
                            )
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
                
                    // Message d'erreur (affiché en mode édition pour les erreurs de validation)
                    if (isEditing && errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Erreur",
                                    tint = Red,
                                    modifier = Modifier.size(20.dp)
                                )
                Text(
                                    text = errorMessage ?: "",
                                    color = Red,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Section Galerie de photos
                    GallerySection(
                        galleryPhotos = galleryPhotos,
                        isLoadingGallery = isLoadingGallery,
                        isUploadingGalleryPhotos = isUploadingGalleryPhotos,
                        isSelectionMode = isSelectionMode,
                        selectedPhotoIds = selectedPhotoIds,
                        isDeletingPhotos = isDeletingPhotos,
                        onAddPhotosClick = {
                            galleryPhotosPicker.launch("image/*")
                        },
                        onPhotoLongPress = { fileId ->
                            isSelectionMode = true
                            selectedPhotoIds = selectedPhotoIds + fileId
                        },
                        onPhotoClick = { fileId ->
                            if (isSelectionMode) {
                                selectedPhotoIds = if (selectedPhotoIds.contains(fileId)) {
                                    selectedPhotoIds - fileId
                                } else {
                                    selectedPhotoIds + fileId
                                }
                                if (selectedPhotoIds.isEmpty()) {
                                    isSelectionMode = false
                                }
                            }
                        },
                        onPhotoViewClick = { index ->
                            selectedPhotoIndex = index
                        },
                        onDeleteSelected = {
                            scope.launch {
                                if (acteurRepository == null || selectedPhotoIds.isEmpty()) return@launch
                                
                                isDeletingPhotos = true
                                errorMessage = null
                                
                                val acteurId = acteurRepository.getCurrentActeurId()
                                if (acteurId == null) {
                                    errorMessage = "Impossible de récupérer l'ID de l'acteur."
                                    isDeletingPhotos = false
                                    return@launch
                                }
                                
                                var successCount = 0
                                var failureCount = 0
                                
                                selectedPhotoIds.forEach { fileId ->
                                    try {
                                        val result = acteurRepository.deleteGalleryPhoto(acteurId, fileId)
                                        result.onSuccess {
                                            successCount++
                                            acteurProfile = it
                                            // Supprimer du cache
                                            GalleryPhotoCache.remove(context, fileId)
                                        }
                                        result.onFailure {
                                            failureCount++
                                            android.util.Log.e("ActorProfileScreen", "❌ Erreur suppression photo $fileId: ${it.message}")
                                        }
                                    } catch (e: Exception) {
                                        failureCount++
                                        android.util.Log.e("ActorProfileScreen", "❌ Exception suppression photo $fileId: ${e.message}", e)
                                    }
                                }
                                
                                if (successCount > 0) {
                                    successMessage = "$successCount photo(s) supprimée(s) avec succès!"
                                    if (failureCount > 0) {
                                        successMessage += " ($failureCount erreur(s))"
                                    }
                                } else {
                                    errorMessage = "Erreur lors de la suppression des photos."
                                }
                                
                                selectedPhotoIds = emptySet()
                                isSelectionMode = false
                                isDeletingPhotos = false
                            }
                        },
                        onCancelSelection = {
                            selectedPhotoIds = emptySet()
                            isSelectionMode = false
                        }
                    )
                    
                    // Informations personnelles - Section PROFILE
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "PROFILE",
                        fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                )
                
                    if (isEditing) {
                        // Mode édition - champs éditables
                        EditableField(
                            label = "Nom",
                    value = nom,
                            onValueChange = { nom = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Prénom",
                    value = prenom,
                            onValueChange = { prenom = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Email",
                    value = email,
                            onValueChange = { email = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Téléphone",
                    value = telephone,
                            onValueChange = { telephone = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Âge",
                    value = age,
                            onValueChange = { age = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Gouvernorat",
                    value = gouvernorat,
                            onValueChange = { gouvernorat = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "Années d'expérience",
                    value = experience,
                            onValueChange = { experience = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Liens sociaux en mode édition
                Text(
                    text = "Réseaux sociaux",
                            fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                
                        EditableField(
                            label = "Instagram",
                    value = instagram,
                            onValueChange = { instagram = it },
                            placeholder = "https://instagram.com/votre-compte"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "YouTube",
                    value = youtube,
                            onValueChange = { youtube = it },
                            placeholder = "https://youtube.com/votre-chaine"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        EditableField(
                            label = "TikTok",
                    value = tiktok,
                            onValueChange = { tiktok = it },
                            placeholder = "https://tiktok.com/@votre-compte"
                        )
                    } else {
                        // Mode affichage - ProfileInfoRow
                        ProfileInfoRow(
                            icon = Icons.Filled.Person,
                            label = "Nom",
                            value = nom.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.Person,
                            label = "Prénom",
                            value = prenom.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.Email,
                            label = "Email",
                            value = email.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.Phone,
                            label = "Téléphone",
                            value = telephone.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.Cake,
                            label = "Âge",
                            value = age.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.LocationOn,
                            label = "Gouvernorat",
                            value = gouvernorat.ifBlank { "Non renseigné" }
                        )
                        HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                        
                        ProfileInfoRow(
                            icon = Icons.Filled.Work,
                            label = "Années d'expérience",
                            value = experience.ifBlank { "Non renseigné" }
                        )
                    }
                
                // Section CV PDF
                Text(
                    text = "CV PDF",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                val documentFileId = acteurProfile?.media?.documentFileId
                val hasDocument = !documentFileId.isNullOrBlank()
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isEditing) {
                                Modifier.clickable {
                                    pdfPicker.launch("application/pdf")
                                }
                            } else if (hasDocument) {
                                Modifier.clickable {
                                    // Télécharger et ouvrir le PDF
                                    scope.launch {
                                        if (acteurRepository == null) return@launch
                                        isDownloadingDocument = true
                                        try {
                                            val documentResult = acteurRepository.downloadMedia(documentFileId!!)
                                            documentResult.onSuccess { bytes ->
                                                if (bytes != null && bytes.isNotEmpty()) {
                                                    // Sauvegarder le PDF temporairement et l'ouvrir
                                                    val pdfFile = File(context.cacheDir, "cv_${System.currentTimeMillis()}.pdf")
                                                    FileOutputStream(pdfFile).use { output ->
                                                        output.write(bytes)
                                                    }
                                                    
                                                    // Ouvrir le PDF avec un Intent
                                                    // Utiliser FileProvider pour partager le fichier de manière sécurisée
                                                    try {
                                                        // Utiliser FileProvider (obligatoire pour Android 7+)
                                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.fileprovider",
                                                            pdfFile
                                                        )
                                                        
                                                        android.util.Log.d("ActorProfileScreen", "✅ URI FileProvider créé: $uri")
                                                        
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                            setDataAndType(uri, "application/pdf")
                                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        }
                                                        
                                                        // Créer un chooser pour permettre à l'utilisateur de choisir l'application
                                                        val chooserIntent = android.content.Intent.createChooser(intent, "Ouvrir le CV avec")
                                                        chooserIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        
                                                        // Accorder les permissions de lecture à toutes les applications qui peuvent gérer l'Intent
                                                        // Cela est nécessaire pour que FileProvider fonctionne correctement avec createChooser
                                                        val resInfoList = context.packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                                                        for (resolveInfo in resInfoList) {
                                                            val packageName = resolveInfo.activityInfo.packageName
                                                            context.grantUriPermission(
                                                                packageName,
                                                                uri,
                                                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                            )
                                                        }
                                                        
                                                        context.startActivity(chooserIntent)
                                                        android.util.Log.d("ActorProfileScreen", "✅ PDF ouvert avec succès")
                                                    } catch (e: android.content.ActivityNotFoundException) {
                                                        android.util.Log.e("ActorProfileScreen", "❌ Aucune application pour ouvrir le PDF: ${e.message}")
                                                        errorMessage = "Aucune application pour ouvrir le PDF. Veuillez installer un lecteur PDF."
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("ActorProfileScreen", "❌ Impossible d'ouvrir le PDF: ${e.message}", e)
                                                        errorMessage = "Impossible d'ouvrir le PDF: ${e.message}"
                                                    }
                                                } else {
                                                    errorMessage = "Le fichier PDF est vide ou introuvable."
                                                }
                                                isDownloadingDocument = false
                                            }
                                            documentResult.onFailure { exception ->
                                                android.util.Log.e("ActorProfileScreen", "❌ Erreur téléchargement PDF: ${exception.message}")
                                                errorMessage = "Erreur lors du téléchargement du CV: ${getErrorMessage(exception)}"
                                                isDownloadingDocument = false
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("ActorProfileScreen", "❌ Exception téléchargement PDF: ${e.message}", e)
                                            errorMessage = "Erreur lors du téléchargement du CV: ${e.message}"
                                            isDownloadingDocument = false
                                        }
                                    }
                                }
                            } else {
                                Modifier
                            }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasDocument) LightBlue else LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                                imageVector = if (hasDocument) Icons.Default.Description else Icons.Default.UploadFile,
                                contentDescription = "CV PDF",
                                tint = DarkBlue
                    )
                            Column {
                    Text(
                                    text = if (hasDocument) "CV téléchargé" else if (isEditing) "Télécharger votre CV (PDF)" else "Aucun CV",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkBlue
                                )
                                if (hasDocument) {
                                    Text(
                                        text = "Fichier PDF disponible",
                                        fontSize = 12.sp,
                                        color = GrayBorder,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        if (isDownloadingDocument) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DarkBlue,
                                strokeWidth = 2.dp
                            )
                        } else if (hasDocument && !isEditing) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Voir le CV",
                                tint = DarkBlue
                            )
                        } else if (isEditing) {
                            Icon(
                                imageVector = if (hasDocument) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = if (hasDocument) "Modifier le CV" else "Ajouter un CV",
                                tint = DarkBlue
                            )
                        }
                        
                        if (selectedDocumentFile != null) {
                            Text(
                                text = "Nouveau fichier sélectionné",
                                fontSize = 12.sp,
                                color = DarkBlue,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                // Espace en bas pour le scroll
                Spacer(modifier = Modifier.height(90.dp))
                }
            }
            
            // Barre de navigation positionnée au-dessus du contenu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 17.dp)
            ) {
                ActorBottomNavigationBar(
                    selectedItem = NavigationItem.PROFILE,
                    onCandidaturesClick = onMyCandidaturesClick,
                onHomeClick = onHomeClick,
                    onProfileClick = onProfileClick
            )
            }
        }
    }
    
    // Alerte Coming Soon
    showComingSoon?.let { feature ->
        ComingSoonAlert(
            onDismiss = { showComingSoon = null },
            featureName = feature
        )
    }
    
    // Vue plein écran pour la galerie
    selectedPhotoIndex?.let { index ->
        if (galleryPhotos.isNotEmpty() && index in galleryPhotos.indices) {
            android.util.Log.d("ActorProfileScreen", "🖼️ Ouverture vue plein écran: index=$index, totalPhotos=${galleryPhotos.size}")
            FullScreenGalleryViewer(
                photos = galleryPhotos,
                initialIndex = index,
                onDismiss = { 
                    android.util.Log.d("ActorProfileScreen", "🖼️ Fermeture vue plein écran")
                    selectedPhotoIndex = null 
                }
            )
        } else {
            android.util.Log.e("ActorProfileScreen", "❌ Index invalide: index=$index, galleryPhotos.size=${galleryPhotos.size}")
        }
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
            mimeType?.contains("pdf") == true -> ".pdf"
            mimeType?.contains("application/pdf") == true -> ".pdf"
            else -> ".tmp"
        }

        val inputStream: InputStream = resolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "$prefix-${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        android.util.Log.d("ActorProfileScreen", "✅ Fichier copié vers: ${file.absolutePath}, taille: ${file.length()} bytes")
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

@Composable
private fun SocialMediaIcon(
    painter: androidx.compose.ui.graphics.painter.Painter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .clickable(onClick = onClick)
            .border(1.dp, GrayBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SocialMediaIconWithVector(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White)
            .clickable(onClick = onClick)
            .border(1.dp, GrayBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = DarkBlue,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = GrayBorder
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF1A1A1A),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
    maxLines: Int = 1,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = LightGray) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DarkBlue,
            unfocusedBorderColor = GrayBorder,
            cursorColor = DarkBlue,
            focusedLabelColor = DarkBlue
        ),
        minLines = minLines,
        maxLines = maxLines,
        singleLine = minLines == 1 && maxLines == 1,
        shape = RoundedCornerShape(12.dp)
    )
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

/**
 * Composable pour afficher la section galerie de photos
 */
@Composable
private fun GallerySection(
    galleryPhotos: List<Pair<String, ImageBitmap>>,
    isLoadingGallery: Boolean,
    isUploadingGalleryPhotos: Boolean,
    isSelectionMode: Boolean,
    selectedPhotoIds: Set<String>,
    isDeletingPhotos: Boolean,
    onAddPhotosClick: () -> Unit,
    onPhotoLongPress: (String) -> Unit,
    onPhotoClick: (String) -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    onPhotoViewClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Galerie de photos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            // Boutons d'action selon le mode
            if (isSelectionMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton annuler
                    TextButton(
                        onClick = onCancelSelection,
                        enabled = !isDeletingPhotos
                    ) {
                        Text(
                            text = "Annuler",
                            fontSize = 14.sp,
                            color = GrayBorder
                        )
                    }
                    
                    // Bouton supprimer
                    Button(
                        onClick = onDeleteSelected,
                        enabled = !isDeletingPhotos && selectedPhotoIds.isNotEmpty(),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (isDeletingPhotos) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                modifier = Modifier.size(18.dp),
                                tint = White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Supprimer (${selectedPhotoIds.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Bouton pour ajouter des photos
                Button(
                    onClick = onAddPhotosClick,
                    enabled = !isUploadingGalleryPhotos,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isUploadingGalleryPhotos) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter des photos",
                            modifier = Modifier.size(18.dp),
                            tint = White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ajouter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoadingGallery) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkBlue)
            }
        } else if (galleryPhotos.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = GrayBorder
                    )
                    Text(
                        text = "Aucune photo dans la galerie",
                        color = GrayBorder,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Cliquez sur 'Ajouter' pour publier vos photos",
                        color = GrayBorder.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(galleryPhotos.size) { index ->
                    val (fileId, bitmap) = galleryPhotos[index]
                    val isSelected = selectedPhotoIds.contains(fileId)
                    GalleryPhotoItem(
                        fileId = fileId,
                        bitmap = bitmap,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onLongPress = { onPhotoLongPress(fileId) },
                        onClick = {
                            if (isSelectionMode) {
                                onPhotoClick(fileId)
                            } else {
                                onPhotoViewClick(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Composable pour afficher un élément de photo dans la galerie avec support de sélection
 */
@Composable
private fun GalleryPhotoItem(
    fileId: String,
    bitmap: ImageBitmap,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = DarkBlue,
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 2.dp
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Photo galerie",
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isSelected || isSelectionMode) {
                                Modifier.background(
                                    color = if (isSelected) DarkBlue.copy(alpha = 0.3f) else Color.Transparent
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
                
                // Indicateur de sélection
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) DarkBlue else White.copy(alpha = 0.7f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) White else GrayBorder,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sélectionné",
                                    tint = White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable pour afficher une photo de la galerie en plein écran avec navigation par swipe
 */
@Composable
private fun FullScreenGalleryViewer(
    photos: List<Pair<String, ImageBitmap>>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var offsetX by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Mettre à jour l'index initial si nécessaire
    LaunchedEffect(initialIndex) {
        currentIndex = initialIndex
        android.util.Log.d("FullScreenGalleryViewer", "📸 Initialisé avec index: $initialIndex, photos: ${photos.size}")
    }
    
    // Log quand l'index change
    LaunchedEffect(currentIndex) {
        android.util.Log.d("FullScreenGalleryViewer", "📸 Index changé: $currentIndex")
        offsetX = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() } // Fermer en cliquant sur le fond
    ) {
        // Navigation par swipe horizontal - Approche simplifiée avec une seule photo visible
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = with(LocalDensity.current) { maxWidth.toPx() }
            
            // Zone de swipe
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenWidth) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                isDragging = false
                                // Quand le drag se termine, changer de photo si le déplacement est suffisant
                                val threshold = screenWidth * 0.25f
                                android.util.Log.d("FullScreenGalleryViewer", "🔄 Drag terminé: offsetX=$offsetX, threshold=$threshold, currentIndex=$currentIndex")
                                when {
                                    offsetX > threshold && currentIndex > 0 -> {
                                        currentIndex--
                                        android.util.Log.d("FullScreenGalleryViewer", "⬅️ Photo précédente: $currentIndex")
                                    }
                                    offsetX < -threshold && currentIndex < photos.size - 1 -> {
                                        currentIndex++
                                        android.util.Log.d("FullScreenGalleryViewer", "➡️ Photo suivante: $currentIndex")
                                    }
                                }
                                offsetX = 0f
                            }
                        ) { change, dragAmount ->
                            if (!isDragging) {
                                isDragging = true
                            }
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            // Limiter le déplacement selon la position
                            when {
                                currentIndex == 0 && newOffset > 0 -> {
                                    // Réduire le déplacement à droite si on est à la première photo
                                    offsetX = newOffset * 0.3f
                                }
                                currentIndex == photos.size - 1 && newOffset < 0 -> {
                                    // Réduire le déplacement à gauche si on est à la dernière photo
                                    offsetX = newOffset * 0.3f
                                }
                                else -> {
                                    offsetX = newOffset
                                }
                            }
                        }
                    }
            )
            
            // Afficher la photo actuelle avec transition
            val density = LocalDensity.current
            val currentPhoto = photos.getOrNull(currentIndex)
            
            android.util.Log.d("FullScreenGalleryViewer", "📐 screenWidth=$screenWidth, currentIndex=$currentIndex, offsetX=$offsetX, photosCount=${photos.size}")
            
            if (currentPhoto != null) {
                val (fileId, bitmap) = currentPhoto
                android.util.Log.d("FullScreenGalleryViewer", "🖼️ Affichage photo actuelle $currentIndex: fileId=$fileId, bitmap=${bitmap.width}x${bitmap.height}")
                
                // Photo actuelle
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = with(density) { offsetX.toDp() })
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Photo galerie ${currentIndex + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Photo suivante (si elle existe et qu'on swipe vers la gauche)
                if (currentIndex < photos.size - 1 && offsetX < 0) {
                    val nextPhoto = photos[currentIndex + 1]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = with(density) { (screenWidth + offsetX).toDp() })
                    ) {
                        Image(
                            bitmap = nextPhoto.second,
                            contentDescription = "Photo suivante",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Photo précédente (si elle existe et qu'on swipe vers la droite)
                if (currentIndex > 0 && offsetX > 0) {
                    val prevPhoto = photos[currentIndex - 1]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = with(density) { (-screenWidth + offsetX).toDp() })
                    ) {
                        Image(
                            bitmap = prevPhoto.second,
                            contentDescription = "Photo précédente",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
        
        // Bouton de fermeture en haut à droite
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Fermer",
                tint = White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Indicateur de position (en bas)
        if (photos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                photos.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) White else White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
    
    // Réinitialiser l'offset quand l'index change
    LaunchedEffect(currentIndex) {
        offsetX = 0f
    }
}

