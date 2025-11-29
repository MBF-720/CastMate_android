package com.example.projecct_mobile.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
// Import crop-kit - Package correct: com.tanishranjan.cropkit
import com.tanishranjan.cropkit.ImageCropper
import com.tanishranjan.cropkit.rememberCropController
import com.tanishranjan.cropkit.CropController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Écran de recadrage d'image réutilisable
 * 
 * @param imageFile Fichier image à recadrer
 * @param onCropComplete Callback appelé avec le fichier recadré
 * @param onCancel Callback appelé si l'utilisateur annule
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropScreen(
    imageFile: File,
    onCropComplete: (File) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Charger le bitmap depuis le fichier
    var bitmap by remember(imageFile) { 
        mutableStateOf<Bitmap?>(null) 
    }
    
    var isProcessing by remember { mutableStateOf(false) }
    
    // Charger le bitmap de manière asynchrone
    LaunchedEffect(imageFile) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeFile(imageFile.absolutePath)
            } catch (e: Exception) {
                android.util.Log.e("ImageCropScreen", "Erreur chargement bitmap: ${e.message}", e)
                null
            }
        }
    }
    
    // Créer le contrôleur de recadrage
    // rememberCropController est une fonction composable, elle doit être appelée directement
    val cropController: CropController? = bitmap?.let { 
        rememberCropController(bitmap = it)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recadrer la photo") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Zone de recadrage
            if (bitmap != null && cropController != null) {
                ImageCropper(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    cropController = cropController
                )
            } else {
                // Indicateur de chargement
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Chargement de l'image...")
                    }
                }
            }
            
            // Boutons d'action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isProcessing
                ) {
                    Text("Annuler")
                }
                
                Button(
                    onClick = {
                        if (cropController != null && bitmap != null) {
                            scope.launch {
                                isProcessing = true
                                try {
                                    // Recadrer l'image
                                    // Utiliser la méthode crop() du CropController
                                    val croppedBitmap = cropController.crop()
                                    
                                    // Sauvegarder le bitmap recadré dans un fichier
                                    val croppedFile = withContext(Dispatchers.IO) {
                                        saveBitmapToFile(
                                            context = context,
                                            bitmap = croppedBitmap,
                                            originalFile = imageFile
                                        )
                                    }
                                    
                                    croppedFile?.let { 
                                        onCropComplete(it)
                                    } ?: run {
                                        android.util.Log.e("ImageCropScreen", "Erreur lors de la sauvegarde du fichier recadré")
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ImageCropScreen", "Erreur recadrage: ${e.message}", e)
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = cropController != null && bitmap != null && !isProcessing
                ) {
                    if (isProcessing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Traitement...")
                        }
                    } else {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}

/**
 * Sauvegarde un Bitmap dans un fichier
 */
private fun saveBitmapToFile(
    context: android.content.Context,
    bitmap: Bitmap,
    originalFile: File
): File? {
    return try {
        // Créer un nouveau fichier pour l'image recadrée
        val croppedFile = File(
            context.cacheDir,
            "cropped_${System.currentTimeMillis()}.jpg"
        )
        
        // Compresser et sauvegarder
        FileOutputStream(croppedFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        
        android.util.Log.d("ImageCropScreen", "✅ Fichier recadré sauvegardé: ${croppedFile.absolutePath}")
        croppedFile
    } catch (e: Exception) {
        android.util.Log.e("ImageCropScreen", "❌ Erreur sauvegarde bitmap: ${e.message}", e)
        null
    }
}

