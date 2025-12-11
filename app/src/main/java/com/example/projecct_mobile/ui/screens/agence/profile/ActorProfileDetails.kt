package com.example.projecct_mobile.ui.screens.agence.profile

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.R
import com.example.projecct_mobile.data.cache.GalleryPhotoCache
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.model.UserRole
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.components.ErrorMessage
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import android.content.Intent

/**
 * Page de profil acteur en mode lecture seule pour les agences
 * Suit la même structure UI que ActorProfileScreen
 */
@Composable
fun ActorProfileDetails(
    acteurId: String? = null,
    onBackClick: () -> Unit = {},
    onNavigateToCastings: () -> Unit = {},
    onNavigateToCreateCasting: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    loadData: Boolean = true,
    initialUser: User? = null
) {
    var acteurProfile by remember { mutableStateOf<ActeurProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Variables pour les informations de l'acteur
    var nom by remember { mutableStateOf(initialUser?.nom ?: "") }
    var prenom by remember { mutableStateOf(initialUser?.prenom ?: "") }
    var email by remember { mutableStateOf(initialUser?.email ?: "") }
    var telephone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gouvernorat by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }

    // États pour les médias de l'acteur
    var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isDownloadingPhoto by remember { mutableStateOf(false) }
    var lastDownloadedPhotoFileId by remember { mutableStateOf<String?>(null) }

    var isDownloadingDocument by remember { mutableStateOf(false) }
    var lastDownloadedDocumentFileId by remember { mutableStateOf<String?>(null) }
    
    // Galerie de photos
    var galleryPhotos by remember { mutableStateOf<List<Pair<String, ImageBitmap>>>(emptyList()) }
    var isDownloadingGallery by remember { mutableStateOf(false) }
    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val acteurRepository = remember(loadData) { if (loadData) ActeurRepository() else null }

    // Charger le profil de l'acteur avec l'ID fourni
    LaunchedEffect(acteurId, loadData) {
        if (!loadData || acteurId.isNullOrBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        errorMessage = null
        
        try {
            android.util.Log.d("ActorProfileDetails", "📞 Chargement du profil acteur ID: $acteurId")
            val result = acteurRepository?.getActeurById(acteurId)
            
            result?.onSuccess { profile ->
                android.util.Log.d("ActorProfileDetails", "✅ Profil acteur chargé: ${profile.nom} ${profile.prenom}")
                acteurProfile = profile
                
                // Mettre à jour les champs avec les données du profil
                nom = profile.nom ?: ""
                prenom = profile.prenom ?: ""
                email = profile.email ?: ""
                telephone = profile.tel ?: ""
                age = profile.age?.toString() ?: ""
                gouvernorat = profile.gouvernorat ?: ""
                experience = profile.experience?.toString() ?: ""
                instagram = profile.socialLinks?.instagram ?: ""
                youtube = profile.socialLinks?.youtube ?: ""
                tiktok = profile.socialLinks?.tiktok ?: ""
                
                isLoading = false
            }
            
            result?.onFailure { exception ->
                android.util.Log.e("ActorProfileDetails", "❌ Erreur chargement profil acteur: ${exception.message}", exception)
                errorMessage = "Erreur lors du chargement du profil: ${getErrorMessage(exception)}"
                isLoading = false
            }
            
            if (result == null) {
                errorMessage = "Impossible de charger le profil"
                isLoading = false
            }
        } catch (e: Exception) {
            android.util.Log.e("ActorProfileDetails", "❌ Exception chargement: ${e.message}", e)
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }

    // Télécharger la photo de profil quand acteurProfile est disponible
    LaunchedEffect(acteurProfile?.media?.photoFileId) {
        val photoFileId = acteurProfile?.media?.photoFileId
        android.util.Log.d("ActorProfileDetails", "🔵 LaunchedEffect(photoFileId) déclenché: '$photoFileId', dernier téléchargé: '$lastDownloadedPhotoFileId'")

        if (!photoFileId.isNullOrBlank() && photoFileId != lastDownloadedPhotoFileId) {
            try {
                isDownloadingPhoto = true
                android.util.Log.d("ActorProfileDetails", "🚀 Téléchargement de la photo: $photoFileId")
                val mediaResult = acteurRepository?.downloadMedia(photoFileId)

                if (mediaResult?.isSuccess == true) {
                    val bytes = mediaResult.getOrNull()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                        if (bitmap != null) {
                            profileImage = bitmap.asImageBitmap()
                            lastDownloadedPhotoFileId = photoFileId
                            android.util.Log.d("ActorProfileDetails", "✅ Photo de profil chargée avec succès!")
                        }
                    }
                } else {
                    val exception = mediaResult?.exceptionOrNull()
                    android.util.Log.e("ActorProfileDetails", "❌ Erreur téléchargement photo: ${exception?.message}", exception)
                }
            } catch (e: CancellationException) {
                android.util.Log.d("ActorProfileDetails", "⚠️ Téléchargement photo annulé")
            } catch (e: Exception) {
                android.util.Log.e("ActorProfileDetails", "❌ Exception téléchargement photo: ${e.message}", e)
            } finally {
                isDownloadingPhoto = false
            }
        }
    }
    
    // Télécharger la galerie de photos
    LaunchedEffect(acteurProfile?.media?.gallery) {
        val gallery = acteurProfile?.media?.gallery
        if (gallery.isNullOrEmpty()) {
            galleryPhotos = emptyList()
            return@LaunchedEffect
        }
        
        try {
            isDownloadingGallery = true
            val downloadedPhotos = mutableListOf<Pair<String, ImageBitmap>>()
            
            gallery.forEach { mediaRef ->
                val fileId = mediaRef.fileId
                if (!fileId.isNullOrBlank()) {
                    // Vérifier le cache d'abord
                    val cachedBitmap = GalleryPhotoCache.get(context, fileId)
                    if (cachedBitmap != null) {
                        downloadedPhotos.add(Pair(fileId, cachedBitmap))
                    } else {
                        val mediaResult = acteurRepository?.downloadMedia(fileId)
                        if (mediaResult?.isSuccess == true) {
                            val bytes = mediaResult.getOrNull()
                            if (bytes != null && bytes.isNotEmpty()) {
                                // Utiliser GalleryPhotoCache.put qui accepte ByteArray et retourne ImageBitmap
                                val imageBitmap = GalleryPhotoCache.put(context, fileId, bytes)
                                if (imageBitmap != null) {
                                    downloadedPhotos.add(Pair(fileId, imageBitmap))
                                }
                            }
                        }
                    }
                }
            }
            
            galleryPhotos = downloadedPhotos
            isDownloadingGallery = false
        } catch (e: Exception) {
            android.util.Log.e("ActorProfileDetails", "❌ Erreur téléchargement galerie: ${e.message}", e)
            isDownloadingGallery = false
        }
    }

    // Statistiques (placeholder)
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
                                }
                                isDownloadingPhoto -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = DarkBlue,
                                        strokeWidth = 3.dp
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(
                        message = errorMessage ?: "Erreur",
                        onRetry = {
                            scope.launch {
                                if (acteurRepository == null || acteurId.isNullOrBlank()) return@launch
                                isLoading = true
                                errorMessage = null
                                try {
                                    val result = acteurRepository.getActeurById(acteurId)
                                    result.onSuccess { profile ->
                                        acteurProfile = profile
                                        nom = profile.nom ?: ""
                                        prenom = profile.prenom ?: ""
                                        email = profile.email ?: ""
                                        telephone = profile.tel ?: ""
                                        age = profile.age?.toString() ?: ""
                                        gouvernorat = profile.gouvernorat ?: ""
                                        experience = profile.experience?.toString() ?: ""
                                        instagram = profile.socialLinks?.instagram ?: ""
                                        youtube = profile.socialLinks?.youtube ?: ""
                                        tiktok = profile.socialLinks?.tiktok ?: ""
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
                    // Section Galerie de photos
                    GallerySection(
                        galleryPhotos = galleryPhotos,
                        isLoadingGallery = isDownloadingGallery,
                        onPhotoViewClick = { index ->
                            selectedPhotoIndex = index
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
                
                    // Section Réseaux sociaux
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Réseaux sociaux",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Fonction helper pour convertir un nom d'utilisateur ou URL en URL complète
                    fun String?.isValidSocialLink(): Boolean {
                        return !this.isNullOrBlank() && this.lowercase() != "null"
                    }

                    val instagramValue = instagram.takeIf { it.isValidSocialLink() }
                        ?: acteurProfile?.socialLinks?.instagram?.takeIf { it.isValidSocialLink() }
                    val youtubeValue = youtube.takeIf { it.isValidSocialLink() }
                        ?: acteurProfile?.socialLinks?.youtube?.takeIf { it.isValidSocialLink() }
                    val tiktokValue = tiktok.takeIf { it.isValidSocialLink() }
                        ?: acteurProfile?.socialLinks?.tiktok?.takeIf { it.isValidSocialLink() }

                    fun getInstagramUrl(input: String?): String? {
                        if (input.isNullOrBlank()) return null
                        val trimmed = input.trim()
                        return when {
                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                            trimmed.startsWith("instagram.com/") -> "https://$trimmed"
                            trimmed.startsWith("www.instagram.com/") -> "https://$trimmed"
                            else -> {
                                val username = trimmed.replace(" ", "-").lowercase()
                                "https://www.instagram.com/$username"
                            }
                        }
                    }

                    val finalInstagramUrl = getInstagramUrl(instagramValue)
                    val finalYoutubeUrl = youtubeValue?.let { 
                        val trimmed = it.trim()
                        when {
                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                            trimmed.startsWith("youtube.com/") || trimmed.startsWith("youtu.be/") -> "https://$trimmed"
                            else -> "https://www.youtube.com/$trimmed"
                        }
                    }
                    val finalTiktokUrl = tiktokValue?.let {
                        val trimmed = it.trim()
                        when {
                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                            trimmed.startsWith("tiktok.com/") -> "https://$trimmed"
                            else -> "https://www.tiktok.com/@$trimmed"
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (instagramValue != null) {
                            SocialMediaIcon(
                                painter = painterResource(id = R.drawable.instagram),
                                onClick = {
                                    try {
                                        val url = finalInstagramUrl ?: "https://www.instagram.com"
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileDetails", "Erreur ouverture Instagram: ${e.message}", e)
                                    }
                                }
                            )
                        }
                        if (youtubeValue != null) {
                            SocialMediaIconWithVector(
                                imageVector = Icons.Default.PlayCircle,
                                onClick = {
                                    try {
                                        val url = finalYoutubeUrl ?: "https://www.youtube.com"
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileDetails", "Erreur ouverture YouTube: ${e.message}", e)
                                    }
                                }
                            )
                        }
                        if (tiktokValue != null) {
                            SocialMediaIconWithVector(
                                imageVector = Icons.Default.MusicNote,
                                onClick = {
                                    try {
                                        val url = finalTiktokUrl ?: "https://www.tiktok.com"
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("ActorProfileDetails", "Erreur ouverture TikTok: ${e.message}", e)
                                    }
                                }
                            )
                        }
                    }
                
                    // Section CV PDF
                    Spacer(modifier = Modifier.height(24.dp))
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
                                if (hasDocument) {
                                    Modifier.clickable {
                                        scope.launch {
                                            if (acteurRepository == null || documentFileId == null) return@launch
                                            isDownloadingDocument = true
                                            try {
                                                val documentResult = acteurRepository.downloadMedia(documentFileId)
                                                documentResult.onSuccess { bytes ->
                                                    if (bytes != null && bytes.isNotEmpty()) {
                                                        val pdfFile = File(context.cacheDir, "cv_${System.currentTimeMillis()}.pdf")
                                                        FileOutputStream(pdfFile).use { output ->
                                                            output.write(bytes)
                                                        }
                                                        
                                                        try {
                                                            val uri = FileProvider.getUriForFile(
                                                                context,
                                                                "${context.packageName}.fileprovider",
                                                                pdfFile
                                                            )
                                                            
                                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                                setDataAndType(uri, "application/pdf")
                                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            }
                                                            
                                                            val chooserIntent = Intent.createChooser(intent, "Ouvrir le CV avec")
                                                            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                            
                                                            val resInfoList = context.packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                                                            for (resolveInfo in resInfoList) {
                                                                val packageName = resolveInfo.activityInfo.packageName
                                                                context.grantUriPermission(
                                                                    packageName,
                                                                    uri,
                                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                                )
                                                            }
                                                            
                                                            context.startActivity(chooserIntent)
                                                            android.util.Log.d("ActorProfileDetails", "✅ PDF ouvert avec succès")
                                                        } catch (e: android.content.ActivityNotFoundException) {
                                                            android.util.Log.e("ActorProfileDetails", "❌ Aucune application pour ouvrir le PDF: ${e.message}", e)
                                                            errorMessage = "Aucune application pour ouvrir le PDF. Veuillez installer un lecteur PDF."
                                                        } catch (e: Exception) {
                                                            android.util.Log.e("ActorProfileDetails", "❌ Impossible d'ouvrir le PDF: ${e.message}", e)
                                                            errorMessage = "Impossible d'ouvrir le PDF: ${e.message}"
                                                        }
                                                    } else {
                                                        errorMessage = "Le fichier PDF est vide ou introuvable."
                                                    }
                                                    isDownloadingDocument = false
                                                }
                                                documentResult.onFailure { exception ->
                                                    android.util.Log.e("ActorProfileDetails", "❌ Erreur téléchargement PDF: ${exception.message}")
                                                    errorMessage = "Erreur lors du téléchargement du CV: ${getErrorMessage(exception)}"
                                                    isDownloadingDocument = false
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("ActorProfileDetails", "❌ Exception téléchargement PDF: ${e.message}", e)
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
                                        text = if (hasDocument) "CV téléchargé" else "Aucun CV",
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
                            } else if (hasDocument) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Voir le CV",
                                    tint = DarkBlue
                                )
                            }
                        }
                    }
                    
                    // Espace en bas pour le scroll
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
    
    // Vue plein écran pour la galerie
    selectedPhotoIndex?.let { index ->
        if (galleryPhotos.isNotEmpty() && index in galleryPhotos.indices) {
            android.util.Log.d("ActorProfileDetails", "🖼️ Ouverture vue plein écran: index=$index, totalPhotos=${galleryPhotos.size}")
            FullScreenGalleryViewer(
                photos = galleryPhotos,
                initialIndex = index,
                onDismiss = { 
                    android.util.Log.d("ActorProfileDetails", "🖼️ Fermeture vue plein écran")
                    selectedPhotoIndex = null 
                }
            )
        } else {
            android.util.Log.e("ActorProfileDetails", "❌ Index invalide: index=$index, galleryPhotos.size=${galleryPhotos.size}")
        }
    }
}

/**
 * Composable pour afficher la section galerie de photos (lecture seule)
 */
@Composable
private fun GallerySection(
    galleryPhotos: List<Pair<String, ImageBitmap>>,
    isLoadingGallery: Boolean,
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
                itemsIndexed(galleryPhotos) { index, (fileId, bitmap) ->
                    GalleryPhotoItem(
                        fileId = fileId,
                        bitmap = bitmap,
                        onClick = {
                            onPhotoViewClick(index)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Composable pour afficher un élément de photo dans la galerie (lecture seule)
 */
@Composable
private fun GalleryPhotoItem(
    fileId: String,
    bitmap: ImageBitmap,
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
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Photo galerie",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
    
    LaunchedEffect(initialIndex) {
        currentIndex = initialIndex
        android.util.Log.d("FullScreenGalleryViewer", "📸 Initialisé avec index: $initialIndex, photos: ${photos.size}")
    }
    
    LaunchedEffect(currentIndex) {
        android.util.Log.d("FullScreenGalleryViewer", "📸 Index changé: $currentIndex")
        offsetX = 0f
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() }
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = with(LocalDensity.current) { maxWidth.toPx() }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(screenWidth) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                isDragging = false
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
                            when {
                                currentIndex == 0 && newOffset > 0 -> {
                                    offsetX = newOffset * 0.3f
                                }
                                currentIndex == photos.size - 1 && newOffset < 0 -> {
                                    offsetX = newOffset * 0.3f
                                }
                                else -> {
                                    offsetX = newOffset
                                }
                            }
                        }
                    }
            )
            
            val density = LocalDensity.current
            val currentPhoto = photos.getOrNull(currentIndex)
            
            if (currentPhoto != null) {
                val (fileId, bitmap) = currentPhoto
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = with(density) { offsetX.toDp() })
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Photo galerie",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            // Indicateur de position
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
                                .size(if (index == currentIndex) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentIndex) White else White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
fun ActorProfileDetailsPreview() {
    Projecct_MobileTheme {
        ActorProfileDetails(
            acteurId = "actor_1",
            loadData = false,
            initialUser = User(
                id = "actor_1",
                nom = "Doe",
                prenom = "John",
                email = "actor@example.com",
                role = UserRole.ACTEUR,
                bio = "Acteur professionnel avec 5 ans d'expérience."
            )
        )
    }
}
