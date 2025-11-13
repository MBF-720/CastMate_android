package com.example.projecct_mobile.ui.screens.agence.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.AgenceProfile
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.model.UserRole
import com.example.projecct_mobile.data.repository.AgenceRepository
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.components.getErrorMessage
import com.example.projecct_mobile.R
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.LightBlue
import com.example.projecct_mobile.ui.theme.LightGray
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun AgencyProfileScreen(
    onBackClick: () -> Unit = {},
    onNavigateToCastings: () -> Unit = {},
    onNavigateToCreateCasting: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    loadData: Boolean = true,
    initialUser: User? = null
) {
    var user by remember { mutableStateOf(initialUser) }
    var agenceProfile by remember { mutableStateOf<AgenceProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var formMessage by remember { mutableStateOf<String?>(null) }

    var agencyName by remember { mutableStateOf(initialUser?.nom ?: "") }
    var responsableName by remember { mutableStateOf(initialUser?.prenom ?: "") }
    var agencyEmail by remember { mutableStateOf(initialUser?.email ?: "") }
    var agencyPhone by remember { mutableStateOf("") }
    var agencyDescription by remember { mutableStateOf(initialUser?.bio ?: "") }
    var agencySiteWeb by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    
    // États pour les médias
    var logoImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedLogoFile by remember { mutableStateOf<File?>(null) }
    var isDownloadingLogo by remember { mutableStateOf(false) }
    var lastDownloadedLogoFileId by remember { mutableStateOf<String?>(null) }
    var isUploadingLogo by remember { mutableStateOf(false) }
    
    var selectedDocumentFile by remember { mutableStateOf<File?>(null) }
    var isDownloadingDocument by remember { mutableStateOf(false) }
    var lastDownloadedDocumentFileId by remember { mutableStateOf<String?>(null) }
    var isUploadingDocument by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // File pickers
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val copiedFile = copyUriToCache(context, uri, "agency_logo")
        if (copiedFile != null) {
            selectedLogoFile = copiedFile
            val bitmap = BitmapFactory.decodeFile(copiedFile.absolutePath)
            if (bitmap != null) {
                logoImage = bitmap.asImageBitmap()
                android.util.Log.d("AgencyProfileScreen", "✅ Nouveau logo sélectionné: ${copiedFile.name}")
            }
        }
    }
    
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val copiedFile = copyUriToCache(context, uri, "agency_document", ".pdf")
        if (copiedFile != null) {
            selectedDocumentFile = copiedFile
            android.util.Log.d("AgencyProfileScreen", "✅ Nouveau document sélectionné: ${copiedFile.name}")
        }
    }

    val agenceRepository = remember(loadData) { if (loadData) AgenceRepository() else null }
    val userRepository = remember(loadData) { if (loadData) UserRepository() else null }
    val tokenManager = remember(loadData, context) { if (loadData) TokenManager(context) else null }

    LaunchedEffect(loadData) {
        if (!loadData) return@LaunchedEffect
        val tokenManagerLocal = tokenManager
        val storedName = tokenManagerLocal?.getUserNomSync()
        val storedResponsable = tokenManagerLocal?.getUserResponsableSync()
        val storedEmail = tokenManagerLocal?.getUserEmailSync()
        val storedPhone = tokenManagerLocal?.getUserPhoneSync()
        val storedDescription = tokenManagerLocal?.getUserDescriptionSync()
        agencyName = storedName ?: agencyName
        responsableName = storedResponsable ?: responsableName
        agencyEmail = storedEmail ?: agencyEmail
        agencyPhone = storedPhone ?: agencyPhone
        agencyDescription = storedDescription ?: agencyDescription
        if (agencyEmail.isBlank()) {
            agencyEmail = storedEmail ?: ""
        }
        if (!storedEmail.isNullOrBlank()) {
            val cached = tokenManagerLocal?.getAgencyProfileCache(storedEmail)
            cached?.nom?.let { agencyName = it }
            cached?.responsable?.let { responsableName = it }
            cached?.phone?.let { agencyPhone = it }
            cached?.description?.let { agencyDescription = it }
        }
    }

    LaunchedEffect(user) {
        if (!loadData || isEditing) return@LaunchedEffect
        user?.let { currentUser ->
            agencyName = currentUser.nom ?: agencyName
            responsableName = currentUser.prenom ?: responsableName
            agencyEmail = currentUser.email
            agencyDescription = currentUser.bio ?: agencyDescription
        }
    }

    // Charger le profil agence avec les médias
    LaunchedEffect(loadData) {
        if (!loadData) return@LaunchedEffect
        isLoading = true
        errorMessage = null
        try {
            val result = agenceRepository?.getCurrentAgence()
            result?.onSuccess { profile ->
                agenceProfile = profile
                agencyName = profile.nomAgence ?: agencyName
                responsableName = profile.responsable ?: responsableName
                agencyEmail = profile.email ?: agencyEmail
                agencyPhone = profile.tel ?: agencyPhone
                agencyDescription = profile.description ?: agencyDescription
                agencySiteWeb = profile.siteWeb ?: ""
                
                // Log pour vérifier les socialLinks
                android.util.Log.d("AgencyProfileScreen", "📥 Profil chargé - socialLinks: ${profile.socialLinks}")
                android.util.Log.d("AgencyProfileScreen", "📥 Facebook depuis profil: '${profile.socialLinks?.facebook}'")
                android.util.Log.d("AgencyProfileScreen", "📥 Instagram depuis profil: '${profile.socialLinks?.instagram}'")
                
                facebook = profile.socialLinks?.facebook ?: ""
                instagram = profile.socialLinks?.instagram ?: ""
                
                android.util.Log.d("AgencyProfileScreen", "📥 Facebook variable: '$facebook'")
                android.util.Log.d("AgencyProfileScreen", "📥 Instagram variable: '$instagram'")
                
                // Créer un User pour compatibilité
                user = User(
                    id = profile.actualId,
                    nom = profile.nomAgence,
                    prenom = profile.responsable,
                    email = profile.email ?: "",
                    role = UserRole.RECRUTEUR,
                    bio = profile.description
                )
                isLoading = false
            }
            result?.onFailure { exception ->
                android.util.Log.e("AgencyProfileScreen", "❌ Erreur chargement profil agence: ${exception.message}", exception)
                // Fallback vers UserRepository si AgenceRepository échoue
                val userResult = userRepository?.getCurrentUser()
                userResult?.onSuccess {
                    user = it
                    agencyName = it.nom ?: agencyName
                    responsableName = it.prenom ?: responsableName
                    agencyEmail = it.email
                    agencyDescription = it.bio ?: agencyDescription
                    isLoading = false
                }
                userResult?.onFailure {
                    if (exception is ApiException.NotFoundException) {
                        try {
                            val tokenManagerLocal = tokenManager
                            val fallbackEmail = tokenManagerLocal?.getUserEmailSync()
                            val storedName = tokenManagerLocal?.getUserNomSync()
                            val storedResponsable = tokenManagerLocal?.getUserResponsableSync()
                            val storedRole = tokenManagerLocal?.getUserRoleSync()
                            val storedDescription = tokenManagerLocal?.getUserDescriptionSync()
                            user = User(
                                id = tokenManagerLocal?.getUserIdSync(),
                                nom = storedName ?: "Agence",
                                prenom = storedResponsable,
                                email = fallbackEmail ?: "",
                                role = storedRole?.let { role ->
                                    runCatching { UserRole.valueOf(role.uppercase()) }.getOrNull()
                                },
                                bio = storedDescription
                            )
                            agencyName = storedName ?: "Agence"
                            responsableName = storedResponsable ?: ""
                            agencyEmail = fallbackEmail ?: ""
                            agencyPhone = tokenManagerLocal?.getUserPhoneSync() ?: ""
                            agencyDescription = storedDescription ?: ""
                            isLoading = false
                        } catch (e: Exception) {
                            errorMessage = "Profil indisponible"
                            isLoading = false
                        }
                    } else {
                        errorMessage = "Erreur lors du chargement: ${exception.message}"
                        isLoading = false
                    }
                }
                if (userResult == null) {
                    isLoading = false
                }
            }
            if (result == null) {
                isLoading = false
            }
        } catch (e: Exception) {
            android.util.Log.e("AgencyProfileScreen", "❌ Exception chargement: ${e.message}", e)
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }
    
    // Télécharger le logo quand agenceProfile est disponible
    LaunchedEffect(agenceProfile?.media?.photoFileId) {
        val logoFileId = agenceProfile?.media?.photoFileId
        android.util.Log.d("AgencyProfileScreen", "🔵 LaunchedEffect(logoFileId) déclenché: '$logoFileId', dernier téléchargé: '$lastDownloadedLogoFileId'")
        
        if (!logoFileId.isNullOrBlank() && logoFileId != lastDownloadedLogoFileId && selectedLogoFile == null) {
            try {
                isDownloadingLogo = true
                android.util.Log.d("AgencyProfileScreen", "🚀 Téléchargement du logo: $logoFileId")
                val mediaResult = agenceRepository?.downloadMedia(logoFileId)
                
                if (mediaResult?.isSuccess == true) {
                    val bytes = mediaResult.getOrNull()
                    if (bytes != null && bytes.isNotEmpty()) {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                        if (bitmap != null) {
                            logoImage = bitmap.asImageBitmap()
                            lastDownloadedLogoFileId = logoFileId
                            android.util.Log.d("AgencyProfileScreen", "✅ Logo chargé avec succès!")
                        }
                    }
                } else {
                    val exception = mediaResult?.exceptionOrNull()
                    android.util.Log.e("AgencyProfileScreen", "❌ Erreur téléchargement logo: ${exception?.message}", exception)
                }
            } catch (e: CancellationException) {
                android.util.Log.d("AgencyProfileScreen", "⚠️ Téléchargement logo annulé")
            } catch (e: Exception) {
                android.util.Log.e("AgencyProfileScreen", "❌ Exception téléchargement logo: ${e.message}", e)
            } finally {
                isDownloadingLogo = false
            }
        }
    }

    val displayName = agencyName.ifBlank { "Agence" }
    val displayEmail = agencyEmail.ifBlank { "contact@agence.com" }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header avec grand cercle pour la photo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color(0xFF1A1A1A)
                    )
                }

                Text(
                    text = "Profil Agence",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Icon(
                    imageVector = Icons.Filled.Business,
                    contentDescription = "Agence",
                    tint = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(DarkBlue)
                        .then(
                            if (isEditing) {
                                Modifier.clickable {
                                    logoPicker.launch("image/*")
                                }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        logoImage != null -> {
                            Image(
                                bitmap = logoImage!!,
                                contentDescription = "Logo agence",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (isEditing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonAdd,
                                        contentDescription = "Modifier le logo",
                                        tint = White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        isDownloadingLogo -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = White,
                                strokeWidth = 3.dp
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = "Logo agence",
                                tint = White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = displayEmail,
                    fontSize = 14.sp,
                    color = GrayBorder
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkBlue)
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "Erreur",
                                color = Red,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Button(onClick = {
                                scope.launch {
                                    if (userRepository == null) return@launch
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val result = userRepository.getCurrentUser()
                                        isLoading = false
                                        result.onSuccess {
                                            user = it
                                            agencyName = it.nom ?: agencyName
                                            responsableName = it.prenom ?: responsableName
                                            agencyEmail = it.email
                                            agencyDescription = it.bio ?: agencyDescription
                                        }
                                        result.onFailure { exception ->
                                            errorMessage = "Erreur: ${exception.message}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Section PROFILE
                        Column {
                            Text(
                                text = "PROFILE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            if (isEditing) {
                                formMessage?.let { message ->
                                    Text(
                                        text = message,
                                        color = Red,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }
                                EditableField(
                                    label = "Nom de l'agence",
                                    value = agencyName,
                                    onValueChange = { agencyName = it }
                                )
                                EditableField(
                                    label = "Responsable",
                                    value = responsableName,
                                    onValueChange = { responsableName = it }
                                )
                                EditableField(
                                    label = "Email",
                                    value = agencyEmail,
                                    onValueChange = { agencyEmail = it }
                                )
                                EditableField(
                                    label = "Téléphone",
                                    value = agencyPhone,
                                    onValueChange = { agencyPhone = it }
                                )
                                EditableField(
                                    label = "Description",
                                    value = agencyDescription,
                                    onValueChange = { agencyDescription = it },
                                    minLines = 3,
                                    maxLines = 5
                                )
                                EditableField(
                                    label = "Site web",
                                    value = agencySiteWeb,
                                    onValueChange = { agencySiteWeb = it },
                                    placeholder = "https://www.example.com"
                                )
                                
                                EditableField(
                                    label = "Facebook",
                                    value = facebook,
                                    onValueChange = { facebook = it },
                                    placeholder = "https://facebook.com/votre-page"
                                )
                                
                                EditableField(
                                    label = "Instagram",
                                    value = instagram,
                                    onValueChange = { instagram = it },
                                    placeholder = "https://instagram.com/votre-compte"
                                )
                                
                                // Section Document PDF en mode édition
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Document administratif",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1A1A1A),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val documentFileIdEdit = agenceProfile?.media?.documentFileId
                                val hasDocumentEdit = !documentFileIdEdit.isNullOrBlank()
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            documentPicker.launch("application/pdf")
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (hasDocumentEdit || selectedDocumentFile != null) LightBlue else LightGray
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
                                                imageVector = if (hasDocumentEdit || selectedDocumentFile != null) Icons.Default.Description else Icons.Default.UploadFile,
                                                contentDescription = "Document",
                                                tint = DarkBlue
                                            )
                                            Column {
                                                Text(
                                                    text = when {
                                                        selectedDocumentFile != null -> "Nouveau document sélectionné"
                                                        hasDocumentEdit -> "Document téléchargé"
                                                        else -> "Télécharger un document (PDF)"
                                                    },
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = DarkBlue
                                                )
                                                if (selectedDocumentFile != null) {
                                                    Text(
                                                        text = selectedDocumentFile!!.name,
                                                        fontSize = 12.sp,
                                                        color = DarkBlue,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                } else if (hasDocumentEdit) {
                                                    Text(
                                                        text = "Fichier PDF disponible",
                                                        fontSize = 12.sp,
                                                        color = GrayBorder,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (selectedDocumentFile != null) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = "Nouveau document",
                                                tint = DarkBlue
                                            )
                                        } else if (hasDocumentEdit) {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = "Modifier le document",
                                                tint = DarkBlue
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.UploadFile,
                                                contentDescription = "Ajouter un document",
                                                tint = DarkBlue
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            formMessage = null
                                            isEditing = false
                                            user?.let {
                                                agencyName = it.nom ?: agencyName
                                                responsableName = it.prenom ?: responsableName
                                                agencyEmail = it.email
                                                agencyDescription = it.bio ?: agencyDescription
                                            }
                                            // Réinitialiser le site web depuis le profil
                                            agencySiteWeb = agenceProfile?.siteWeb ?: ""
                                            // Réinitialiser les réseaux sociaux depuis le profil
                                            facebook = agenceProfile?.socialLinks?.facebook ?: ""
                                            instagram = agenceProfile?.socialLinks?.instagram ?: ""
                                            // Réinitialiser les fichiers sélectionnés
                                            selectedLogoFile = null
                                            selectedDocumentFile = null
                                            // Réinitialiser l'image du logo si un fichier était sélectionné
                                            if (agenceProfile?.media?.photoFileId != null) {
                                                lastDownloadedLogoFileId = null // Force le re-téléchargement
                                            }
                                            if (!loadData) {
                                                agencyPhone = ""
                                            } else {
                                                scope.launch {
                                                    agencyPhone = tokenManager?.getUserPhoneSync() ?: agencyPhone
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Annuler")
                                    }
                                    Button(
                                        onClick = {
                                            if (isSaving) return@Button
                                            scope.launch {
                                                formMessage = null
                                                if (agencyEmail.isBlank()) {
                                                    formMessage = "L'email est obligatoire"
                                                    return@launch
                                                }
                                                isSaving = true
                                                try {
                                                    val currentRole = tokenManager?.getUserRoleSync()
                                                    val currentId = tokenManager?.getUserIdSync() ?: agenceProfile?.actualId
                                                    
                                                    // Mettre à jour via l'API si on a un ID et un repository
                                                    if (currentId != null && agenceRepository != null) {
                                                        val socialLinks = if (facebook.isNotBlank() || instagram.isNotBlank()) {
                                                            com.example.projecct_mobile.data.model.AgenceSocialLinks(
                                                                facebook = facebook.takeIf { it.isNotBlank() },
                                                                instagram = instagram.takeIf { it.isNotBlank() }
                                                            )
                                                        } else {
                                                            null
                                                        }
                                                        
                                                        val updateRequest = com.example.projecct_mobile.data.model.UpdateAgenceRequest(
                                                            nomAgence = agencyName.takeIf { it.isNotBlank() },
                                                            responsable = responsableName.takeIf { it.isNotBlank() },
                                                            email = agencyEmail.takeIf { it.isNotBlank() },
                                                            tel = agencyPhone.takeIf { it.isNotBlank() },
                                                            siteWeb = agencySiteWeb.takeIf { it.isNotBlank() },
                                                            description = agencyDescription.takeIf { it.isNotBlank() },
                                                            socialLinks = socialLinks
                                                        )
                                                        
                                                        val result = agenceRepository.updateAgence(currentId, updateRequest)
                                                        result.onSuccess { updatedProfile ->
                                                            agenceProfile = updatedProfile
                                                            agencySiteWeb = updatedProfile.siteWeb ?: ""
                                                            facebook = updatedProfile.socialLinks?.facebook ?: ""
                                                            instagram = updatedProfile.socialLinks?.instagram ?: ""
                                                            android.util.Log.d("AgencyProfileScreen", "✅ Profil agence mis à jour via API")
                                                            
                                                            // Uploader les médias si sélectionnés
                                                            val logoFile = selectedLogoFile
                                                            val documentFile = selectedDocumentFile
                                                            
                                                            if ((logoFile != null || documentFile != null) && updatedProfile.actualId != null) {
                                                                isUploadingLogo = logoFile != null
                                                                isUploadingDocument = documentFile != null
                                                                
                                                                val mediaResult = agenceRepository.updateProfileMedia(
                                                                    id = updatedProfile.actualId!!,
                                                                    logoFile = logoFile,
                                                                    documentFile = documentFile
                                                                )
                                                                
                                                                mediaResult.onSuccess { profileWithMedia ->
                                                                    agenceProfile = profileWithMedia
                                                                    selectedLogoFile = null
                                                                    selectedDocumentFile = null
                                                                    lastDownloadedLogoFileId = null
                                                                    lastDownloadedDocumentFileId = null
                                                                    android.util.Log.d("AgencyProfileScreen", "✅ Médias uploadés avec succès")
                                                                }
                                                                
                                                                mediaResult.onFailure { exception ->
                                                                    android.util.Log.e("AgencyProfileScreen", "❌ Erreur upload médias: ${exception.message}", exception)
                                                                    formMessage = "Profil mis à jour mais erreur upload médias: ${getErrorMessage(exception)}"
                                                                }
                                                                
                                                                isUploadingLogo = false
                                                                isUploadingDocument = false
                                                            }
                                                        }
                                                        result.onFailure { exception ->
                                                            android.util.Log.e("AgencyProfileScreen", "❌ Erreur mise à jour API: ${exception.message}", exception)
                                                            formMessage = "Erreur API: ${getErrorMessage(exception)}"
                                                            return@launch
                                                        }
                                                    }
                                                    
                                                    // Sauvegarder localement aussi
                                                    tokenManager?.saveUserInfo(
                                                        currentId,
                                                        agencyEmail.trim(),
                                                        currentRole,
                                                        agencyName.trim(),
                                                        responsableName.trim(),
                                                        agencyPhone.trim(),
                                                        agencyDescription.trim()
                                                    )
                                                    if (!agencyEmail.isBlank()) {
                                                        tokenManager?.saveAgencyProfileCache(
                                                            agencyEmail.trim(),
                                                            agencyName.trim().ifBlank { null },
                                                            responsableName.trim().ifBlank { null },
                                                            agencyPhone.trim().ifBlank { null },
                                                            agencyDescription.trim().ifBlank { null }
                                                        )
                                                    }
                                                    user = user?.copy(
                                                        nom = agencyName.trim().ifBlank { null },
                                                        prenom = responsableName.trim().ifBlank { null },
                                                        email = agencyEmail.trim(),
                                                        bio = agencyDescription.trim().ifBlank { null }
                                                    ) ?: User(
                                                        id = currentId,
                                                        nom = agencyName.trim().ifBlank { null },
                                                        prenom = responsableName.trim().ifBlank { null },
                                                        email = agencyEmail.trim(),
                                                        role = currentRole?.let { role ->
                                                            runCatching { UserRole.valueOf(role.uppercase()) }.getOrNull()
                                                        },
                                                        bio = agencyDescription.trim().ifBlank { null }
                                                    )
                                                    agencyPhone = agencyPhone.trim()
                                                    agencyDescription = agencyDescription.trim()
                                                    formMessage = "Informations enregistrées"
                                                    isEditing = false
                                                } catch (e: Exception) {
                                                    android.util.Log.e("AgencyProfileScreen", "❌ Exception enregistrement: ${e.message}", e)
                                                    formMessage = "Impossible d'enregistrer: ${e.message}"
                                                } finally {
                                                    isSaving = false
                                                }
                                            }
                                        },
                                        enabled = !isSaving,
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                                    ) {
                                        Text(if (isSaving) "Enregistrement..." else "Enregistrer", color = White)
                                    }
                                }
                            } else {
                                if (isEditing) {
                                    formMessage?.let { message ->
                                        Text(
                                            text = message,
                                            color = Red,
                                            fontSize = 14.sp
                                        )
                                    }
                                    EditableField(
                                        label = "Nom de l'agence",
                                        value = agencyName,
                                        onValueChange = { agencyName = it }
                                    )
                                    EditableField(
                                        label = "Responsable",
                                        value = responsableName,
                                        onValueChange = { responsableName = it }
                                    )
                                    EditableField(
                                        label = "Email",
                                        value = agencyEmail,
                                        onValueChange = { agencyEmail = it }
                                    )
                                    EditableField(
                                        label = "Téléphone",
                                        value = agencyPhone,
                                        onValueChange = { agencyPhone = it }
                                    )
                                    EditableField(
                                        label = "Description",
                                        value = agencyDescription,
                                        onValueChange = { agencyDescription = it },
                                        minLines = 3,
                                        maxLines = 5
                                    )
                                    
                                    EditableField(
                                        label = "Facebook",
                                        value = facebook,
                                        onValueChange = { facebook = it },
                                        placeholder = "https://facebook.com/votre-page"
                                    )
                                    
                                    EditableField(
                                        label = "Instagram",
                                        value = instagram,
                                        onValueChange = { instagram = it },
                                        placeholder = "https://instagram.com/votre-compte"
                                    )
                                    
                                    // Section Document PDF en mode édition
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Document administratif",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A1A1A),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    val documentFileIdEdit = agenceProfile?.media?.documentFileId
                                    val hasDocumentEdit = !documentFileIdEdit.isNullOrBlank()
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                documentPicker.launch("application/pdf")
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (hasDocumentEdit || selectedDocumentFile != null) LightBlue else LightGray
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
                                                    imageVector = if (hasDocumentEdit || selectedDocumentFile != null) Icons.Default.Description else Icons.Default.UploadFile,
                                                    contentDescription = "Document",
                                                    tint = DarkBlue
                                                )
                                                Column {
                                                    Text(
                                                        text = when {
                                                            selectedDocumentFile != null -> "Nouveau document sélectionné"
                                                            hasDocumentEdit -> "Document téléchargé"
                                                            else -> "Télécharger un document (PDF)"
                                                        },
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = DarkBlue
                                                    )
                                                    if (selectedDocumentFile != null) {
                                                        Text(
                                                            text = selectedDocumentFile!!.name,
                                                            fontSize = 12.sp,
                                                            color = DarkBlue,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    } else if (hasDocumentEdit) {
                                                        Text(
                                                            text = "Fichier PDF disponible",
                                                            fontSize = 12.sp,
                                                            color = GrayBorder,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            if (selectedDocumentFile != null) {
                                                Icon(
                                                    imageVector = Icons.Default.UploadFile,
                                                    contentDescription = "Nouveau document",
                                                    tint = DarkBlue
                                                )
                                            } else if (hasDocumentEdit) {
                                                Icon(
                                                    imageVector = Icons.Default.UploadFile,
                                                    contentDescription = "Modifier le document",
                                                    tint = DarkBlue
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.UploadFile,
                                                    contentDescription = "Ajouter un document",
                                                    tint = DarkBlue
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                formMessage = null
                                                isEditing = false
                                                user?.let {
                                                    agencyName = it.nom ?: agencyName
                                                    responsableName = it.prenom ?: responsableName
                                                    agencyEmail = it.email
                                                    agencyDescription = it.bio ?: agencyDescription
                                                }
                                                // Réinitialiser les réseaux sociaux depuis le profil
                                                facebook = agenceProfile?.socialLinks?.facebook ?: ""
                                                instagram = agenceProfile?.socialLinks?.instagram ?: ""
                                                // Réinitialiser les fichiers sélectionnés
                                                selectedLogoFile = null
                                                selectedDocumentFile = null
                                                // Réinitialiser l'image du logo si un fichier était sélectionné
                                                if (agenceProfile?.media?.photoFileId != null) {
                                                    lastDownloadedLogoFileId = null // Force le re-téléchargement
                                                }
                                                if (!loadData) {
                                                    agencyPhone = ""
                                                } else {
                                                    scope.launch {
                                                        agencyPhone = tokenManager?.getUserPhoneSync() ?: agencyPhone
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Annuler")
                                        }
                                        Button(
                                            onClick = {
                                                if (isSaving) return@Button
                                                scope.launch {
                                                    formMessage = null
                                                    if (agencyEmail.isBlank()) {
                                                        formMessage = "L'email est obligatoire"
                                                        return@launch
                                                    }
                                                    isSaving = true
                                                    try {
                                                        val currentRole = tokenManager?.getUserRoleSync()
                                                        val currentId = tokenManager?.getUserIdSync() ?: agenceProfile?.actualId
                                                        
                                                        // Mettre à jour via l'API si on a un ID et un repository
                                                        if (currentId != null && agenceRepository != null) {
                                                            val socialLinks = if (facebook.isNotBlank() || instagram.isNotBlank()) {
                                                                com.example.projecct_mobile.data.model.AgenceSocialLinks(
                                                                    facebook = facebook.takeIf { it.isNotBlank() },
                                                                    instagram = instagram.takeIf { it.isNotBlank() }
                                                                )
                                                            } else {
                                                                null
                                                            }
                                                            
                                                            val updateRequest = com.example.projecct_mobile.data.model.UpdateAgenceRequest(
                                                                nomAgence = agencyName.takeIf { it.isNotBlank() },
                                                                responsable = responsableName.takeIf { it.isNotBlank() },
                                                                email = agencyEmail.takeIf { it.isNotBlank() },
                                                                tel = agencyPhone.takeIf { it.isNotBlank() },
                                                                siteWeb = agencySiteWeb.takeIf { it.isNotBlank() },
                                                                description = agencyDescription.takeIf { it.isNotBlank() },
                                                                socialLinks = socialLinks
                                                            )
                                                            
                                                            val result = agenceRepository.updateAgence(currentId, updateRequest)
                                                            result.onSuccess { updatedProfile ->
                                                                agenceProfile = updatedProfile
                                                                agencySiteWeb = updatedProfile.siteWeb ?: ""
                                                                facebook = updatedProfile.socialLinks?.facebook ?: ""
                                                                instagram = updatedProfile.socialLinks?.instagram ?: ""
                                                                android.util.Log.d("AgencyProfileScreen", "✅ Profil agence mis à jour via API")
                                                                
                                                                // Uploader les médias si sélectionnés
                                                                val logoFile = selectedLogoFile
                                                                val documentFile = selectedDocumentFile
                                                                
                                                                if ((logoFile != null || documentFile != null) && updatedProfile.actualId != null) {
                                                                    isUploadingLogo = logoFile != null
                                                                    isUploadingDocument = documentFile != null
                                                                    
                                                                    val mediaResult = agenceRepository.updateProfileMedia(
                                                                        id = updatedProfile.actualId!!,
                                                                        logoFile = logoFile,
                                                                        documentFile = documentFile
                                                                    )
                                                                    
                                                                    mediaResult.onSuccess { profileWithMedia ->
                                                                        agenceProfile = profileWithMedia
                                                                        selectedLogoFile = null
                                                                        selectedDocumentFile = null
                                                                        lastDownloadedLogoFileId = null
                                                                        lastDownloadedDocumentFileId = null
                                                                        android.util.Log.d("AgencyProfileScreen", "✅ Médias uploadés avec succès")
                                                                    }
                                                                    
                                                                    mediaResult.onFailure { exception ->
                                                                        android.util.Log.e("AgencyProfileScreen", "❌ Erreur upload médias: ${exception.message}", exception)
                                                                        formMessage = "Profil mis à jour mais erreur upload médias: ${getErrorMessage(exception)}"
                                                                    }
                                                                    
                                                                    isUploadingLogo = false
                                                                    isUploadingDocument = false
                                                                }
                                                            }
                                                            result.onFailure { exception ->
                                                                android.util.Log.e("AgencyProfileScreen", "❌ Erreur mise à jour API: ${exception.message}", exception)
                                                                formMessage = "Erreur API: ${getErrorMessage(exception)}"
                                                                return@launch
                                                            }
                                                        }
                                                        
                                                        // Sauvegarder localement aussi
                                                        tokenManager?.saveUserInfo(
                                                            currentId,
                                                            agencyEmail.trim(),
                                                            currentRole,
                                                            agencyName.trim(),
                                                            responsableName.trim(),
                                                            agencyPhone.trim(),
                                                            agencyDescription.trim()
                                                        )
                                                        if (!agencyEmail.isBlank()) {
                                                            tokenManager?.saveAgencyProfileCache(
                                                                agencyEmail.trim(),
                                                                agencyName.trim().ifBlank { null },
                                                                responsableName.trim().ifBlank { null },
                                                                agencyPhone.trim().ifBlank { null },
                                                                agencyDescription.trim().ifBlank { null }
                                                            )
                                                        }
                                                        user = user?.copy(
                                                            nom = agencyName.trim().ifBlank { null },
                                                            prenom = responsableName.trim().ifBlank { null },
                                                            email = agencyEmail.trim(),
                                                            bio = agencyDescription.trim().ifBlank { null }
                                                        ) ?: User(
                                                            id = currentId,
                                                            nom = agencyName.trim().ifBlank { null },
                                                            prenom = responsableName.trim().ifBlank { null },
                                                            email = agencyEmail.trim(),
                                                            role = currentRole?.let { role ->
                                                                runCatching { UserRole.valueOf(role.uppercase()) }.getOrNull()
                                                            },
                                                            bio = agencyDescription.trim().ifBlank { null }
                                                        )
                                                        agencyPhone = agencyPhone.trim()
                                                        agencyDescription = agencyDescription.trim()
                                                        formMessage = "Informations enregistrées"
                                                        isEditing = false
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AgencyProfileScreen", "❌ Exception enregistrement: ${e.message}", e)
                                                        formMessage = "Impossible d'enregistrer: ${e.message}"
                                                    } finally {
                                                        isSaving = false
                                                    }
                                                }
                                            },
                                            enabled = !isSaving,
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                                        ) {
                                            Text(if (isSaving) "Enregistrement..." else "Enregistrer", color = White)
                                        }
                                    }
                                } else {
                                    // Mode affichage - Style comme dans l'image
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Person,
                                        label = "Responsable",
                                        value = responsableName.ifBlank { "Non renseigné" }
                                    )
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Business,
                                        label = "Créée le",
                                        value = user?.createdAt ?: "Non disponible"
                                    )
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Business,
                                        label = "Rôle",
                                        value = user?.role?.name ?: "RECRUTEUR"
                                    )
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Email,
                                        label = "Email",
                                        value = agencyEmail
                                    )
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Phone,
                                        label = "Téléphone",
                                        value = agencyPhone.ifBlank { "Non renseigné" }
                                    )
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    
                                    ProfileInfoRow(
                                        icon = Icons.Filled.Description,
                                        label = "Description",
                                        value = agencyDescription.ifBlank { "Non renseigné" }
                                    )
                                    
                                    // Section "Other Ways People Can Find Me" avec icônes réseaux sociaux
                                    Spacer(modifier = Modifier.height(24.dp))
                                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.3f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Other Ways People Can Find Me",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A1A1A),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    // Fonction helper pour convertir un nom d'utilisateur ou URL en URL complète
                                    fun String?.isValidSocialLink(): Boolean {
                                        return !this.isNullOrBlank() && this.lowercase() != "null"
                                    }
                                    
                                    val facebookValue = facebook.takeIf { it.isValidSocialLink() } 
                                        ?: agenceProfile?.socialLinks?.facebook?.takeIf { it.isValidSocialLink() }
                                    val instagramValue = instagram.takeIf { it.isValidSocialLink() } 
                                        ?: agenceProfile?.socialLinks?.instagram?.takeIf { it.isValidSocialLink() }
                                    
                                    fun getFacebookUrl(input: String?): String? {
                                        if (input.isNullOrBlank()) return null
                                        val trimmed = input.trim()
                                        return when {
                                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                                            trimmed.startsWith("facebook.com/") -> "https://$trimmed"
                                            trimmed.startsWith("www.facebook.com/") -> "https://$trimmed"
                                            else -> {
                                                val username = trimmed.replace(" ", "-").lowercase()
                                                "https://www.facebook.com/$username"
                                            }
                                        }
                                    }
                                    
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
                                    
                                    val finalFacebookUrl = getFacebookUrl(facebookValue)
                                    val finalInstagramUrl = getInstagramUrl(instagramValue)
                                    
                                    // Fonction pour obtenir l'URL du site web
                                    fun getWebsiteUrl(input: String?): String? {
                                        if (input.isNullOrBlank()) return null
                                        val trimmed = input.trim()
                                        return when {
                                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                                            else -> "https://$trimmed"
                                        }
                                    }
                                    
                                    val siteWebForIcon = agencySiteWeb.takeIf { it.isNotBlank() }
                                    val finalWebsiteUrl = getWebsiteUrl(siteWebForIcon)
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (siteWebForIcon != null) {
                                            SocialMediaIconWithVector(
                                                imageVector = Icons.Filled.Language,
                                                onClick = {
                                                    try {
                                                        val url = finalWebsiteUrl ?: ""
                                                        if (url.isNotBlank()) {
                                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                            context.startActivity(intent)
                                                        }
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AgencyProfileScreen", "Erreur ouverture site web: ${e.message}", e)
                                                    }
                                                }
                                            )
                                        }
                                        if (facebookValue != null) {
                                            SocialMediaIcon(
                                                painter = painterResource(id = R.drawable.facebook),
                                                onClick = {
                                                    try {
                                                        val url = finalFacebookUrl ?: "https://www.facebook.com"
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AgencyProfileScreen", "Erreur ouverture Facebook: ${e.message}", e)
                                                    }
                                                }
                                            )
                                        }
                                        if (instagramValue != null) {
                                            SocialMediaIcon(
                                                painter = painterResource(id = R.drawable.instagram),
                                                onClick = {
                                                    try {
                                                        val url = finalInstagramUrl ?: "https://www.instagram.com"
                                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("AgencyProfileScreen", "Erreur ouverture Instagram: ${e.message}", e)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    
                                    // Section Document PDF
                                    val documentFileId = agenceProfile?.media?.documentFileId
                                    val hasDocument = !documentFileId.isNullOrBlank()
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Document administratif",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1A1A1A),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (isEditing) {
                                                    Modifier.clickable {
                                                        documentPicker.launch("application/pdf")
                                                    }
                                                } else if (hasDocument) {
                                                    Modifier.clickable {
                                                        scope.launch {
                                                            if (agenceRepository == null || documentFileId == null) return@launch
                                                            isDownloadingDocument = true
                                                            try {
                                                                val documentResult = agenceRepository.downloadMedia(documentFileId)
                                                                documentResult.onSuccess { bytes ->
                                                                    if (bytes != null && bytes.isNotEmpty()) {
                                                                        val pdfFile = File(context.cacheDir, "document_${System.currentTimeMillis()}.pdf")
                                                                        FileOutputStream(pdfFile).use { output ->
                                                                            output.write(bytes)
                                                                        }
                                                                        
                                                                        try {
                                                                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                                                                context,
                                                                                "${context.packageName}.fileprovider",
                                                                                pdfFile
                                                                            )
                                                                            
                                                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                                                setDataAndType(uri, "application/pdf")
                                                                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                            }
                                                                            
                                                                            val chooserIntent = android.content.Intent.createChooser(intent, "Ouvrir le document avec")
                                                                            chooserIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                                            
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
                                                                        } catch (e: android.content.ActivityNotFoundException) {
                                                                            errorMessage = "Aucune application pour ouvrir le PDF. Veuillez installer un lecteur PDF."
                                                                        } catch (e: Exception) {
                                                                            errorMessage = "Impossible d'ouvrir le PDF: ${e.message}"
                                                                        }
                                                                    }
                                                                    isDownloadingDocument = false
                                                                }
                                                                documentResult.onFailure { exception ->
                                                                    errorMessage = "Erreur lors du téléchargement: ${getErrorMessage(exception)}"
                                                                    isDownloadingDocument = false
                                                                }
                                                            } catch (e: Exception) {
                                                                errorMessage = "Erreur: ${e.message}"
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
                                                    contentDescription = "Document",
                                                    tint = DarkBlue
                                                )
                                                Column {
                                                    Text(
                                                        text = when {
                                                            selectedDocumentFile != null -> "Nouveau document sélectionné"
                                                            hasDocument -> "Document téléchargé"
                                                            isEditing -> "Télécharger un document (PDF)"
                                                            else -> "Aucun document"
                                                        },
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = DarkBlue
                                                    )
                                                    if (hasDocument && selectedDocumentFile == null) {
                                                        Text(
                                                            text = "Fichier PDF disponible",
                                                            fontSize = 12.sp,
                                                            color = GrayBorder,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    } else if (selectedDocumentFile != null) {
                                                        Text(
                                                            text = selectedDocumentFile!!.name,
                                                            fontSize = 12.sp,
                                                            color = DarkBlue,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            when {
                                                isUploadingDocument -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = DarkBlue,
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                                isDownloadingDocument -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = DarkBlue,
                                                        strokeWidth = 2.dp
                                                    )
                                                }
                                                selectedDocumentFile != null -> {
                                                    Icon(
                                                        imageVector = Icons.Default.UploadFile,
                                                        contentDescription = "Nouveau document",
                                                        tint = DarkBlue
                                                    )
                                                }
                                                hasDocument && !isEditing -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Visibility,
                                                        contentDescription = "Voir le document",
                                                        tint = DarkBlue
                                                    )
                                                }
                                                isEditing -> {
                                                    Icon(
                                                        imageVector = if (hasDocument) Icons.Default.UploadFile else Icons.Default.UploadFile,
                                                        contentDescription = if (hasDocument) "Modifier le document" else "Ajouter un document",
                                                        tint = DarkBlue
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            formMessage = null
                                            isEditing = true
                                        }
                                    ) {
                                        Text("Modifier mes informations", color = DarkBlue, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToCastings,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                                    contentDescription = null,
                                    tint = White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Voir mes castings", color = White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = onNavigateToCreateCasting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GrayBorder.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    tint = DarkBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Créer un casting", color = DarkBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Se déconnecter", color = Red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 13.sp, color = GrayBorder)
        Text(text = value.ifBlank { "Non renseigné" }, fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
            contentDescription = "Site web",
            tint = DarkBlue,
            modifier = Modifier.size(32.dp)
        )
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
        singleLine = minLines == 1 && maxLines == 1
    )
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
        android.util.Log.d("AgencyProfileScreen", "✅ Fichier copié vers: ${file.absolutePath}, taille: ${file.length()} bytes")
        file
    } catch (e: Exception) {
        android.util.Log.e("AgencyProfileScreen", "Erreur lors de la copie du fichier: ${e.message}", e)
        null
    }
}

@Preview(showBackground = true)
@Composable
fun AgencyProfileScreenPreview() {
    Projecct_MobileTheme {
        AgencyProfileScreen(
            loadData = false,
            initialUser = User(
                id = "agency_1",
                nom = "CastMate Agency",
                prenom = "Responsable",
                email = "agency@example.com",
                role = UserRole.RECRUTEUR,
                bio = "Agence spécialisée dans le casting depuis 2015."
            )
        )
    }
}
