package com.example.projecct_mobile.utils

/**
 * Utilitaire pour valider les liens des réseaux sociaux
 */
object SocialLinkValidator {
    
    /**
     * Valide un lien Instagram
     * Formats acceptés :
     * - @username
     * - https://instagram.com/username
     * - https://www.instagram.com/username
     * - http://instagram.com/username
     * - http://www.instagram.com/username
     * - instagram.com/username
     */
    fun validateInstagram(link: String): ValidationResult {
        if (link.isBlank()) {
            return ValidationResult.Success
        }
        
        val trimmed = link.trim()
        
        // Format @username
        if (trimmed.startsWith("@")) {
            val username = trimmed.substring(1)
            return if (username.isNotBlank() && username.length >= 1 && username.length <= 30 && 
                       username.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Le nom d'utilisateur Instagram doit contenir entre 1 et 30 caractères (lettres, chiffres, points et underscores uniquement)")
            }
        }
        
        // Format URL
        val instagramPatterns = listOf(
            Regex("^https?://(www\\.)?instagram\\.com/[a-zA-Z0-9._]+/?$", RegexOption.IGNORE_CASE),
            Regex("^https?://(www\\.)?instagram\\.com/[a-zA-Z0-9._]+/.*$", RegexOption.IGNORE_CASE),
            Regex("^instagram\\.com/[a-zA-Z0-9._]+/?$", RegexOption.IGNORE_CASE),
            Regex("^instagram\\.com/[a-zA-Z0-9._]+/.*$", RegexOption.IGNORE_CASE)
        )
        
        val isValidUrl = instagramPatterns.any { it.matches(trimmed) }
        
        return if (isValidUrl) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Le lien Instagram doit être au format @username ou https://instagram.com/username")
        }
    }
    
    /**
     * Valide un lien YouTube
     * Formats acceptés :
     * - https://youtube.com/@channel
     * - https://www.youtube.com/@channel
     * - https://youtube.com/channel/...
     * - https://www.youtube.com/channel/...
     * - https://youtube.com/user/...
     * - https://www.youtube.com/user/...
     */
    fun validateYouTube(link: String): ValidationResult {
        if (link.isBlank()) {
            return ValidationResult.Success
        }
        
        val trimmed = link.trim()
        
        val youtubePatterns = listOf(
            Regex("^https?://(www\\.)?youtube\\.com/@[a-zA-Z0-9._-]+/?$", RegexOption.IGNORE_CASE),
            Regex("^https?://(www\\.)?youtube\\.com/channel/[a-zA-Z0-9_-]+/?$", RegexOption.IGNORE_CASE),
            Regex("^https?://(www\\.)?youtube\\.com/user/[a-zA-Z0-9_-]+/?$", RegexOption.IGNORE_CASE),
            Regex("^https?://(www\\.)?youtu\\.be/[a-zA-Z0-9_-]+/?$", RegexOption.IGNORE_CASE)
        )
        
        val isValidUrl = youtubePatterns.any { it.matches(trimmed) }
        
        return if (isValidUrl) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Le lien YouTube doit être au format https://youtube.com/@channel ou https://youtube.com/channel/...")
        }
    }
    
    /**
     * Valide un lien TikTok
     * Formats acceptés :
     * - @username
     * - https://tiktok.com/@username
     * - https://www.tiktok.com/@username
     * - http://tiktok.com/@username
     * - http://www.tiktok.com/@username
     */
    fun validateTikTok(link: String): ValidationResult {
        if (link.isBlank()) {
            return ValidationResult.Success
        }
        
        val trimmed = link.trim()
        
        // Format @username
        if (trimmed.startsWith("@")) {
            val username = trimmed.substring(1)
            return if (username.isNotBlank() && username.length >= 1 && username.length <= 24 && 
                       username.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Le nom d'utilisateur TikTok doit contenir entre 1 et 24 caractères (lettres, chiffres, points et underscores uniquement)")
            }
        }
        
        // Format URL
        val tiktokPatterns = listOf(
            Regex("^https?://(www\\.)?tiktok\\.com/@[a-zA-Z0-9._]+/?$", RegexOption.IGNORE_CASE),
            Regex("^https?://(www\\.)?tiktok\\.com/@[a-zA-Z0-9._]+/.*$", RegexOption.IGNORE_CASE)
        )
        
        val isValidUrl = tiktokPatterns.any { it.matches(trimmed) }
        
        return if (isValidUrl) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Le lien TikTok doit être au format @username ou https://tiktok.com/@username")
        }
    }
    
    /**
     * Normalise un lien Instagram pour l'envoi au backend
     * Convertit @username en https://instagram.com/username
     */
    fun normalizeInstagram(link: String): String {
        if (link.isBlank()) return link.trim()
        
        val trimmed = link.trim()
        if (trimmed.startsWith("@")) {
            val username = trimmed.substring(1)
            return "https://instagram.com/$username"
        }
        
        // Si c'est déjà une URL, la retourner telle quelle
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        
        // Si c'est instagram.com/username sans protocole, ajouter https://
        if (trimmed.startsWith("instagram.com/", ignoreCase = true)) {
            return "https://$trimmed"
        }
        
        return trimmed
    }
    
    /**
     * Normalise un lien TikTok pour l'envoi au backend
     * Convertit @username en https://tiktok.com/@username
     */
    fun normalizeTikTok(link: String): String {
        if (link.isBlank()) return link.trim()
        
        val trimmed = link.trim()
        if (trimmed.startsWith("@")) {
            val username = trimmed.substring(1)
            return "https://tiktok.com/@$username"
        }
        
        // Si c'est déjà une URL, la retourner telle quelle
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        
        // Si c'est tiktok.com/@username sans protocole, ajouter https://
        if (trimmed.startsWith("tiktok.com/@", ignoreCase = true)) {
            return "https://$trimmed"
        }
        
        return trimmed
    }
    
    /**
     * Normalise un lien YouTube pour l'envoi au backend
     * Retourne le lien tel quel s'il est valide
     */
    fun normalizeYouTube(link: String): String {
        if (link.isBlank()) return link.trim()
        
        val trimmed = link.trim()
        
        // Si c'est déjà une URL, la retourner telle quelle
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        
        // Si c'est youtube.com/... sans protocole, ajouter https://
        if (trimmed.startsWith("youtube.com/", ignoreCase = true) || 
            trimmed.startsWith("youtu.be/", ignoreCase = true)) {
            return "https://$trimmed"
        }
        
        return trimmed
    }
    
    /**
     * Résultat de validation
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}

