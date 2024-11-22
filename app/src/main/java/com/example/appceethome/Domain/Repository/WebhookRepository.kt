package com.example.appceethome.Domain.Repository

import com.example.appceethome.Data.Model.WebhookRequest
import com.example.appceethome.Data.Model.WebhookResponse
import com.example.appceethome.Data.Network.RetrofitClient
import retrofit2.Call

class WebhookRepository {
    private val service = RetrofitClient.instance

    // Método genérico para chamar endpoints
    fun callWebhook(endpoint: String, query: String): Call<WebhookResponse> {
        val request = WebhookRequest(query)
        return when (endpoint) {
            "consultarStatusPlantinha" -> service.getStatus(request)
            // Adicione outros endpoints conforme necessário
            else -> throw IllegalArgumentException("Endpoint não suportado: $endpoint")
        }
    }
}
