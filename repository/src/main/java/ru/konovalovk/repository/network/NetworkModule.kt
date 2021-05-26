package ru.konovalovk.repository.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.ConnectionSpec
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import ru.konovalovk.repository.network.api.GoogleTranslateAPI
import java.util.*
import java.util.concurrent.TimeUnit


object NetworkModule {
    private val googleUrl = "https://translate.google.com/"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val contentType = "application/json".toMediaType()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addNetworkInterceptor(loggingInterceptor)
        .build()

    private val retrofitMovieBuilder = Retrofit.Builder()
        .baseUrl(googleUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    val googleApi : GoogleTranslateAPI = retrofitMovieBuilder.create()
}