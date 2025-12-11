# 📝 Comment créer le fichier .env dans Android Studio

## ⚠️ IMPORTANT : Ce fichier est OBLIGATOIRE pour que l'application fonctionne

Le fichier `.env` contient vos clés API et **NE DOIT PAS** être commité sur GitHub.

## 🚀 Étapes pour créer le fichier .env

### 1. Ouvrir le dossier assets dans Android Studio

1. Dans Android Studio, ouvrez le projet `CastMate_android_3`
2. Dans l'explorateur de fichiers (à gauche), naviguez vers :
   ```
   app → src → main → assets
   ```

### 2. Créer le fichier .env

**Option A : Via l'interface Android Studio**
1. Clic droit sur le dossier `assets`
2. Sélectionnez **New → File**
3. Nommez le fichier : `.env` (avec le point au début)
4. Cliquez sur **OK**

**Option B : Via l'explorateur Windows**
1. Ouvrez l'explorateur Windows
2. Naviguez vers : `C:\Users\Negza\Desktop\CastMate_android_3\app\src\main\assets\`
3. Créez un nouveau fichier texte
4. Renommez-le en `.env` (avec le point au début)

### 3. Ajouter le contenu dans le fichier .env

Ouvrez le fichier `.env` que vous venez de créer et copiez-collez ce contenu :

```env
# Clés API pour l'application CastMate
# ⚠️ NE PAS COMMITER CE FICHIER SUR GITHUB ⚠️

# Clé API Groq pour l'analyse vidéo et le chatbot
# Obtenez votre clé sur https://console.groq.com/
GROQ_API_KEY=votre_cle_groq_ici
# Clé API Gemini pour le chatbot agence (optionnel, si utilisé)
# Obtenez votre clé sur https://makersuite.google.com/app/apikey
GEMINI_API_KEY=votre_cle_gemini_ici
```

### 4. Vérifier que le fichier est bien créé

Le fichier doit se trouver à cet emplacement :
```
app/src/main/assets/.env
```

## ✅ Vérification

1. Dans Android Studio, le fichier `.env` doit apparaître dans le dossier `assets`
2. Compilez le projet : **Build → Make Project**
3. Si tout est correct, l'application devrait démarrer sans erreur

## 🔒 Sécurité

- ✅ Le fichier `.env` est déjà dans `.gitignore` (il ne sera pas commité)
- ✅ Ne partagez jamais ce fichier publiquement
- ✅ Si vous changez de clé API, modifiez simplement le fichier `.env`

## ❌ Si vous avez une erreur au démarrage

Si vous voyez cette erreur :
```
IllegalStateException: Impossible de charger GROQ_API_KEY depuis .env
```

Cela signifie que :
1. Le fichier `.env` n'existe pas dans `app/src/main/assets/`
2. Le fichier existe mais ne contient pas `GROQ_API_KEY=...`
3. Le fichier a une mauvaise syntaxe

**Solution** : Vérifiez que le fichier `.env` existe bien et contient les lignes `GROQ_API_KEY=...` et `GEMINI_API_KEY=...`

