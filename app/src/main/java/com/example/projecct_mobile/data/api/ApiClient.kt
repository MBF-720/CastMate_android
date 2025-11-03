package com.example.projecct_mobile.data.api

import android.content.Context
import com.example.projecct_mobile.data.local.TokenManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuration du client API Retrofit
 */
object ApiClient {
    
    private const val BASE_URL = "https://cast-mate.vercel.app/"
    
    // Instance singleton du client Retrofit
    private var retrofit: Retrofit? = null
    
    // Instance du TokenManager
    private var tokenManager: TokenManager? = null
    
    /**
     * Initialise le client API avec le contexte de l'application
     */
    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
        
        val gson: Gson = GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        // Intercepteur de logging (pour le debug)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // En production, utiliser Level.NONE
        }
        
        // Configuration d'OkHttp avec les intercepteurs
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager!!))
            .addInterceptor(ErrorInterceptor(tokenManager!!, gson))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        // Configuration de Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * Récupère l'instance du TokenManager
     */
    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("ApiClient n'est pas initialisé. Appelez initialize() d'abord.")
    }
    
    /**
     * Récupère l'instance Retrofit
     */
    private fun getRetrofit(): Retrofit {
        return retrofit ?: throw IllegalStateException("ApiClient n'est pas initialisé. Appelez initialize() d'abord.")
    }
    
    /**
     * Crée une instance du service d'authentification
     */
    fun getAuthService(): AuthApiService {
        return getRetrofit().create(AuthApiService::class.java)
    }
    
    /**
     * Crée une instance du service des castings
     */
    fun getCastingService(): CastingApiService {
        return getRetrofit().create(CastingApiService::class.java)
    }
    
    /**
     * Crée une instance du service des utilisateurs
     */
    fun getUserService(): UserApiService {
        return getRetrofit().create(UserApiService::class.java)
    }
}

