package com.example.projecct_mobile.ui.utils

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.projecct_mobile.data.api.ApiClient

/**
 * Utilitaire pour configurer Coil avec l'authentification JWT
 */
object CoilImageLoader {
    
    private var imageLoader: ImageLoader? = null
    
    /**
     * Initialise et retourne l'ImageLoader Coil configuré avec l'authentification
     */
    fun getImageLoader(context: Context): ImageLoader {
        if (imageLoader == null) {
            try {
                val okHttpClient = ApiClient.getOkHttpClient()
                Log.d("CoilImageLoader", "✅ Création de l'ImageLoader avec OkHttpClient authentifié")
                
                imageLoader = ImageLoader.Builder(context)
                    .okHttpClient(okHttpClient)
                    .memoryCache {
                        MemoryCache.Builder(context)
                            .maxSizePercent(0.25)
                            .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizePercent(0.02)
                            .build()
                    }
                    .respectCacheHeaders(false) // Ignorer les headers de cache du serveur si nécessaire
                    .allowHardware(true) // Permettre le rendu matériel pour de meilleures performances
                    .build()
                
                Log.d("CoilImageLoader", "✅ ImageLoader créé avec succès")
            } catch (e: Exception) {
                Log.e("CoilImageLoader", "❌ Erreur lors de la création de l'ImageLoader: ${e.message}", e)
                // Fallback : créer un ImageLoader sans authentification en cas d'erreur
                imageLoader = ImageLoader.Builder(context)
                    .build()
            }
        }
        return imageLoader!!
    }
}

