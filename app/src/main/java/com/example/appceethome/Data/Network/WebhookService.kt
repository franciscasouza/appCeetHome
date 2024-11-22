package com.example.appceethome.Data.Network

import com.example.appceethome.Data.Model.WebhookRequest
import com.example.appceethome.Data.Model.WebhookResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface WebhookService {
    @POST("consultarStatusPlantinha")
    fun getStatus(@Body request: WebhookRequest): Call<WebhookResponse>
}
