package com.example.projecct_mobile.ui.screens.auth.signup

import android.content.Context
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.example.projecct_mobile.ui.theme.Black
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.LightBlue
import com.example.projecct_mobile.ui.theme.LightGray
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Étape 2 - Informations professionnelles pour l'inscription ACTEUR
 */
@Composable
fun SignUpActorStep2Screen(
    onBackClick: () -> Unit = {},
    onNextClick: (anneesExperience: String, cvUrl: String?, instagram: String, youtube: String, tiktok: String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current

    var anneesExperience by remember { mutableStateOf("") }
    var cvUrl by remember { mutableStateOf<String?>(null) }
    var cvFileName by remember { mutableStateOf<String?>(null) }
    var instagram by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult

        val copiedFile = copyUriToCache(context, uri, "cv_document", ".pdf")
        if (copiedFile != null) {
            cvUrl = copiedFile.absolutePath
            cvFileName = resolveFileName(context, uri) ?: copiedFile.name
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // En-tête moderne
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(White.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "Étape 2/3",
                            fontSize = 14.sp,
                            color = White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Votre expérience",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Informations professionnelles",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Contenu moderne
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    spotColor = DarkBlue.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Années d'expérience
                OutlinedTextField(
                    value = anneesExperience,
                    onValueChange = { anneesExperience = it },
                    label = { Text("Années d'expérience *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // CV Upload
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pdfPicker.launch("application/pdf") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cvUrl != null) LightBlue else LightGray
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Upload CV",
                                tint = DarkBlue
                            )
                            Column {
                                Text(
                                    text = if (cvUrl != null) "CV téléchargé" else "Télécharger votre CV (PDF)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DarkBlue
                                )
                                if (cvUrl == null) {
                                    Text(
                                        text = "Fichier PDF uniquement",
                                        fontSize = 12.sp,
                                        color = GrayBorder
                                    )
                                } else {
                                    cvFileName?.let { name ->
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            color = DarkBlue.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Réseaux sociaux (optionnels)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Instagram
                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = { Text("Instagram") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("📷", fontSize = 20.sp)
                    }
                )

                // YouTube
                OutlinedTextField(
                    value = youtube,
                    onValueChange = { youtube = it },
                    label = { Text("YouTube") },
                    placeholder = { Text("https://youtube.com/@channel") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("▶️", fontSize = 20.sp)
                    }
                )

                // TikTok
                OutlinedTextField(
                    value = tiktok,
                    onValueChange = { tiktok = it },
                    label = { Text("TikTok") },
                    placeholder = { Text("@username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Text("🎵", fontSize = 20.sp)
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                formError?.let { message ->
                    Text(
                        text = message,
                        color = Red,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        val experienceValue = anneesExperience.toIntOrNull()
                        if (experienceValue == null) {
                            formError = "Veuillez saisir un nombre d'années d'expérience valide"
                            return@Button
                        }
                        if (experienceValue < 0 || experienceValue > 60) {
                            formError = "L'expérience doit être comprise entre 0 et 60 ans"
                            return@Button
                        }

                        val urlFields = listOf(
                            youtube to "YouTube",
                            instagram to "Instagram",
                            tiktok to "TikTok"
                        )

                        urlFields.forEach { (value, label) ->
                            if (value.isNotBlank()) {
                                val sanitized = value.trim()
                                val isUrl = sanitized.startsWith("http://") || sanitized.startsWith("https://")
                                val matchesPattern = Patterns.WEB_URL.matcher(sanitized).matches()
                                val isHandle = sanitized.startsWith("@") && sanitized.length >= 3
                                if (!(isUrl && matchesPattern) && !isHandle) {
                                    formError = "$label doit être un lien valide ou commencer par @"
                                    return@Button
                                }
                            }
                        }

                        formError = null
                        onNextClick(
                            experienceValue.toString(),
                            cvUrl,
                            instagram.trim(),
                            youtube.trim(),
                            tiktok.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = DarkBlue.copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue,
                        disabledContainerColor = GrayBorder.copy(alpha = 0.5f)
                    ),
                    enabled = anneesExperience.isNotBlank()
                ) {
                    Text(
                        text = "Continuer",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpActorStep2ScreenPreview() {
    Projecct_MobileTheme {
        SignUpActorStep2Screen()
    }
}

private fun copyUriToCache(context: Context, uri: Uri, prefix: String, forcedExtension: String): File? {
    return try {
        val resolver = context.contentResolver
        val inputStream: InputStream = resolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "$prefix-${'$'}{System.currentTimeMillis()}$forcedExtension")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

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

