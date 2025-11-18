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
 * Cache pour les photos de la galerie d'un acteur.
 * Utilise un cache mémoire et un cache disque pour améliorer les performances.
 */
object GalleryPhotoCache {
    // Cache mémoire : Map<fileId, ImageBitmap>
    private val memoryCache = mutableMapOf<String, ImageBitmap>()

    /**
     * Récupère une photo depuis le cache (mémoire ou disque).
     * @param context Le contexte Android
     * @param fileId L'ID du fichier
     * @return L'ImageBitmap si trouvée, null sinon
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
     * Met une photo en cache (mémoire et disque).
     * @param context Le contexte Android
     * @param fileId L'ID du fichier
     * @param bytes Les bytes de l'image
     * @return L'ImageBitmap décodée
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
     * Supprime une photo du cache (mémoire et disque).
     * @param context Le contexte Android
     * @param fileId L'ID du fichier
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
     * Vide tout le cache (mémoire et disque).
     * @param context Le contexte Android
     */
    suspend fun clear(context: Context) = withContext(Dispatchers.IO) {
        // Vider le cache mémoire
        memoryCache.clear()
        
        // Vider le cache disque
        val cacheDir = File(context.cacheDir, "gallery_photos")
        if (cacheDir.exists()) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    /**
     * Retourne le fichier de cache pour un fileId donné.
     */
    private fun getCacheFile(context: Context, fileId: String): File {
        val cacheDir = File(context.cacheDir, "gallery_photos")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, "photo_$fileId.jpg")
    }
}

