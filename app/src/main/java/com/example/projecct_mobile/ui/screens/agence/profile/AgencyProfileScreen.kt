package com.example.projecct_mobile.ui.screens.agence.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.data.local.TokenManager
import com.example.projecct_mobile.data.model.ApiException
import com.example.projecct_mobile.data.model.User
import com.example.projecct_mobile.data.model.UserRole
import com.example.projecct_mobile.data.repository.UserRepository
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.launch

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

    val userRepository = remember(loadData) { if (loadData) UserRepository() else null }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
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

    LaunchedEffect(loadData) {
        if (!loadData) return@LaunchedEffect
        isLoading = true
        errorMessage = null
        try {
            val result = userRepository?.getCurrentUser()
            result?.onSuccess {
                user = it
                agencyName = it.nom ?: agencyName
                responsableName = it.prenom ?: responsableName
                agencyEmail = it.email
                agencyDescription = it.bio ?: agencyDescription
                isLoading = false
            }
            result?.onFailure { exception ->
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
            if (result == null) {
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Erreur: ${e.message}"
            isLoading = false
        }
    }

    val displayName = agencyName.ifBlank { "Agence" }
    val displayEmail = agencyEmail.ifBlank { "contact@agence.com" }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBlue, DarkBlueLight)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                            tint = White
                        )
                    }

                    Text(
                        text = "Profil Agence",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = "Agence",
                        tint = White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = "Logo agence",
                            tint = White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = displayName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = displayEmail,
                        fontSize = 14.sp,
                        color = White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(White)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
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
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                                        val currentId = tokenManager?.getUserIdSync()
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
                                    InfoRow(label = "Responsable", value = responsableName)
                                    InfoRow(label = "Créée le", value = user?.createdAt ?: "Non disponible")
                                    InfoRow(label = "Rôle", value = user?.role?.name ?: "RECRUTEUR")
                                    InfoRow(label = "Email", value = agencyEmail)
                                    InfoRow(label = "Téléphone", value = agencyPhone.ifBlank { "Non renseigné" })
                                    InfoRow(label = "Description", value = agencyDescription.ifBlank { "Non renseigné" })

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
private fun EditableField(label: String, value: String, onValueChange: (String) -> Unit, minLines: Int = 1, maxLines: Int = 1) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
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
