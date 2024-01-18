package org.mklabs.signal_generator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var selectedSignal = "sine"
    private lateinit var frequencySeekBar: SeekBar
    private lateinit var frequencyTextView: TextView
    private lateinit var generateButton: Button
    private lateinit var stopButton: Button

    private lateinit var frequencyVal: EditText
    private lateinit var applyButton: Button

    private lateinit var audioTrack: AudioTrack
    private val audioTrackLock = Any()


    private val sampleRate = 44100
    private val duration = 60 // 재생할 시간
    private val numSamples = sampleRate * duration
    private val bufferSizeInBytes = 2 * AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ) // 2배로 설정
    private lateinit var buffer: ShortArray
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기에 선택되어야 하는 라디오 버튼 설정
        val radioButton1 = findViewById<AppCompatRadioButton>(R.id.radioButton1)
        radioButton1.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.selectedColor)

        // 주파수 조절 바와 선택한 주파수
        frequencySeekBar = findViewById(R.id.frequencySeekBar)
        frequencyTextView = findViewById(R.id.frequencyTextView)

        // 주파수 직접 입력 후 적용
        frequencyVal = findViewById(R.id.frequencyVal)
        applyButton = findViewById(R.id.applyFrequencyButton)

        // 주파수 출력, 중지 버튼
        generateButton = findViewById(R.id.generateButton)
        stopButton = findViewById(R.id.stopButton)

        frequencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val frequency = progress
                frequencyTextView.text = "$frequency Hz"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun signalSelect(view: View) {
        // 라디오 버튼 클릭 시 배경색 변경
        val clickedButton = view as AppCompatRadioButton
        selectedSignal = clickedButton.text.toString()
        clickedButton.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.selectedColor)

        val radioGroup = findViewById<View>(R.id.radioGroup) as RadioGroup
        for (i in 0 until radioGroup.childCount) {
            val child = radioGroup.getChildAt(i)
            if (child is AppCompatRadioButton && child != view) {
                // 선택 안 한 라디오 버튼의 배경색과 글자색 초기화
                child.backgroundTintList =
                    ContextCompat.getColorStateList(this, R.color.unselectedColor)
                updateButtonTextColor(child, false)
            }
        }
        // 선택한 버튼의 글자 색 변경
        updateButtonTextColor(clickedButton, true)

        println("선택한 버튼: ${clickedButton.text}")
        println("선택한 버튼: $selectedSignal")
    }

    // applyFrequencyButton의 클릭 이벤트 처리
    fun applyFrequency(view: View) {
        // SeekBar의 현재 값 가져오기
        val selectedFrequency: Int = frequencySeekBar.progress
        val frequencyValString: String = frequencyVal.text.toString()

        // 값이 비어있지 않은 경우에만 처리
        if (!TextUtils.isEmpty(frequencyValString)) {
            try {
                val frequencyVal: Int = frequencyValString.toInt()

                // 주파수 범위 검사 (20에서 20000 사이)
                if (frequencyVal in 20..20000) {
                    frequencyTextView.text = "$frequencyVal Hz"
                    frequencySeekBar.progress = frequencyVal
                } else {
                    // 범위를 벗어난 경우 처리
                    Toast.makeText(this, "please right between 20hz~20khz", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                // 숫자로 변환할 수 없는 경우 처리
                Toast.makeText(this, "please right number between 20hz~20khz", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateButtonTextColor(button: AppCompatRadioButton, isSelected: Boolean) {
        val textColorResId = if (isSelected) R.color.white else R.color.black
        button.setTextColor(ContextCompat.getColorStateList(this, textColorResId))
    }

    // generate, stop 버튼 변경
    private fun updateButtonVisibility(isPlaying: Boolean) {
        if (isPlaying) {
            generateButton.visibility = View.GONE
            stopButton.visibility = View.VISIBLE
            println("updateButtonVisibility: isplaying 값은:$isPlaying")
        } else {
            generateButton.visibility = View.VISIBLE
            stopButton.visibility = View.GONE
            println("updateButtonVisibility: isplaying 값은:$isPlaying")
        }
    }

    private fun generateSineWave(frequency: Int) {
        buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            buffer[i] = (Short.MAX_VALUE * Math.sin(2.0 * Math.PI * i / (sampleRate / frequency))).toInt().toShort()
        }
        println("SineWave 송출")
    }

    private fun generateSquareWave(frequency: Int) {
        buffer = ShortArray(numSamples)
        val cycleSamples = sampleRate / frequency

        for (i in 0 until numSamples) {
            buffer[i] = if (i % cycleSamples < cycleSamples / 2) {
                Short.MAX_VALUE
            } else {
                Short.MIN_VALUE
            }
        }
        println("SquareWave 송출")
    }

    private fun generateTriangleWave(frequency: Int) {
        buffer = ShortArray(numSamples)
        val cycleSamples = sampleRate / frequency

        for (i in 0 until numSamples) {
            val value = 2.0 * Math.abs((i % cycleSamples) - cycleSamples / 2).toDouble() / (cycleSamples / 2) - 1.0
            buffer[i] = (Short.MAX_VALUE * value).toInt().toShort()
        }
        println("TriangleWave 송출")
    }

    private fun playSound() {
        // AudioTrack 초기화
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        // AudioTrack 초기화 후 상태 확인
        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) {
            // 초기화에 실패하면 오류 처리
            // 오류 처리를 추가하세요.
            return
        }

        // setPlaybackPositionUpdateListener이 직접 사용 불가능하므로 setNotificationMarkerPosition을 사용합니다.
        audioTrack.setNotificationMarkerPosition(buffer.size)

        // Notification Marker 위치에 도달할 때의 콜백을 등록합니다.
        audioTrack.setPlaybackPositionUpdateListener(
            object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack) {
                    // Not needed in this example
                }

                override fun onPeriodicNotification(track: AudioTrack) {
                    if (track.playState == AudioTrack.PLAYSTATE_STOPPED) {
                        // 재생이 끝났을 때 처리
                        isPlaying = false
                        runOnUiThread {
                            updateButtonVisibility(isPlaying)
                        }
                    }
                }
            }
        )

        // AudioTrack 재생 시작
        audioTrack.play()

        // 비동기적으로 오디오를 여러 번 쓰기
        Thread {
            val tempBuffer = ShortArray(bufferSizeInBytes / 2)

            // audioTrack 객체에 접근하는 부분을 동기화
            synchronized(audioTrackLock) {
                var written = 0
                try {
                    while (written < buffer.size) {
                        // audioTrack의 상태를 다시 확인
                        if (::audioTrack.isInitialized && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            val remaining = buffer.size - written
                            val toWrite = if (remaining > tempBuffer.size) tempBuffer.size else remaining
                            System.arraycopy(buffer, written, tempBuffer, 0, toWrite)

                            val result = audioTrack.write(tempBuffer, 0, toWrite)

                            if (result < 0) {
                                // 오류 처리
                                break
                            }

                            written += result
                        } else {
                            // audioTrack이 초기화되지 않았거나 재생 상태가 아닌 경우
                            break
                        }
                    }
                } finally {
                    // 모든 버퍼가 재생되면 재생을 중지하고 AudioTrack 해제
                    if (audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                        try {
                            audioTrack.stop()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                        audioTrack.release()
                    }
                    isPlaying = false
                    runOnUiThread {
                        updateButtonVisibility(isPlaying)
                    }
                }
            }
        }.start()
    }


    fun onGenerateButtonClick(view: View) {
        if (!isPlaying) {
            isPlaying = true
            val frequencySeekBar = findViewById<SeekBar>(R.id.frequencySeekBar)
            val frequency = frequencySeekBar.progress
            if (selectedSignal == "sine") generateSineWave(frequency)
            else if (selectedSignal == "square") generateSquareWave(frequency)
            else generateTriangleWave(frequency)
            updateButtonVisibility(isPlaying)
            playSound()
            println("audioTrack.state: " + audioTrack.state)
            println("audioTrack.playState" + audioTrack.playState)
        }
    }

    fun stopGenerateSound(view: View) {
        isPlaying = false
        stopSound()
        updateButtonVisibility(isPlaying)
    }

    // 재생 중인 소리 중지
    private fun stopSound() {
        // audioTrack이 초기화되었고, STATE_UNINITIALIZED 상태가 아니며, PLAYSTATE_STOPPED 상태가 아닌 경우에만 정지 및 해제
        if (::audioTrack.isInitialized) {
            if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED && audioTrack.playState != AudioTrack.PLAYSTATE_STOPPED) {
                try {
                    audioTrack.stop() // 오디오 재생 종료
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }
                audioTrack.release() // 오디오 트랙이 잡은 모든 리소스를 해제
            }
        }
    }
}
