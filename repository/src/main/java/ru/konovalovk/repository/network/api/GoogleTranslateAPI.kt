package ru.konovalovk.repository.network.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoogleTranslateAPI {
    @GET("translate_a/single?client=gtx&dt=bd&oe=UTF-8")
    suspend fun getWordTranslation(
        @Query("sl") originalWordLanguage: String,
        @Query("tl") translatedWordLanguage: String,
        @Query("q") originalWord : String
    ): ResponseBody

    @GET("translate_a/single?client=gtx&dt=t&oe=UTF-8")
    suspend fun getPhraseTranslation(
        @Query("sl") originalPhraseLanguage: String,
        @Query("tl") translatedPhraseLanguage: String,
        @Query("q") originalWord : String
    ): ResponseBody

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id")  movieId : String,
        @Query("api_key") apiKey : String
    )// : MovieDetailsAPI
}