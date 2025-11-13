package com.example.projecct_mobile.ui.screens.agence.casting

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.GrayBorder
import com.example.projecct_mobile.ui.theme.LightBlue
import com.example.projecct_mobile.ui.theme.LightGray
import com.example.projecct_mobile.ui.theme.Projecct_MobileTheme
import com.example.projecct_mobile.ui.theme.White
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCastingScreen(
    onBackClick: () -> Unit = {},
    onSaveCastingClick: (
        titre: String,
        descriptionRole: String,
        synopsis: String,
        dateDebut: String,
        dateFin: String,
        prix: Double,
        types: List<String>,
        age: String?,
        conditions: String,
        lieu: String,
        afficheFile: File?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _ -> },
    externalErrorMessage: String? = null
) {
    var titre by remember { mutableStateOf("") }
    var descriptionRole by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf("") }
    var dateFin by remember { mutableStateOf("") }
    var prix by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var ageFrom by remember { mutableStateOf(18) }
    var ageTo by remember { mutableStateOf(35) }
    var conditions by remember { mutableStateOf("") }
    var lieu by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Types disponibles pour les castings
    val availableTypes = listOf("Cinéma", "Télévision", "Théâtre", "Publicité", "Court-métrage", "Documentaire")
    
    // États pour l'affiche
    var selectedAfficheFile by remember { mutableStateOf<File?>(null) }
    var afficheImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current
    val activity = remember(context) { 
        when {
            context is Activity -> context
            context is android.content.ContextWrapper -> {
                var ctx: Context? = context
                while (ctx != null) {
                    if (ctx is Activity) return@remember ctx
                    ctx = (ctx as? android.content.ContextWrapper)?.baseContext
                }
                null
            }
            else -> null
        }
    }
    
    // Formater les dates
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    // Callback pour afficher le date picker de début
    val onDateDebutClick: () -> Unit = {
        android.util.Log.d("CreateCastingScreen", "📅 Clic sur date début")
        val calendar = Calendar.getInstance()
        // Si une date existe déjà, la parser
        if (dateDebut.isNotBlank()) {
            try {
                val parsedDate = dateFormat.parse(dateDebut)
                if (parsedDate != null) {
                    calendar.time = parsedDate
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCastingScreen", "Erreur parsing date début: ${e.message}")
            }
        }
        
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        android.util.Log.d("CreateCastingScreen", "📅 Affichage DatePickerDialog début: $initialYear-$initialMonth-$initialDay")
        android.util.Log.d("CreateCastingScreen", "📅 Context type: ${context.javaClass.simpleName}, Activity: ${activity?.javaClass?.simpleName}")
        
        val dialogContext = activity ?: context
        
        try {
            DatePickerDialog(
                dialogContext,
                { _, year, month, dayOfMonth ->
                    android.util.Log.d("CreateCastingScreen", "✅ Date début sélectionnée: $year-$month-$dayOfMonth")
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    dateDebut = dateFormat.format(selectedCalendar.time)
                    android.util.Log.d("CreateCastingScreen", "✅ Date début formatée: $dateDebut")
                },
                initialYear,
                initialMonth,
                initialDay
            ).show()
            android.util.Log.d("CreateCastingScreen", "✅ DatePickerDialog.show() appelé pour début")
        } catch (e: Exception) {
            android.util.Log.e("CreateCastingScreen", "❌ Erreur affichage DatePickerDialog début: ${e.message}", e)
        }
    }
    
    // Callback pour afficher le date picker de fin
    val onDateFinClick: () -> Unit = {
        android.util.Log.d("CreateCastingScreen", "📅 Clic sur date fin")
        val calendar = Calendar.getInstance()
        // Si une date existe déjà, la parser
        if (dateFin.isNotBlank()) {
            try {
                val parsedDate = dateFormat.parse(dateFin)
                if (parsedDate != null) {
                    calendar.time = parsedDate
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateCastingScreen", "Erreur parsing date fin: ${e.message}")
            }
        }
        
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        android.util.Log.d("CreateCastingScreen", "📅 Affichage DatePickerDialog fin: $initialYear-$initialMonth-$initialDay")
        android.util.Log.d("CreateCastingScreen", "📅 Context type: ${context.javaClass.simpleName}, Activity: ${activity?.javaClass?.simpleName}")
        
        val dialogContext = activity ?: context
        
        try {
            DatePickerDialog(
                dialogContext,
                { _, year, month, dayOfMonth ->
                    android.util.Log.d("CreateCastingScreen", "✅ Date fin sélectionnée: $year-$month-$dayOfMonth")
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    dateFin = dateFormat.format(selectedCalendar.time)
                    android.util.Log.d("CreateCastingScreen", "✅ Date fin formatée: $dateFin")
                },
                initialYear,
                initialMonth,
                initialDay
            ).show()
            android.util.Log.d("CreateCastingScreen", "✅ DatePickerDialog.show() appelé pour fin")
        } catch (e: Exception) {
            android.util.Log.e("CreateCastingScreen", "❌ Erreur affichage DatePickerDialog fin: ${e.message}", e)
        }
    }
    
    // File picker pour l'affiche
    val affichePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val copiedFile = copyUriToCache(context, uri, "casting_affiche")
        if (copiedFile != null) {
            selectedAfficheFile = copiedFile
            val bitmap = BitmapFactory.decodeFile(copiedFile.absolutePath)
            if (bitmap != null) {
                afficheImage = bitmap.asImageBitmap()
                android.util.Log.d("CreateCastingScreen", "✅ Affiche sélectionnée: ${copiedFile.name}")
            }
        } else {
            android.util.Log.e("CreateCastingScreen", "❌ Impossible de copier le fichier affiche")
            errorMessage = "Impossible de copier le fichier affiche. Veuillez réessayer."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nouveau Casting",
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(White.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue,
                    titleContentColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(androidx.compose.ui.graphics.Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            OutlinedTextField(
                value = titre,
                onValueChange = { titre = it },
                label = {
                    Text(
                        "Titre du casting *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = descriptionRole,
                onValueChange = { descriptionRole = it },
                label = {
                    Text(
                        "Description du rôle *",
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

            OutlinedTextField(
                value = synopsis,
                onValueChange = { synopsis = it },
                label = {
                    Text(
                        "Synopsis *",
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

            // Date de début avec date picker
            OutlinedTextField(
                value = dateDebut,
                onValueChange = { },
                readOnly = true,
                label = {
                    Text(
                        "Date de début *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                placeholder = { Text("Sélectionner une date", color = GrayBorder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateDebutClick() },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { onDateDebutClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Sélectionner la date",
                            tint = DarkBlue
                        )
                    }
                },
                singleLine = true
            )

            // Date de fin avec date picker
            OutlinedTextField(
                value = dateFin,
                onValueChange = { },
                readOnly = true,
                label = {
                    Text(
                        "Date de fin *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                placeholder = { Text("Sélectionner une date", color = GrayBorder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateFinClick() },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = { onDateFinClick() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = "Sélectionner la date",
                            tint = DarkBlue
                        )
                    }
                },
                singleLine = true
            )
            
            OutlinedTextField(
                value = prix,
                onValueChange = { prix = it },
                label = {
                    Text(
                        "Prix *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                placeholder = { Text("ex: 5000", color = GrayBorder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Section Types de casting (un seul choix)
            Text(
                text = "Type de casting (optionnel)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, RoundedCornerShape(12.dp))
                    .border(1.dp, GrayBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTypes.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedType = if (selectedType == type) null else type
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = if (selectedType == type) null else type
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = DarkBlue,
                                unselectedColor = GrayBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
            
            // Section Tranche d'âge avec compteurs
            Text(
                text = "Tranche d'âge (optionnel)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compteur "De"
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "De",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .background(White, RoundedCornerShape(12.dp))
                            .border(1.dp, GrayBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (ageFrom > 0) ageFrom-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Diminuer",
                                tint = DarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "$ageFrom",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { if (ageFrom < ageTo) ageFrom++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Augmenter",
                                tint = DarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Compteur "À"
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "À",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .background(White, RoundedCornerShape(12.dp))
                            .border(1.dp, GrayBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (ageTo > ageFrom) ageTo-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = "Diminuer",
                                tint = DarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "$ageTo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { ageTo++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Augmenter",
                                tint = DarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = lieu,
                onValueChange = { lieu = it },
                label = {
                    Text(
                        "Lieu *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                placeholder = { Text("ex: Tunis, Ariana", color = GrayBorder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkBlue,
                    unfocusedBorderColor = GrayBorder.copy(alpha = 0.4f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                ),
                singleLine = true
            )
            
            OutlinedTextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = {
                    Text(
                        "Conditions *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                placeholder = { Text("ex: Disponibilité totale requise", color = GrayBorder) },
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
            
            // Section Affiche
            Text(
                text = "Affiche (optionnel)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        affichePicker.launch("image/*")
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (afficheImage != null) LightBlue else LightGray
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (afficheImage != null) {
                        Image(
                            bitmap = afficheImage!!,
                            contentDescription = "Affiche sélectionnée",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Affiche",
                                tint = DarkBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ajouter une affiche",
                                fontSize = 14.sp,
                                color = GrayBorder
                            )
                        }
                    }
                }
            }
            
            // Message d'erreur
            (errorMessage ?: externalErrorMessage)?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF0000),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    errorMessage = null
                    // Validation
                    if (titre.isBlank()) {
                        errorMessage = "Le titre est requis"
                        return@Button
                    }
                    if (descriptionRole.isBlank()) {
                        errorMessage = "La description du rôle est requise"
                        return@Button
                    }
                    if (synopsis.isBlank()) {
                        errorMessage = "Le synopsis est requis"
                        return@Button
                    }
                    if (dateDebut.isBlank()) {
                        errorMessage = "La date de début est requise"
                        return@Button
                    }
                    if (dateFin.isBlank()) {
                        errorMessage = "La date de fin est requise"
                        return@Button
                    }
                    if (prix.isBlank()) {
                        errorMessage = "Le prix est requis"
                        return@Button
                    }
                    val prixValue = prix.toDoubleOrNull()
                    if (prixValue == null || prixValue < 0) {
                        errorMessage = "Le prix doit être un nombre positif"
                        return@Button
                    }
                    if (conditions.isBlank()) {
                        errorMessage = "Les conditions sont requises"
                        return@Button
                    }
                    if (lieu.isBlank()) {
                        errorMessage = "Le lieu est requis"
                        return@Button
                    }
                    
                    isLoading = true
                    // Construire la chaîne d'âge
                    val ageString = if (ageFrom > 0 || ageTo > 0) "$ageFrom-$ageTo ans" else null
                    // Convertir selectedType en liste (ou null si aucun sélectionné)
                    val typesList = selectedType?.let { listOf(it) } ?: emptyList()
                    
                    onSaveCastingClick(
                        titre,
                        descriptionRole,
                        synopsis,
                        dateDebut,
                        dateFin,
                        prixValue,
                        typesList,
                        ageString,
                        conditions,
                        lieu,
                        selectedAfficheFile
                    )
                    isLoading = false
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
                enabled = !isLoading && titre.isNotBlank() && descriptionRole.isNotBlank() &&
                    synopsis.isNotBlank() && dateDebut.isNotBlank() && dateFin.isNotBlank() &&
                    prix.isNotBlank() && conditions.isNotBlank() && lieu.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Publier le Casting",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Copie un URI vers le cache de l'application
 */
private fun copyUriToCache(context: Context, uri: Uri, prefix: String): File? {
    return try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val extension = when {
            mimeType?.contains("png") == true -> ".png"
            mimeType?.contains("jpg") == true -> ".jpg"
            mimeType?.contains("jpeg") == true -> ".jpg"
            else -> ".jpg"
        }
        
        val inputStream: InputStream? = resolver.openInputStream(uri)
        if (inputStream == null) return null
        
        val file = File(context.cacheDir, "$prefix-${System.currentTimeMillis()}$extension")
        FileOutputStream(file).use { output ->
            inputStream.use { input -> input.copyTo(output) }
        }
        android.util.Log.d("CreateCastingScreen", "✅ Fichier copié vers: ${file.absolutePath}, taille: ${file.length()} bytes")
        file
    } catch (e: Exception) {
        android.util.Log.e("CreateCastingScreen", "Erreur copie fichier: ${e.message}", e)
        null
    }
}

@Preview(showBackground = true)
@Composable
fun CreateCastingScreenPreview() {
    Projecct_MobileTheme {
        CreateCastingScreen()
    }
}
