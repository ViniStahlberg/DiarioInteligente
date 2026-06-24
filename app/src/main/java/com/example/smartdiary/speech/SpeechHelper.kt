package com.smartdiary.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onReady: () -> Unit,
    private val onEnd: () -> Unit
) {
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) = onReady()
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() = onEnd()
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) onResult(matches[0])
                else onError("Nenhum resultado reconhecido")
            }
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
            override fun onError(error: Int) {
                onError(when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de microfone negada"
                    SpeechRecognizer.ERROR_NETWORK -> "Erro de rede"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Não foi possível reconhecer a fala"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tempo esgotado"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecedor ocupado, tente novamente"
                    else -> "Erro desconhecido ($error)"
                })
            }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale agora...")
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() = recognizer?.stopListening()
    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }
}