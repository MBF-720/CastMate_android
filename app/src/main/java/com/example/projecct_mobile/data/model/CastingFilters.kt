package com.example.projecct_mobile.data.model

/**
 * Modèle de données pour les filtres de casting
 */
data class CastingFilters(
    val searchQuery: String = "",
    val selectedTypes: List<String> = emptyList(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val lieu: String? = null,
    val dateDebut: String? = null, // Format: "YYYY-MM-DD"
    val dateFin: String? = null, // Format: "YYYY-MM-DD"
    val age: String? = null // Format: "25-35 ans" ou "25" pour minimum
) {
    /**
     * Vérifie si des filtres sont actifs
     */
    fun hasActiveFilters(): Boolean {
        return searchQuery.isNotBlank() ||
                selectedTypes.isNotEmpty() ||
                minPrice != null ||
                maxPrice != null ||
                lieu != null ||
                dateDebut != null ||
                dateFin != null ||
                age != null
    }
    
    /**
     * Réinitialise tous les filtres
     */
    fun reset(): CastingFilters {
        return CastingFilters()
    }
}

