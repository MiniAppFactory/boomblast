package com.miniappfactory.boomblocks.game.retro

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.miniappfactory.boomblocks.game.retro.TetrisGameEngine.SoundEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// AI Studio'da uretilen "SoundManager"dan yeniden adlandirildi — bizim kendi
// com.miniappfactory.boomblocks.utils.SoundManager (Boom Blocks'un ana SFX motoru) ile isim
// karisikligi olmasin diye. Ses efektleri ToneGenerator ile PROGRAMATIK
// uretiliyor, hic ses dosyasi (res/raw) gerektirmiyor.
class RetroSoundManager(context: Context) {

    private val applicationContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 80)
    } catch (e: Exception) {
        null
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    fun playSound(effect: SoundEffect) {
        if (hapticsEnabled) {
            triggerVibration(effect)
        }

        if (!soundEnabled) return

        scope.launch {
            try {
                val tg = toneGenerator ?: return@launch
                when (effect) {
                    SoundEffect.MOVE -> tg.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
                    SoundEffect.ROTATE -> tg.startTone(ToneGenerator.TONE_PROP_ACK, 40)
                    SoundEffect.SOFT_DROP -> tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 30)
                    SoundEffect.HARD_DROP -> {
                        tg.startTone(ToneGenerator.TONE_DTMF_D, 60)
                    }
                    SoundEffect.HOLD -> tg.startTone(ToneGenerator.TONE_DTMF_A, 50)
                    SoundEffect.SINGLE_CLEAR -> {
                        tg.startTone(ToneGenerator.TONE_DTMF_0, 100)
                    }
                    SoundEffect.DOUBLE_CLEAR -> {
                        tg.startTone(ToneGenerator.TONE_DTMF_1, 100)
                        delay(90)
                        tg.startTone(ToneGenerator.TONE_DTMF_3, 120)
                    }
                    SoundEffect.TRIPLE_CLEAR -> {
                        tg.startTone(ToneGenerator.TONE_DTMF_3, 80)
                        delay(80)
                        tg.startTone(ToneGenerator.TONE_DTMF_5, 80)
                        delay(80)
                        tg.startTone(ToneGenerator.TONE_DTMF_7, 120)
                    }
                    SoundEffect.TETRIS_CLEAR -> {
                        // Fanfare jingle
                        val tones = intArrayOf(ToneGenerator.TONE_DTMF_1, ToneGenerator.TONE_DTMF_5, ToneGenerator.TONE_DTMF_8, ToneGenerator.TONE_DTMF_C)
                        for (t in tones) {
                            tg.startTone(t, 90)
                            delay(90)
                        }
                    }
                    SoundEffect.LEVEL_UP -> {
                        tg.startTone(ToneGenerator.TONE_DTMF_4, 100)
                        delay(100)
                        tg.startTone(ToneGenerator.TONE_DTMF_8, 150)
                    }
                    SoundEffect.GAME_OVER -> {
                        val tones = intArrayOf(ToneGenerator.TONE_DTMF_9, ToneGenerator.TONE_DTMF_6, ToneGenerator.TONE_DTMF_3, ToneGenerator.TONE_DTMF_0)
                        for (t in tones) {
                            tg.startTone(t, 120)
                            delay(120)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore audio hardware errors silently
            }
        }
    }

    private fun triggerVibration(effect: SoundEffect) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            when (effect) {
                SoundEffect.MOVE, SoundEffect.ROTATE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(15)
                    }
                }
                SoundEffect.SOFT_DROP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(20)
                    }
                }
                SoundEffect.HARD_DROP, SoundEffect.HOLD -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(45)
                    }
                }
                SoundEffect.SINGLE_CLEAR, SoundEffect.DOUBLE_CLEAR -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(60)
                    }
                }
                SoundEffect.TRIPLE_CLEAR, SoundEffect.TETRIS_CLEAR -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = longArrayOf(0, 40, 30, 80)
                        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(120)
                    }
                }
                SoundEffect.LEVEL_UP, SoundEffect.GAME_OVER -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = longArrayOf(0, 50, 50, 100)
                        v.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(150)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration permission exceptions
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
