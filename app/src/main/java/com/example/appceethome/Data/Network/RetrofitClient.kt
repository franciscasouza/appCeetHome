package com.example.appceethome.Data.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://us-central1-ceetiot2024-42288.cloudfunctions.net/"

    val instance: WebhookService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(WebhookService::class.java)
    }
}


