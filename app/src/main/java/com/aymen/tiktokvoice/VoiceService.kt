package com.aymen.tiktokvoice

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.accessibility.AccessibilityEvent

class VoiceService : AccessibilityService() {

    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastActionTime = 0L

    private val nextWords = listOf("التالي", "التالى", "next", "الجاي", "الموالي")
    private val prevWords = listOf("السابق", "رجوع", "للوراء", "previous", "back", "اللي قبل")
    private val toggleWords = listOf("وقف", "توقف", "إيقاف", "ايقاف", "شغل", "شغّل", "stop", "pause", "play")

    override fun onServiceConnected() {
        super.onServiceConnected()
        startListening()
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            handler.postDelayed({ startListening() }, 5000)
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                list?.any { handleCommand(it) }
                handler.postDelayed({ startListening() }, 300)
            }
            override fun onError(error: Int) {
                handler.postDelayed({ startListening() }, 1000)
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer?.startListening(intent)
    }

    private fun handleCommand(text: String): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastActionTime < 1500) return false
        val t = text.lowercase()
        val dm = resources.displayMetrics
        val w = dm.widthPixels.toFloat()
        val h = dm.heightPixels.toFloat()
        when {
            nextWords.any { t.contains(it) } -> swipe(w / 2, h * 0.75f, h * 0.25f)
            prevWords.any { t.contains(it) } -> swipe(w / 2, h * 0.25f, h * 0.75f)
            toggleWords.any { t.contains(it) } -> tap(w / 2, h / 2)
            else -> return false
        }
        lastActionTime = now
        return true
    }

    private fun swipe(x: Float, y1: Float, y2: Float) {
        val path = Path().apply {
            moveTo(x, y1)
            lineTo(x, y2)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 250)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun tap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        super.onDestroy()
    }
}
