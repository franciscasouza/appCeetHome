package com.example.appceethome.ui.theme.Home

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appceethome.Domain.Repository.WebhookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val recognizerIntent: Intent
) : ViewModel() {

    private val repository = WebhookRepository()

    // Estados reativos
    var listening = mutableStateOf(false)
        private set

    var waitingForCommands = mutableStateOf(false)
        private set

    private var lastCommand: String? = null

    private val commandMap = mapOf(
        "status da plantinha" to { speak: (String) -> Unit ->
            fetchWebhookResponse("consultarStatusPlantinha", "status da plantinha", speak)
        },
        "temperatura do aquário" to { speak: (String) -> Unit ->
            fetchWebhookResponse("consultarTemperaturaAquario", "temperatura do aquário", speak)
        },
        "status composteira" to { speak: (String) -> Unit ->
            fetchWebhookResponse("consultarStatusComposteira", "status composteira", speak)
        },
        "alimentar o pet" to { speak: (String) -> Unit ->
            fetchWebhookResponse("alimentarPet", "alimentar o pet", speak)
        }
    )

    init {
        ensureContinuousListening()
    }

    // Iniciar escuta contínua
    fun startContinuousListening(onResult: (String) -> Unit) {
        if (!listening.value) {
            listening.value = true

            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("SpeechRecognizer", "Pronto para ouvir...")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val command = matches[0].lowercase(Locale.getDefault()).trim()
                        Log.d("SpeechRecognizer", "Comando detectado: $command")
                        onResult(command)
                    }
                }

                override fun onError(error: Int) {
                    Log.d("SpeechRecognizer", "Erro detectado: $error")
                    listening.value = false
                    if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                        restartListening()
                    }
                }

                override fun onEndOfSpeech() {
                    listening.value = false
                    restartListening()
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onBeginningOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            restartListening()
        }
    }

    private fun restartListening() {
        try {
            speechRecognizer.startListening(recognizerIntent)
            listening.value = true
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "Erro ao reiniciar escuta: ${e.message}")
            listening.value = false
        }
    }

    // Processar comandos reconhecidos
    fun processCommand(command: String, speak: (String) -> Unit) {
        if (command.contains("olá", ignoreCase = true)) {
            // Interação inicial
            handleGreeting(speak)
        } else if (waitingForCommands.value) {
            // Processar comandos do mapa
            val matchedCommand = commandMap.keys.find { keyword ->
                command.contains(keyword, ignoreCase = true)
            }

            if (matchedCommand != null) {
                commandMap[matchedCommand]?.invoke(speak)
            } else {
                speak("Desculpe, não reconheci esse comando. Tente novamente.")
            }
        } else {
            // Caso não tenha dito "Olá"
            speak("Por favor, diga 'Olá' para começar.")
        }
    }

    private fun handleGreeting(speak: (String) -> Unit) {
        resetInteractionState()
        Log.d("HomeViewModel", "Interação iniciada com 'Olá'.")
        speak("Olá, como posso te ajudar?")

        // Esperar pelo próximo comando
        viewModelScope.launch {
            delay(1000)
            restartListening()
        }
    }

    fun fetchWebhookResponse(endpoint: String, query: String, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    val call = repository.callWebhook(endpoint, query)
                    val result = call.execute()
                    if (result.isSuccessful) {
                        result.body()?.fulfillmentText ?: "Resposta vazia do webhook."
                    } else {
                        "Erro ao consultar o webhook: ${result.code()} - ${result.message()}"
                    }
                } catch (e: Exception) {
                    "Falha na comunicação com o webhook: ${e.message}"
                }
            }
            onResponse(response)
        }
    }

    private fun ensureContinuousListening() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (!listening.value) {
                    Log.d("HomeViewModel", "Reconhecimento de fala inativo. Reiniciando...")
                    restartListening()
                }
            }
        }
    }

    private fun resetInteractionState() {
        waitingForCommands.value = true
        lastCommand = null
        Log.d("HomeViewModel", "Interação reiniciada. Aguardando novos comandos.")
    }
}
