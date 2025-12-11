package com.example.projecct_mobile.ui.screens.map

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.projecct_mobile.ui.theme.DarkBlue
import com.example.projecct_mobile.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastingMapScreen(
    location: String,
    castingTitle: String = "",
    onBackClick: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var coordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val scope = rememberCoroutineScope()

    // Géocoder l'adresse pour obtenir les coordonnées
    LaunchedEffect(location) {
        if (location.isNotBlank()) {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val coords = geocodeAddress(location)
                    coordinates = coords
                    if (coords == null) {
                        errorMessage = "Impossible de trouver l'emplacement: $location"
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CastingMapScreen", "Erreur géocodage: ${e.message}", e)
                    errorMessage = "Erreur lors de la recherche de l'emplacement"
                } finally {
                    isLoading = false
                }
            }
        } else {
            errorMessage = "Aucune adresse fournie"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (castingTitle.isNotBlank()) "Localisation: $castingTitle" else "Localisation du casting",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = DarkBlue)
                            Text(
                                text = "Recherche de l'emplacement...",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp
                            )
                            Text(
                                text = errorMessage ?: "Erreur inconnue",
                                color = Color(0xFF666666),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = location,
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkBlue
                                )
                            ) {
                                Text("Retour")
                            }
                        }
                    }
                }
                coordinates != null -> {
                    // Afficher la carte avec OpenStreetMap et Leaflet
                    val (lat, lon) = coordinates!!
                    val htmlContent = generateMapHTML(location, lat, lon)
                    
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                loadDataWithBaseURL(
                                    "https://www.openstreetmap.org/",
                                    htmlContent,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Géocode une adresse en utilisant l'API Nominatim (OpenStreetMap) - GRATUIT
 */
private suspend fun geocodeAddress(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    return@withContext try {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val url = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"
        
        val connection = java.net.URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "CastMate Android App")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val response = connection.getInputStream().bufferedReader().use { it.readText() }
        val jsonArray = org.json.JSONArray(response)
        
        if (jsonArray.length() > 0) {
            val firstResult = jsonArray.getJSONObject(0)
            val lat = firstResult.getString("lat").toDouble()
            val lon = firstResult.getString("lon").toDouble()
            android.util.Log.d("CastingMapScreen", "✅ Coordonnées trouvées: $lat, $lon pour: $address")
            Pair(lat, lon)
        } else {
            android.util.Log.w("CastingMapScreen", "⚠️ Aucun résultat pour: $address")
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("CastingMapScreen", "❌ Erreur géocodage: ${e.message}", e)
        null
    }
}

/**
 * Génère le HTML pour afficher une carte OpenStreetMap avec Leaflet
 */
private fun generateMapHTML(location: String, lat: Double, lon: Double): String {
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            body {
                margin: 0;
                padding: 0;
                font-family: Arial, sans-serif;
            }
            #map {
                width: 100%;
                height: 100vh;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            // Initialiser la carte
            var map = L.map('map').setView([$lat, $lon], 15);
            
            // Ajouter la couche de tuiles OpenStreetMap
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors',
                maxZoom: 19
            }).addTo(map);
            
            // Ajouter un marqueur à l'emplacement
            var marker = L.marker([$lat, $lon]).addTo(map);
            marker.bindPopup('<b>$location</b>').openPopup();
            
            // Centrer la carte sur le marqueur
            map.setView([$lat, $lon], 15);
        </script>
    </body>
    </html>
    """.trimIndent()
}

