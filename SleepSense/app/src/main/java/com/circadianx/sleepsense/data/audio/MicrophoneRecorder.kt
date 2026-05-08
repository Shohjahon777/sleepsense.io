package com.circadianx.sleepsense.data.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.circadianx.sleepsense.data.bluetooth.HardwarePacket
import com.circadianx.sleepsense.data.model.ApneaEvent
import com.circadianx.sleepsense.data.model.HeartbeatPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log10
import kotlin.math.sqrt

private const val SAMPLE_RATE = 44100
private const val BUFFER_MS = 100
private const val SNORE_ONSET_MS = 5_000
private const val APNEA_SILENCE_MS = 10_000
private const val HEARTBEAT_INTERVAL_MS = 60_000

@Singleton
class MicrophoneRecorder @Inject constructor() {

    @Volatile private var active = false

    private val _liveDbfs = MutableStateFlow(-60f)
    val liveDbfs: StateFlow<Float> = _liveDbfs.asStateFlow()

    fun stop() {
        active = false
    }

    fun recordAndDetect(sessionId: Long, thresholdDbfs: Float = -20f): Flow<HardwarePacket> = flow @androidx.annotation.RequiresPermission(
        android.Manifest.permission.RECORD_AUDIO
    ) {
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val samplesPerBuffer = SAMPLE_RATE * BUFFER_MS / 1000
        val bufferSize = minBuf.coerceAtLeast(samplesPerBuffer * 2)

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        recorder.startRecording()
        active = true

        val pcm = ShortArray(samplesPerBuffer)
        var snoreOnsetMs = 0L
        var silenceAfterSnoreMs = 0L
        var isSnoring = false
        var heartbeatAccumMs = 0L
        var hbDbSum = 0.0
        var hbSamples = 0
        var preSnoreDb = -60f

        try {
            while (active) {
                val read = recorder.read(pcm, 0, samplesPerBuffer)
                if (read <= 0) continue

                val rms = sqrt(pcm.take(read).sumOf { s -> (s.toLong() * s).toDouble() } / read)
                val dbfs = if (rms > 0.0) (20.0 * log10(rms / 32768.0)).toFloat() else -96f
                _liveDbfs.value = dbfs

                hbDbSum += dbfs
                hbSamples++
                heartbeatAccumMs += BUFFER_MS

                if (dbfs >= thresholdDbfs) {
                    snoreOnsetMs += BUFFER_MS
                    silenceAfterSnoreMs = 0
                    preSnoreDb = dbfs
                    if (snoreOnsetMs >= SNORE_ONSET_MS) isSnoring = true
                } else {
                    if (!isSnoring) snoreOnsetMs = 0
                    if (isSnoring) {
                        silenceAfterSnoreMs += BUFFER_MS
                        if (silenceAfterSnoreMs >= APNEA_SILENCE_MS) {
                            emit(
                                HardwarePacket.Apnea(
                                    event = ApneaEvent(
                                        sessionId = sessionId,
                                        timestamp = System.currentTimeMillis() / 1000,
                                        durationSec = (silenceAfterSnoreMs / 1000).toInt(),
                                        preSnoreDb = preSnoreDb,
                                        dominantFreqHz = 250,
                                        tempC = 0f,
                                        humidityPct = 0f,
                                        movementDetected = false
                                    ),
                                    sessionId = sessionId
                                )
                            )
                            isSnoring = false
                            snoreOnsetMs = 0
                            silenceAfterSnoreMs = 0
                        }
                    }
                }

                if (heartbeatAccumMs >= HEARTBEAT_INTERVAL_MS) {
                    val avgDb = if (hbSamples > 0) (hbDbSum / hbSamples).toFloat() else -60f
                    emit(
                        HardwarePacket.Heartbeat(
                            HeartbeatPacket(
                                type = "HEARTBEAT",
                                avgSnoreDb = avgDb,
                                tempC = 0f,
                                humidityPct = 0f,
                                statusCode = 0
                            )
                        )
                    )
                    heartbeatAccumMs = 0
                    hbDbSum = 0.0
                    hbSamples = 0
                }
            }
        } finally {
            recorder.stop()
            recorder.release()
            _liveDbfs.value = -60f
        }
    }.flowOn(Dispatchers.IO)
}
