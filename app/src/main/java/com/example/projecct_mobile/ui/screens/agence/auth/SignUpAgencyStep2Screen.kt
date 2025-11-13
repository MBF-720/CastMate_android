package com.example.projecct_mobile.ui.screens.agence.auth

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.UploadFile
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.DarkBlueLight
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.LightGray
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.Red
import com.example.projecct_mobile.ui.theme.White
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun SignUpAgencyStep2Screen(
    onBackClick: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onFinishClick: (siteWeb: String, description: String, logoFile: File?, documentFile: File?, facebook: String?, instagram: String?) -> Unit = { _, _, _, _, _, _ -> }
) {
    var siteWeb by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var facebook by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var selectedLogoFile by remember { mutableStateOf<File?>(null) }
    var selectedDocumentFile by remember { mutableStateOf<File?>(null) }
    var logoImage by remember { mutableStateOf<ImageBitmap?>(null) }
    
    val context = LocalContext.current
    
    // File picker pour choisir un logo
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        
        val copiedFile = copyUriToCache(context, uri, "agency_logo")
        if (copiedFile != null) {
            selectedLogoFile = copiedFile
            // Prévisualiser le logo
            val bitmap = BitmapFactory.decodeFile(copiedFile.absolutePath)
            if (bitmap != null) {
                logoImage = bitmap.asImageBitmap()
                android.util.Log.d("SignUpAgencyStep2Screen", "✅ Logo sélectionné: ${copiedFile.name}")
            }
        } else {
            android.util.Log.e("SignUpAgencyStep2Screen", "❌ Impossible de copier le fichier logo")
        }
    }
    
    // File picker pour choisir un document PDF
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        
        val copiedFile = copyUriToCache(context, uri, "agency_document", ".pdf")
        if (copiedFile != null) {
            selectedDocumentFile = copiedFile
            android.util.Log.d("SignUpAgencyStep2Screen", "✅ Document sélectionné: ${copiedFile.name}, taille: ${copiedFile.length()} bytes")
        } else {
            android.util.Log.e("SignUpAgencyStep2Screen", "❌ Impossible de copier le fichier document")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(White.copy(alpha = 0.15f), CircleShape)
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
                            text = "Étape 2/2",
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
                        text = "Presque terminé !",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Quelques informations supplémentaires",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = White.copy(alpha = 0.9f)
                    )
                }
            }
        }

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
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = siteWeb,
                    onValueChange = { siteWeb = it },
                    label = {
                        Text(
                            "Site web (optionnel)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            "Description / présentation de l'agence *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    maxLines = 5,
                    minLines = 3
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Logo (optionnel)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable { logoPicker.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLogoFile != null) Color.Transparent else LightGray.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            GrayBorder.copy(alpha = 0.4f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoImage != null) {
                                Image(
                                    bitmap = logoImage!!,
                                    contentDescription = "Logo sélectionné",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Logo",
                                        tint = DarkBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ajouter un logo",
                                        fontSize = 12.sp,
                                        color = GrayBorder
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Document administratif *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable { documentPicker.launch("application/pdf") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDocumentFile != null) Color.Transparent else LightGray.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            GrayBorder.copy(alpha = 0.4f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedDocumentFile != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.UploadFile,
                                        contentDescription = "Document",
                                        tint = DarkBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Document téléchargé",
                                        fontSize = 12.sp,
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.UploadFile,
                                        contentDescription = "Document",
                                        tint = DarkBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Carte pro ou autorisation",
                                        fontSize = 12.sp,
                                        color = GrayBorder
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Réseaux sociaux
                Text(
                    text = "Réseaux sociaux (optionnel)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = facebook,
                    onValueChange = { facebook = it },
                    label = {
                        Text(
                            "Facebook",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    placeholder = {
                        Text(
                            "https://facebook.com/votre-page",
                            color = LightGray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = {
                        Text(
                            "Instagram",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    },
                    placeholder = {
                        Text(
                            "https://instagram.com/votre-compte",
                            color = LightGray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkBlue,
                        unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            onFinishClick(
                                siteWeb,
                                description,
                                selectedLogoFile,
                                selectedDocumentFile,
                                facebook.takeIf { it.isNotBlank() },
                                instagram.takeIf { it.isNotBlank() }
                            )
                        }
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
                    enabled = !isLoading && description.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = "Créer mon compte",
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
        android.util.Log.d("SignUpAgencyStep2Screen", "✅ Fichier copié vers: ${file.absolutePath}, taille: ${file.length()} bytes")
        file
    } catch (e: Exception) {
        android.util.Log.e("SignUpAgencyStep2Screen", "Erreur lors de la copie du fichier: ${e.message}", e)
        null
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpAgencyStep2ScreenPreview() {
    Projecct_MobileTheme {
        SignUpAgencyStep2Screen()
    }
}
