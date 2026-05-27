package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.BatteryManager
import android.net.Uri
import android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.provider.Settings
import android.provider.MediaStore
import java.io.File
import kotlin.math.sin

class DeviceController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var synthTrack: AudioTrack? = null
    private var isPlayingSynth = false

    // --- 1. Flashlight Control ---
    fun setFlashlight(enable: Boolean): String {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                cameraManager?.setTorchMode(cameraId, enable)
                if (enable) "Photon Emitters Activated. Flashlight is ON."
                else "Photon Emitters Deactivated. Flashlight is OFF."
            } else {
                "Optical hardware failure. No torch device found."
            }
        } catch (e: Exception) {
            "Hardware diagnostic returned error: ${e.message}"
        }
    }

    // --- 2. Battery Status ---
    fun getBatteryStatus(): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BATTERY_PROPERTY_CAPACITY) ?: -1
        return if (level != -1) {
            "Power Core status: $level% charge remaining."
        } else {
            "Unable to analyze primary power core levels."
        }
    }

    fun getBatteryPercent(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BATTERY_PROPERTY_CAPACITY) ?: 50
    }

    // --- 3. Storage and RAM Diagnostic ---
    fun getStorageDiagnostics(): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize

            val totalGB = totalBytes / (1024 * 1024 * 1024)
            val availableGB = availableBytes / (1024 * 1024 * 1024)
            val usedGB = totalGB - availableGB

            StorageInfo(totalGB, availableGB, usedGB)
        } catch (e: Exception) {
            StorageInfo(512, 256, 256)
        }
    }

    fun getRamDiagnostics(): RamInfo {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memoryInfo)

            val totalGB = memoryInfo.totalMem / (1024 * 1024 * 1024)
            val availGB = memoryInfo.availMem / (1024 * 1024 * 1024)
            val usedGB = totalGB - availGB

            RamInfo(totalGB.toInt(), availGB.toInt(), usedGB.toInt(), memoryInfo.lowMemory)
        } catch (e: Exception) {
            RamInfo(12, 6, 6, false)
        }
    }

    // --- 4. Audio / Volume Panel ---
    fun setSystemVolume(percent: Int): String {
        return try {
            if (audioManager != null) {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVol = (percent.toFloat() / 100f * maxVol).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
                "Acoustic feedback levels adjusted to $percent%."
            } else {
                "Audio manager components did not respond."
            }
        } catch (e: Exception) {
            "Audio calibration failed: ${e.message}"
        }
    }

    // --- 5. Cyber Synth Sound Generator (Real-time Audio Generating) ---
    fun playCyberSynth(): String {
        if (isPlayingSynth) return "Pulse frequency already running."
        isPlayingSynth = true
        Thread {
            try {
                val sampleRate = 44100
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                
                synthTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize * 2,
                    AudioTrack.MODE_STREAM
                )

                synthTrack?.play()
                val samples = ShortArray(minBufferSize)
                var angle1 = 0.0
                var angle2 = 0.0

                while (isPlayingSynth) {
                    for (i in samples.indices) {
                        // Cyberspace Sci-Fi dual-frequency modulation
                        // Generates a deep futuristic ambient beat/hum
                        // Sinewave with a low-frequency hum (80Hz and 120Hz) modulated slightly
                        angle1 += (2 * Math.PI * 110.0) / sampleRate
                        angle2 += (2 * Math.PI * 115.0) / sampleRate
                        val wave = (sin(angle1) * 0.5 + sin(angle2) * 0.3)
                        samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
                    }
                    synthTrack?.write(samples, 0, samples.size)
                }
            } catch (e: Exception) {
                Log.e("CyberSynth", "Synth pipeline broken", e)
            }
        }.start()
        return "Broadcasting deep space cyber-hum."
    }

    fun stopCyberSynth(): String {
        isPlayingSynth = false
        try {
            synthTrack?.stop()
            synthTrack?.release()
            synthTrack = null
        } catch (e: Exception) {
            Log.e("CyberSynth", "Synth shutdown error", e)
        }
        return "Cyber-frequency synthesis suspended."
    }

    // --- 6. Quick Launch Controls ---
    fun launchYouTube(query: String = ""): String {
        val intent = if (query.isNotEmpty()) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))
        } else {
            context.packageManager.getLaunchIntentForPackage("com.google.android.youtube") 
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Opening YouTube frequency buffer."
        } catch (e: Exception) {
            "YouTube launcher failed. Connecting via browser."
            openBrowser("https://www.youtube.com")
        }
    }

    fun searchGoogle(query: String): String {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Transmitting search request: $query"
        } catch (e: Exception) {
            "Searching browser buffer failed."
        }
    }

    fun openBrowser(url: String): String {
        val webUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Opening cyber-link: $webUrl"
        } catch (e: Exception) {
            "Unable to direct web link parameters."
        }
    }

    fun openSettings(): String {
        val intent = Intent(Settings.ACTION_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Initializing hardware configuration interface (Settings)."
        } catch (e: Exception) {
            "Settings configuration offline."
        }
    }

    fun openGallery(): String {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Retrieving visual data array (Gallery)."
        } catch (e: Exception) {
            "Unable to establish link to image data stream."
        }
    }

    fun launchCamera(): String {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Booting optical primary sensors (Camera)."
        } catch (e: Exception) {
            "Camera interface not ready."
        }
    }

    fun makeCall(number: String): String {
        val cleaned = number.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            "Drafting voice channel linkage to $cleaned."
        } catch (e: Exception) {
            "Unable to request call link."
        }
    }

    fun sendSms(number: String, body: String): String {
        val cleaned = number.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$cleaned")).apply {
            putExtra("sms_body", body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            "Transmitting tactical data linkage packet (SMS Draft open)."
        } catch (e: Exception) {
            "Unable to link system SMS protocols."
        }
    }
}

data class StorageInfo(
    val totalGB: Long,
    val availableGB: Long,
    val usedGB: Long
)

data class RamInfo(
    val totalGB: Int,
    val availableGB: Int,
    val usedGB: Int,
    val isLowMem: Boolean
)
