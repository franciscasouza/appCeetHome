package com.example.appceethome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.appceethome.ui.theme.AppCeetHomeTheme
import com.example.appceethome.ui.theme.Home.HomeScreen
import com.example.appceethome.ui.theme.Home.HomeViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var textToSpeech: TextToSpeech

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Text-to-Speech
        initializeTextToSpeech()

        // Verificar permissões
        if (checkMicrophonePermission()) {
            setupSpeechRecognition()
        }

        // Configurar a interface gráfica
        setContent {
            AppCeetHomeTheme {
                if (::viewModel.isInitialized) {
                    HomeScreen(viewModel = viewModel)
                } else {
                    // Mostra uma tela de carregamento ou mensagem de erro se o ViewModel não for inicializado
                    Toast.makeText(this, "Erro ao inicializar a interface", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("pt", "BR")
            } else {
                Toast.makeText(this, "Erro ao inicializar Text-to-Speech", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpeechRecognition() {
        // Inicializar SpeechRecognizer e Intent
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        // Inicializar ViewModel com dependências
        viewModel = HomeViewModel(speechRecognizer, recognizerIntent)

        // Iniciar a escuta contínua
        viewModel.startContinuousListening { command ->
            handleCommand(command)
        }
    }

    private fun handleCommand(command: String) {
        // Processar comando no ViewModel
        viewModel.processCommand(command) { response ->
            speak(response)
        }
    }

    private fun checkMicrophonePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMicrophonePermission()
            false
        } else {
            true
        }
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSpeechRecognition()
            } else {
                Toast.makeText(
                    this,
                    "Permissão para usar o microfone é necessária.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun speak(message: String) {
        if (::textToSpeech.isInitialized) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.shutdown()
        }
    }
}
