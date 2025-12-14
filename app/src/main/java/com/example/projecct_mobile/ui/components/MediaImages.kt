package com.example.projecct_mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.projecct_mobile.R
import com.example.projecct_mobile.utils.CoilConfig
import com.example.projecct_mobile.utils.MediaUrlHelper

/**
 * Composant pour afficher une photo de profil avec Coil
 */
@Composable
fun ProfilePhoto(
    photoFileId: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    error: Painter? = null
) {
    val context = LocalContext.current
    val imageUrl = MediaUrlHelper.getProfilePhotoUrl(photoFileId)
    
    if (imageUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Photo de profil",
            imageLoader = CoilConfig.createImageLoader(context),
            modifier = modifier.clip(shape),
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            },
            error = {
                if (error != null) {
                    Image(
                        painter = error,
                        contentDescription = "Photo de profil par défaut",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Photo de profil par défaut",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )
    } else {
        if (placeholder != null) {
            Image(
                painter = placeholder,
                contentDescription = "Photo de profil par défaut",
                modifier = modifier.clip(shape),
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = modifier.clip(shape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Photo de profil par défaut",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Composant pour afficher une photo de galerie avec Coil
 */
@Composable
fun GalleryPhoto(
    fileId: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val imageUrl = MediaUrlHelper.getGalleryPhotoUrl(fileId)
    
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Photo de galerie",
        imageLoader = CoilConfig.createImageLoader(context),
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        },
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("❌")
            }
        }
    )
}

