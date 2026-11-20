package com.pminteractive.pmtours.network

import com.pminteractive.pmtours.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Interceptor que inyecta automáticamente la API Key en cada petición
    private val authInterceptor = Interceptor { chain ->
        val requestOriginal = chain.request()
        val requestConHeader = requestOriginal.newBuilder()
            .header("X-API-Key", BuildConfig.LINCE_API_KEY)
            .build()
        chain.proceed(requestConHeader)
    }

    // Interceptor para ver las peticiones y respuestas JSON en el Logcat de Android Studio
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Cliente HTTP con tiempos de espera configurados para evitar congelamientos
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // Instancia única de LinceApiService conectada a la URL centralizada en Gradle
    val apiService: LinceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.LINCE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LinceApiService::class.java)
    }
}