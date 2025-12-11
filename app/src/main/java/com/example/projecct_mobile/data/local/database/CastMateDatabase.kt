package com.example.projecct_mobile.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de données Room pour l'application CastMate
 * Contient les conversations du chatbot et leurs messages
 */
@Database(
    entities = [
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CastMateDatabase : RoomDatabase() {
    
    abstract fun chatDao(): ChatDao
    
    companion object {
        @Volatile
        private var INSTANCE: CastMateDatabase? = null
        
        private const val DATABASE_NAME = "castmate_database"
        
        /**
         * Obtient l'instance singleton de la base de données
         */
        fun getInstance(context: Context): CastMateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CastMateDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // En dev: supprime et recrée la DB si version change
                    .build()
                
                INSTANCE = instance
                android.util.Log.d("CastMateDatabase", "✅ Base de données Room initialisée")
                instance
            }
        }
        
        /**
         * Ferme la base de données (utilisé lors des tests ou pour libérer des ressources)
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
            android.util.Log.d("CastMateDatabase", "🔒 Base de données Room fermée")
        }
    }
}

