package com.example.myapplicationbodytd.managers

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import android.media.ToneGenerator
import android.media.AudioManager

class SoundManager private constructor(private val context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<SoundType, Int>()
    private var backgroundMusic: MediaPlayer? = null
    private var isMuted = false
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context).also { instance = it }
            }
        }
    }

    init {
        val attributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(attributes)
            .build()

        // Initialiser les sons avec des valeurs par défaut
        sounds[SoundType.TOWER_PLACED] = 0
        sounds[SoundType.TOWER_UPGRADED] = 0
        sounds[SoundType.ENEMY_HIT] = 0
        sounds[SoundType.ENEMY_DEATH] = 0
        sounds[SoundType.WAVE_START] = 0
        sounds[SoundType.WAVE_COMPLETE] = 0
        sounds[SoundType.GAME_OVER] = 0
        sounds[SoundType.BUTTON_CLICK] = 0
    }

    fun playSound(soundType: SoundType) {
        if (isMuted) return
        try {
            when (soundType) {
                SoundType.TOWER_PLACED -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
                SoundType.TOWER_UPGRADED -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 200)
                SoundType.ENEMY_HIT -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                SoundType.ENEMY_DEATH -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 100)
                SoundType.WAVE_START -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300)
                SoundType.WAVE_COMPLETE -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 300)
                SoundType.GAME_OVER -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 500)
                SoundType.BUTTON_CLICK -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors de la lecture du son", e)
        }
    }

    fun startBackgroundMusic() {
        if (isMuted) return
        try {
            backgroundMusic?.start()
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors du démarrage de la musique", e)
        }
    }

    fun pauseBackgroundMusic() {
        try {
            backgroundMusic?.pause()
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors de la pause de la musique", e)
        }
    }

    fun stopBackgroundMusic() {
        try {
            backgroundMusic?.stop()
            backgroundMusic?.prepare()
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors de l'arrêt de la musique", e)
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            pauseBackgroundMusic()
        } else {
            startBackgroundMusic()
        }
    }

    fun release() {
        try {
            soundPool.release()
            backgroundMusic?.release()
            backgroundMusic = null
            toneGenerator.release()
        } catch (e: Exception) {
            Log.e("SoundManager", "Erreur lors de la libération des ressources", e)
        }
    }
}

enum class SoundType {
    TOWER_PLACED,
    TOWER_UPGRADED,
    ENEMY_HIT,
    ENEMY_DEATH,
    WAVE_START,
    WAVE_COMPLETE,
    GAME_OVER,
    BUTTON_CLICK
} 