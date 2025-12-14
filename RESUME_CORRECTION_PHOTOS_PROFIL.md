# Résumé des corrections pour les photos de profil et galerie

## Problème identifié
Les photos de profil, galerie et CV ne s'affichent plus dans l'application Android car:
1. Le code télécharge manuellement les images en bytes via `downloadMedia()`
2. Le backend s'attend à ce que les images soient chargées via des URLs directes (`BASE_URL/media/{fileId}`) avec authentification JWT

## Solution implémentée

### 1. Mise à jour des modèles de données (`MediaModels.kt`)
- ✅ Ajout de toutes les propriétés manquantes: `photoMimeType`, `photoOriginalName`, `photoLength`, `documentMimeType`, `documentOriginalName`, `documentLength`
- ✅ Mise à jour de `MediaFileRef` avec toutes les propriétés: `originalName`, `length`, `uploadDate`

### 2. Création du helper d'URLs (`MediaUrlHelper.kt`)
- ✅ Création d'un helper centralisé pour construire les URLs des médias
- ✅ Méthodes pour photo de profil: `getProfilePhotoUrl(photoFileId/media)`
- ✅ Méthodes pour CV: `getCvUrl(documentFileId/media)`
- ✅ Méthodes pour galerie: `getGalleryPhotoUrl(fileId/mediaFileRef)` et `getGalleryPhotoUrls(gallery/media)`

### 3. Configuration Coil avec authentification JWT (`CoilConfig.kt`)
- ✅ Création d'un `ImageLoader` Coil personnalisé
- ✅ Intercepteur `AuthImageInterceptor` qui ajoute automatiquement le header `Authorization: Bearer {token}` à toutes les requêtes d'images
- ✅ Configuration avec cache mémoire et disque activé

### 4. Composants réutilisables (`MediaImages.kt`)
- ✅ `ProfilePhoto`: Composant pour afficher une photo de profil avec Coil
- ✅ `GalleryPhoto`: Composant pour afficher une photo de galerie avec Coil
- ✅ Support des placeholders, erreurs, loading spinners

### 5. Mise à jour de `ActorProfileScreen.kt`
- ✅ Import des nouveaux composants `ProfilePhoto` et `GalleryPhoto`
- ✅ Suppression de la variable `profileImage: ImageBitmap?` (Coil gère maintenant le chargement)
- ✅ Suppression du `LaunchedEffect` de téléchargement manuel de la photo de profil
- ✅ Remplacement de l'affichage `Image(bitmap = profileImage)` par `ProfilePhoto(photoFileId = acteurProfile?.media?.photoFileId)`
- ✅ Simplification de la galerie: `galleryFileIds: List<String>` au lieu de `galleryPhotos: List<Pair<String, ImageBitmap>>`
- ✅ Mise à jour du `LaunchedEffect` de la galerie pour extraire seulement les `fileIds`
- ✅ Mise à jour de `GallerySection` pour accepter `List<String>` et supprimer `isLoadingGallery`
- ✅ Mise à jour de `GalleryPhotoItem` pour utiliser `GalleryPhoto` avec Coil au lieu de `Image(bitmap)`
- ✅ Mise à jour de `FullScreenGalleryViewer` pour accepter `List<String>` et utiliser `GalleryPhoto` avec Coil

## Fichiers modifiés
1. ✅ `app/src/main/java/com/example/projecct_mobile/data/model/MediaModels.kt`
2. ✅ `app/src/main/java/com/example/projecct_mobile/utils/MediaUrlHelper.kt` (nouveau)
3. ✅ `app/src/main/java/com/example/projecct_mobile/utils/CoilConfig.kt` (nouveau)
4. ✅ `app/src/main/java/com/example/projecct_mobile/ui/components/MediaImages.kt` (nouveau)
5. ✅ `app/src/main/java/com/example/projecct_mobile/ui/screens/acteur/ActorProfileScreen.kt`

## Fichiers restant à mettre à jour
- ⏳ `app/src/main/java/com/example/projecct_mobile/ui/screens/profile/ProfileScreen.kt` (même logique que ActorProfileScreen)
- ⏳ `app/src/main/java/com/example/projecct_mobile/ui/screens/agence/profile/ActorProfileDetails.kt` (écran de détail acteur côté agence)

## Avantages de cette approche
1. **Performance**: Coil gère automatiquement le cache mémoire et disque
2. **Simplicité**: Plus besoin de télécharger manuellement les images en bytes
3. **Sécurité**: L'authentification JWT est ajoutée automatiquement par l'intercepteur
4. **Maintenabilité**: Code centralisé et réutilisable
5. **UX**: Loading spinners et placeholders automatiques

## Prochaines étapes
1. Mettre à jour `ProfileScreen.kt` avec la même approche
2. Mettre à jour `ActorProfileDetails.kt` avec la même approche
3. Tester l'affichage des photos de profil, galerie et CV
4. Vérifier que le téléchargement de CV fonctionne toujours (peut nécessiter une mise à jour similaire)

## Points d'attention
- Le code d'upload de photos/CV n'a pas été modifié (il fonctionne déjà)
- Les composants `ProfilePhoto` et `GalleryPhoto` gèrent automatiquement les cas où le `fileId` est null ou vide
- Le cache Coil est configuré pour ignorer les headers de cache du serveur (important pour les images authentifiées)

