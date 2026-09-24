package com.example.ui

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordedFile: File? = null

    init {
        recordedFile = File(context.cacheDir, "child_speech_recording.3gp")
    }

    fun startRecording(): Boolean {
        try {
            stopPlaying() // Stop any ongoing playback
            stopRecording() // Clear any active recorder

            val attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.createAttributionContext("microphone_attribution")
            } else {
                context
            }

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(attributionContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordedFile?.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to start recording", e)
            return false
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop recording", e)
        } finally {
            mediaRecorder = null
        }
    }

    fun startPlaying(onComplete: () -> Unit): Boolean {
        if (recordedFile == null || !recordedFile!!.exists() || recordedFile!!.length() == 0L) {
            return false
        }
        try {
            stopPlaying()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(recordedFile!!.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onComplete()
                    stopPlaying()
                }
            }
            return true
        } catch (e: IOException) {
            Log.e("AudioRecorderHelper", "Failed to play recording", e)
            return false
        }
    }

    fun stopPlaying() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Failed to stop playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    fun hasRecording(): Boolean {
        return recordedFile?.exists() == true && recordedFile!!.length() > 0
    }
}
