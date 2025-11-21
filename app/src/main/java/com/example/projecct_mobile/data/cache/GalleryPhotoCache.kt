package com.example.projecct_mobile.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Cache pour les photos de la galerie
 * Stocke les photos en mémoire et sur le disque pour éviter de les re-télécharger
 */
object GalleryPhotoCache {
    // Cache en mémoire (Map<fileId, ImageBitmap>)
    private val memoryCache = mutableMapOf<String, ImageBitmap>()
    
    // Obtenir le répertoire de cache pour les photos
    private fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, "gallery_photos")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }
    
    /**
     * Obtenir le fichier de cache pour une photo
     */
    private fun getCacheFile(context: Context, fileId: String): File {
        return File(getCacheDir(context), "photo_$fileId.jpg")
    }
    
    /**
     * Vérifier si une photo est en cache
     */
    fun isCached(context: Context, fileId: String): Boolean {
        // Vérifier d'abord le cache mémoire
        if (memoryCache.containsKey(fileId)) {
            return true
        }
        // Vérifier ensuite le cache disque
        return getCacheFile(context, fileId).exists()
    }
    
    /**
     * Obtenir une photo depuis le cache
     * Retourne null si la photo n'est pas en cache
     */
    suspend fun get(context: Context, fileId: String): ImageBitmap? = withContext(Dispatchers.IO) {
        // Vérifier d'abord le cache mémoire
        memoryCache[fileId]?.let { return@withContext it }
        
        // Vérifier ensuite le cache disque
        val cacheFile = getCacheFile(context, fileId)
        if (cacheFile.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
                bitmap?.let {
                    val imageBitmap = it.asImageBitmap()
                    // Mettre en cache mémoire pour les prochaines fois
                    memoryCache[fileId] = imageBitmap
                    return@withContext imageBitmap
                }
            } catch (e: Exception) {
                android.util.Log.e("GalleryPhotoCache", "❌ Erreur lecture cache disque pour $fileId: ${e.message}")
                // Supprimer le fichier corrompu
                cacheFile.delete()
            }
        }
        null
    }
    
    /**
     * Mettre une photo en cache
     */
    suspend fun put(context: Context, fileId: String, bytes: ByteArray): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            // Décoder le bitmap
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap?.let {
                val imageBitmap = it.asImageBitmap()
                
                // Mettre en cache mémoire
                memoryCache[fileId] = imageBitmap
                
                // Sauvegarder sur le disque
                val cacheFile = getCacheFile(context, fileId)
                FileOutputStream(cacheFile).use { output ->
                    it.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
                
                android.util.Log.d("GalleryPhotoCache", "✅ Photo $fileId mise en cache (${bytes.size} bytes)")
                return@withContext imageBitmap
            }
        } catch (e: Exception) {
            android.util.Log.e("GalleryPhotoCache", "❌ Erreur mise en cache pour $fileId: ${e.message}", e)
        }
        null
    }
    
    /**
     * Supprimer une photo du cache
     */
    suspend fun remove(context: Context, fileId: String) = withContext(Dispatchers.IO) {
        // Supprimer du cache mémoire
        memoryCache.remove(fileId)
        
        // Supprimer du cache disque
        val cacheFile = getCacheFile(context, fileId)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }
    
    /**
     * Vider le cache
     */
    suspend fun clear(context: Context) = withContext(Dispatchers.IO) {
        // Vider le cache mémoire
        memoryCache.clear()
        
        // Vider le cache disque
        val cacheDir = getCacheDir(context)
        cacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * Obtenir la taille du cache disque
     */
    fun getCacheSize(context: Context): Long {
        val cacheDir = getCacheDir(context)
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}

