@file:OptIn(ExperimentalForeignApi::class)

package com.mjproductions.fiscal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.setActive
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue


private const val AUTH_STATUS_AUTHORIZED = 3L

class IosSpeechRecognizer : SpeechRecognizer {
    private val recognizer = SFSpeechRecognizer()
    private val audioEngine = AVAudioEngine()
    private var task: SFSpeechRecognitionTask? = null
    private var request: SFSpeechAudioBufferRecognitionRequest? = null

    override fun start(onResult: (String) -> Unit, onError: (String) -> Unit) {
        if (recognizer == null) {
            onError("Speech recognizer unavailable for this locale")
            return
        }
        SFSpeechRecognizer.requestAuthorization { status ->
            dispatch_async(dispatch_get_main_queue()) {
                if (status.value != AUTH_STATUS_AUTHORIZED) {
                    onError("Speech recognition not authorized")
                    return@dispatch_async
                }
                if (recognizer.isAvailable() != true) {
                    onError("Speech recognizer not available right now")
                    return@dispatch_async
                }
                beginRecognition(onResult, onError)
            }
        }
    }

    private fun beginRecognition(onResult: (String) -> Unit, onError: (String) -> Unit) {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryRecord, null)
            session.setMode(AVAudioSessionModeMeasurement, null)
            session.setActive(true, null)

            val req = SFSpeechAudioBufferRecognitionRequest().also { request = it }
            req.shouldReportPartialResults = false

            val inputNode = audioEngine.inputNode
            val format = inputNode.outputFormatForBus(0u)

            inputNode.installTapOnBus(0u, 1024u, format) { buffer, _ ->
                buffer?.let { req.appendAudioPCMBuffer(it) }
            }

            audioEngine.prepare()
            audioEngine.startAndReturnError(null)

            task = recognizer.recognitionTaskWithRequest(req) { result, error ->
                if (result != null && result.isFinal()) {
                    onResult(result.bestTranscription.formattedString)
                    stop()
                }
                if (error != null) {
                    onError(error.localizedDescription)
                    stop()
                }
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown speech error")
        }
    }
    override fun stop() {
        try {
            audioEngine.stop()
            audioEngine.inputNode.removeTapOnBus(0u)
        } catch (_: Throwable) { /* engine wasn't running */ }
        request?.endAudio()
        task?.cancel()
        task = null
        request = null
    }
}

@Composable
actual fun rememberSpeechRecognizer(): SpeechRecognizer =
    remember { IosSpeechRecognizer() }