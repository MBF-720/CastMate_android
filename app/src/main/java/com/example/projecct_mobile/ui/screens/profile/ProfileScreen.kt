package com.example.projecct_mobile.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.example.projecct_mobile.data.model.ActeurProfile
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.model.UserRole
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.data.repository.ActeurRepository
import com.example.projecct_mobile.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    android.util.Log.e("ProfileScreen", "🔵🔵🔵 ProfileScreen composé 🔵🔵🔵")
    System.out.println("ProfileScreen: ProfileScreen composé")
    
    var user by remember { mutableStateOf<User?>(null) }
    var acteurProfile by remember { mutableStateOf<ActeurProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val userRepository = remember { UserRepository() }
    val acteurRepository = remember { ActeurRepository() }
    var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var mediaError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Log quand acteurProfile change et déclencher le téléchargement de la photo
    LaunchedEffect(acteurProfile) {
        android.util.Log.e("ProfileScreen", "🔵🔵🔵 LaunchedEffect(acteurProfile) déclenché - acteurProfile != null: ${acteurProfile != null} 🔵🔵🔵")
        System.out.println("ProfileScreen: LaunchedEffect(acteurProfile) déclenché - acteurProfile != null: ${acteurProfile != null}")
        
        if (acteurProfile != null) {
            android.util.Log.e("ProfileScreen", "🔵 acteurProfile.media: ${acteurProfile?.media}")
            android.util.Log.e("ProfileScreen", "🔵 acteurProfile.media?.photoFileId: '${acteurProfile?.media?.photoFileId}'")
            System.out.println("ProfileScreen: acteurProfile.media?.photoFileId: '${acteurProfile?.media?.photoFileId}'")
            
            // Télécharger la photo si photoFileId est disponible
            val photoFileId = acteurProfile?.media?.photoFileId
            android.util.Log.e("ProfileScreen", "🔵 photoFileId extrait: '$photoFileId', isNullOrBlank: ${photoFileId.isNullOrBlank()}, profileImage == null: ${profileImage == null}")
            System.out.println("ProfileScreen: photoFileId extrait: '$photoFileId', isNullOrBlank: ${photoFileId.isNullOrBlank()}, profileImage == null: ${profileImage == null}")
            
            if (!photoFileId.isNullOrBlank() && profileImage == null) {
                try {
                    android.util.Log.e("ProfileScreen", "🚀🚀🚀 Téléchargement de la photo depuis LaunchedEffect(acteurProfile): $photoFileId 🚀🚀🚀")
                    System.out.println("ProfileScreen: Début du téléchargement de la photo: $photoFileId")
                    val mediaResult = acteurRepository.downloadMedia(photoFileId)
                    android.util.Log.e("ProfileScreen", "📥 Résultat téléchargement: ${mediaResult.isSuccess}")
                    System.out.println("ProfileScreen: Résultat téléchargement: ${mediaResult.isSuccess}")
                    
                    if (mediaResult.isSuccess) {
                        val bytes = mediaResult.getOrNull()
                        android.util.Log.e("ProfileScreen", "📦 Bytes reçus: ${bytes?.size ?: 0} bytes")
                        System.out.println("ProfileScreen: Bytes reçus: ${bytes?.size ?: 0} bytes")
                        
                        if (bytes != null && bytes.isNotEmpty()) {
                            val bitmap = withContext(Dispatchers.IO) {
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            
                            if (bitmap != null) {
                                android.util.Log.e("ProfileScreen", "🖼️ Bitmap décodé: ${bitmap.width}x${bitmap.height}")
                                System.out.println("ProfileScreen: Bitmap décodé: ${bitmap.width}x${bitmap.height}")
                                profileImage = bitmap.asImageBitmap()
                                android.util.Log.e("ProfileScreen", "✅✅✅ Photo chargée avec succès depuis LaunchedEffect(acteurProfile) ✅✅✅")
                                System.out.println("ProfileScreen: Photo chargée avec succès!")
                            } else {
                                android.util.Log.e("ProfileScreen", "❌ Bitmap est null après décodage")
                                System.out.println("ProfileScreen: Bitmap est null après décodage")
                            }
                        } else {
                            android.util.Log.e("ProfileScreen", "⚠️ Bytes vides ou null")
                            System.out.println("ProfileScreen: Bytes vides ou null")
                        }
                    } else {
                        val exception = mediaResult.exceptionOrNull()
                        android.util.Log.e("ProfileScreen", "❌ Erreur téléchargement: ${exception?.message}", exception)
                        System.out.println("ProfileScreen: Erreur téléchargement: ${exception?.message}")
                    }
                } catch (e: CancellationException) {
                    android.util.Log.e("ProfileScreen", "⚠️ Téléchargement annulé")
                    System.out.println("ProfileScreen: Téléchargement annulé")
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "❌ Exception téléchargement: ${e.message}", e)
                    System.out.println("ProfileScreen: Exception téléchargement: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                android.util.Log.e("ProfileScreen", "⚠️ Condition non remplie: photoFileId.isNullOrBlank()=${photoFileId.isNullOrBlank()}, profileImage==null=${profileImage == null}")
                System.out.println("ProfileScreen: Condition non remplie: photoFileId.isNullOrBlank()=${photoFileId.isNullOrBlank()}, profileImage==null=${profileImage == null}")
            }
        } else {
            android.util.Log.e("ProfileScreen", "⚠️ acteurProfile est null")
            System.out.println("ProfileScreen: acteurProfile est null")
        }
    }
    
    // Log quand user change
    LaunchedEffect(user) {
        android.util.Log.d("ProfileScreen", "🔵 user changé: ${user != null}")
        if (user != null) {
            android.util.Log.d("ProfileScreen", "🔵 user.media: ${user?.media}")
            android.util.Log.d("ProfileScreen", "🔵 user.media?.photoFileId: ${user?.media?.photoFileId}")
        }
    }

    // 1. Charger le profil utilisateur (général)
    LaunchedEffect(Unit) {
        try {
            android.util.Log.e("ProfileScreen", "=== 🟢 LaunchedEffect(Unit) démarré ===")
            System.out.println("ProfileScreen: LaunchedEffect(Unit) démarré")
            isLoading = true
            errorMessage = null
            mediaError = null

            android.util.Log.e("ProfileScreen", "=== Début du chargement du profil ===")
            System.out.println("ProfileScreen: Début du chargement du profil")
            val userResult = userRepository.getCurrentUser()
            
            if (userResult.isSuccess) {
                val userProfile = userResult.getOrNull()
                if (userProfile != null) {
                    user = userProfile
                    android.util.Log.e("ProfileScreen", "Profil utilisateur chargé: ${userProfile.email}, role: ${userProfile.role}")
                    System.out.println("ProfileScreen: Profil utilisateur chargé: ${userProfile.email}, role: ${userProfile.role}")
                    android.util.Log.e("ProfileScreen", "User media: ${userProfile.media}")

                    // 2. Si c'est un acteur, charger le profil acteur détaillé
                    if (userProfile.role == UserRole.ACTEUR) {
                        try {
                            android.util.Log.e("ProfileScreen", "Chargement du profil acteur...")
                            System.out.println("ProfileScreen: Chargement du profil acteur...")
                            val acteurResult = acteurRepository.getCurrentActeur()
                            if (acteurResult.isSuccess) {
                                val profile = acteurResult.getOrNull()
                                if (profile != null) {
                                    android.util.Log.e("ProfileScreen", "✅ Profil acteur chargé: ${profile.nom} ${profile.prenom}")
                                    System.out.println("ProfileScreen: Profil acteur chargé: ${profile.nom} ${profile.prenom}")
                                    android.util.Log.e("ProfileScreen", "✅ ActeurProfile media: ${profile.media}")
                                    android.util.Log.e("ProfileScreen", "✅ photoFileId depuis profil: '${profile.media?.photoFileId}'")
                                    System.out.println("ProfileScreen: photoFileId depuis profil: '${profile.media?.photoFileId}'")
                                    
                                    // Mettre à jour acteurProfile (cela déclenchera le LaunchedEffect(photoFileId))
                                    android.util.Log.e("ProfileScreen", "🔄 AVANT: acteurProfile = ${acteurProfile?.nom} ${acteurProfile?.prenom}")
                                    System.out.println("ProfileScreen: AVANT: acteurProfile = ${acteurProfile?.nom} ${acteurProfile?.prenom}")
                                    acteurProfile = profile
                                    android.util.Log.e("ProfileScreen", "✅✅✅ acteurProfile mis à jour avec photoFileId: '${profile.media?.photoFileId}' ✅✅✅")
                                    System.out.println("ProfileScreen: acteurProfile mis à jour avec photoFileId: '${profile.media?.photoFileId}'")
                                    android.util.Log.e("ProfileScreen", "🔄 APRÈS: acteurProfile = ${acteurProfile?.nom} ${acteurProfile?.prenom}")
                                    System.out.println("ProfileScreen: APRÈS: acteurProfile = ${acteurProfile?.nom} ${acteurProfile?.prenom}")
                                } else {
                                    android.util.Log.e("ProfileScreen", "❌ Profil acteur est null")
                                    System.out.println("ProfileScreen: Profil acteur est null")
                                }
                            } else {
                                val exception = acteurResult.exceptionOrNull()
                                android.util.Log.e("ProfileScreen", "❌ Impossible de charger le profil acteur: ${exception?.message}", exception)
                                System.out.println("ProfileScreen: Impossible de charger le profil acteur: ${exception?.message}")
                            }
                        } catch (e: CancellationException) {
                            android.util.Log.e("ProfileScreen", "⚠️ Chargement acteur annulé")
                            System.out.println("ProfileScreen: Chargement acteur annulé")
                            throw e
                        } catch (e: Exception) {
                            android.util.Log.e("ProfileScreen", "❌ Erreur lors du chargement du profil acteur: ${e.message}", e)
                            System.out.println("ProfileScreen: Erreur lors du chargement du profil acteur: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    isLoading = false
                    android.util.Log.e("ProfileScreen", "=== Profil chargé, isLoading = false ===")
                    System.out.println("ProfileScreen: Profil chargé, isLoading = false")
                } else {
                    android.util.Log.e("ProfileScreen", "❌ UserProfile est null")
                    System.out.println("ProfileScreen: UserProfile est null")
                    isLoading = false
                }
            } else {
                val exception = userResult.exceptionOrNull()
                android.util.Log.e("ProfileScreen", "❌ Erreur chargement utilisateur: ${exception?.message}", exception)
                System.out.println("ProfileScreen: Erreur chargement utilisateur: ${exception?.message}")
                errorMessage = "Erreur lors du chargement: ${exception?.message}"
                isLoading = false
            }
        } catch (e: CancellationException) {
            android.util.Log.e("ProfileScreen", "⚠️ LaunchedEffect annulé")
            System.out.println("ProfileScreen: LaunchedEffect annulé")
            return@LaunchedEffect
        } catch (e: Exception) {
            android.util.Log.e("ProfileScreen", "❌ Exception: ${e.message}", e)
            System.out.println("ProfileScreen: Exception: ${e.message}")
            e.printStackTrace()
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }

    // 2. Télécharger la photo quand photoFileId est disponible
    // Utiliser acteurProfile?.media?.photoFileId et user?.media?.photoFileId comme clés pour forcer le déclenchement
    LaunchedEffect(acteurProfile?.media?.photoFileId, user?.media?.photoFileId) {
        val currentPhotoFileId = acteurProfile?.media?.photoFileId ?: user?.media?.photoFileId
        android.util.Log.d("ProfileScreen", "=== 🟢 LaunchedEffect photoFileId déclenché ===")
        android.util.Log.d("ProfileScreen", "🔵 currentPhotoFileId: '$currentPhotoFileId'")
        android.util.Log.d("ProfileScreen", "🔵 acteurProfile?.media?.photoFileId: '${acteurProfile?.media?.photoFileId}'")
        android.util.Log.d("ProfileScreen", "🔵 user?.media?.photoFileId: '${user?.media?.photoFileId}'")
        android.util.Log.d("ProfileScreen", "🔵 acteurProfile != null: ${acteurProfile != null}")
        android.util.Log.d("ProfileScreen", "🔵 user != null: ${user != null}")
        
        if (!currentPhotoFileId.isNullOrBlank()) {
            try {
                android.util.Log.d("ProfileScreen", "🚀 Début du téléchargement de la photo: $currentPhotoFileId")
                profileImage = null // Réinitialiser l'image
                mediaError = null
                
                val mediaResult = acteurRepository.downloadMedia(currentPhotoFileId)
                android.util.Log.d("ProfileScreen", "📥 Résultat du téléchargement: ${mediaResult.isSuccess}")
                
                if (mediaResult.isSuccess) {
                    val bytes = mediaResult.getOrNull()
                    android.util.Log.d("ProfileScreen", "📦 Bytes reçus: ${bytes?.size ?: 0} bytes")
                    
                    if (bytes != null && bytes.isNotEmpty()) {
                        android.util.Log.d("ProfileScreen", "📦 Début du décodage du bitmap...")
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                        
                        if (bitmap != null) {
                            android.util.Log.d("ProfileScreen", "🖼️ Bitmap décodé: ${bitmap.width}x${bitmap.height}")
                            profileImage = bitmap.asImageBitmap()
                            android.util.Log.d("ProfileScreen", "✅✅✅ ImageBitmap créé et assigné à profileImage ✅✅✅")
                        } else {
                            android.util.Log.e("ProfileScreen", "❌ Erreur: Bitmap est null après décodage")
                            mediaError = "Impossible de décoder l'image"
                        }
                    } else {
                        android.util.Log.w("ProfileScreen", "⚠️ Bytes vides ou null")
                        mediaError = "Aucune donnée reçue"
                    }
                } else {
                    val exception = mediaResult.exceptionOrNull()
                    android.util.Log.e("ProfileScreen", "❌ Impossible de charger la photo: ${exception?.message}", exception)
                    mediaError = "Impossible de charger la photo: ${exception?.message}"
                }
            } catch (e: CancellationException) {
                android.util.Log.w("ProfileScreen", "⚠️ Téléchargement annulé")
                // Ne pas relancer l'exception pour ne pas bloquer l'affichage
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "❌ Exception lors du téléchargement: ${e.message}", e)
                android.util.Log.e("ProfileScreen", "❌ Stack trace:", e)
                mediaError = "Erreur: ${e.message}"
            }
        } else {
            android.util.Log.w("ProfileScreen", "⚠️ currentPhotoFileId est null ou vide - acteurProfile: ${acteurProfile != null}, user: ${user != null}")
            android.util.Log.w("ProfileScreen", "⚠️ acteurProfile?.media: ${acteurProfile?.media}")
            android.util.Log.w("ProfileScreen", "⚠️ acteurProfile?.media?.photoFileId: '${acteurProfile?.media?.photoFileId}'")
        }
    }

    // Utiliser les données acteur si disponibles, sinon les données user
    val nom = acteurProfile?.nom ?: user?.nom ?: ""
    val prenom = acteurProfile?.prenom ?: user?.prenom ?: ""
    val email = acteurProfile?.email ?: user?.email ?: ""
    val phone = acteurProfile?.tel ?: ""
    val age = acteurProfile?.age?.toString().orEmpty()
    val gouvernorat = acteurProfile?.gouvernorat ?: ""
    val experience = acteurProfile?.experience?.toString().orEmpty()
    val bio = user?.bio ?: acteurProfile?.centresInteret?.joinToString(", ") ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        // En-tête bleu avec gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Barre de navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                White.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Mon Profil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )

                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                White.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Photo de profil et informations
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo de profil avec shadow
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                spotColor = White.copy(alpha = 0.3f)
                            )
                            .clip(CircleShape)
                            .background(White)
                            .border(4.dp, White.copy(alpha = 0.3f), CircleShape),
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
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = DarkBlue,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    mediaError?.let { message ->
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Nom complet
                    Text(
                        text = listOf(prenom, nom).joinToString(" ").trim().ifEmpty { "Utilisateur" },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.3.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rôle avec badge
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = user?.role?.name ?: "Non spécifié",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Contenu du profil - Card avec shadow
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    spotColor = DarkBlue.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Spacer(modifier = Modifier.height(90.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = DarkBlue, strokeWidth = 3.dp)
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Red,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = errorMessage ?: "Erreur",
                            color = Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    val result = userRepository.getCurrentUser()
                                    isLoading = false
                                    result.onSuccess { userProfile ->
                                        user = userProfile
                                    }
                                    result.onFailure { exception ->
                                        errorMessage = "Erreur: ${exception.message}"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
                        ) {
                            Text("Réessayer", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Informations personnelles
                    ProfileSection(
                        title = "Informations personnelles",
                        items = listOfNotNull(
                            ProfileItem("Email", email.ifBlank { "Non spécifié" }, Icons.Default.Email),
                            phone.takeIf { it.isNotBlank() }?.let { ProfileItem("Téléphone", it, Icons.Default.Call) },
                            age.takeIf { it.isNotBlank() }?.let { ProfileItem("Âge", "$it ans", Icons.Default.Info) },
                            gouvernorat.takeIf { it.isNotBlank() }?.let { ProfileItem("Gouvernorat", it, Icons.Default.Info) },
                            experience.takeIf { it.isNotBlank() }?.let { ProfileItem("Années d'expérience", it, Icons.Default.Info) },
                            bio.takeIf { it.isNotBlank() }?.let { ProfileItem("Bio", it, Icons.Default.Info) }
                        )
                    )

                    // CV avec design moderne
                    val documentFileId = acteurProfile?.media?.documentFileId
                    if (!documentFileId.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = DarkBlue.copy(alpha = 0.1f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),
                            onClick = { /* Ouvrir le CV */ }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                DarkBlue.copy(alpha = 0.1f),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = "CV",
                                            tint = DarkBlue,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Mon CV",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Black
                                        )
                                        Text(
                                            text = "Cliquer pour voir",
                                            fontSize = 13.sp,
                                            color = GrayBorder,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open",
                                    tint = DarkBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bouton de déconnexion avec design moderne
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = Red.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Logout",
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Déconnexion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkBlue,
            letterSpacing = 0.3.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        items.forEach { item ->
            ProfileItemRow(item = item)
        }
    }
}

data class ProfileItem(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ProfileItemRow(item: ProfileItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DarkBlue.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône avec background circulaire
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        DarkBlue.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = DarkBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    color = GrayBorder,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.value,
                    fontSize = 16.sp,
                    color = Black,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    Projecct_MobileTheme {
        ProfileScreen()
    }
}

