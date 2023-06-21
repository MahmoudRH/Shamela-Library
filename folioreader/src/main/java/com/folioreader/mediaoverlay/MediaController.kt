package com.folioreader.mediaoverlay

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.folioreader.Constants
import com.folioreader.model.event.MediaOverlayPlayPauseEvent
import com.folioreader.model.event.MediaOverlaySpeedEvent.Speed
import com.folioreader.model.media_overlay.OverlayItems
import com.folioreader.util.UiUtil
import org.readium.r2.shared.Clip
import org.readium.r2.shared.MediaOverlays
import java.io.IOException
import java.util.Locale

/**
 * @author gautam chibde on 21/6/17.
 */
class MediaController(
    private val context: Context,
    private val mediaType: MediaType,
    private val callbacks: MediaControllerCallbacks
) {
    enum class MediaType {
        TTS, SMIL
    }

    //**********************************//
    //          MEDIA OVERLAY           //
    //**********************************//
    private var mediaOverlays: MediaOverlays? = null
    private var mediaItems: List<OverlayItems> = ArrayList()
    private var mediaItemPosition = 0
    private var mediaPlayer: MediaPlayer? = null
    private var currentClip: Clip? = null
    private var isMediaPlayerReady = false
    private var mediaHandler: Handler? = null

    //*********************************//
    //              TTS                //
    //*********************************//
    private var mTextToSpeech: TextToSpeech? = null
    private var mIsSpeaking = false
    private val mHighlightTask: Runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer!!.currentPosition
            if (mediaPlayer!!.duration != currentPosition) {
                if (mediaItemPosition < mediaItems.size) {
                    //int end = (int) currentClip.end * 1000;
                    val end = (currentClip!!.end!! * 1000).toInt()
                    if (currentPosition > end) {
                        mediaItemPosition++
                        currentClip = mediaItems[mediaItemPosition].id?.let {
                            mediaOverlays!!.clip(
                                it
                            )
                        }
                        if (currentClip != null) {
                            callbacks.highLightText(mediaItems[mediaItemPosition].id)
                        } else {
                            mediaItemPosition++
                        }
                    }
                    mediaHandler!!.postDelayed(this, 10)
                } else {
                    mediaHandler!!.removeCallbacks(this)
                }
            }
        }
    }

    fun resetMediaPosition() {
        mediaItemPosition = 0
    }

    fun setSMILItems(overlayItems: List<OverlayItems>) {
        mediaItems = overlayItems
    }

    operator fun next() {
        mediaItemPosition++
    }

    fun setTextToSpeech(context: Context) {
        mTextToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                mTextToSpeech!!.language = Locale.UK
                mTextToSpeech!!.setSpeechRate(0.70f)
            }
            mTextToSpeech!!.setOnUtteranceCompletedListener {
                (context as AppCompatActivity).runOnUiThread {
                    if (mIsSpeaking) {
                        callbacks.highLightTTS()
                    }
                }
            }
        }
    }

    fun setUpMediaPlayer(mediaOverlays: MediaOverlays?, path: String, mBookTitle: String) {
        this.mediaOverlays = mediaOverlays
        mediaHandler = Handler()
        try {
            mediaItemPosition = 0
            val uri = Constants.DEFAULT_STREAMER_URL + mBookTitle + path
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(uri)
            mediaPlayer!!.prepare()
            isMediaPlayerReady = true
        } catch (e: IOException) {
            Log.e(TAG, e.message!!)
        }
    }

    fun setSpeed(speed: Speed) {
        when (speed) {
            Speed.HALF -> setPlaybackSpeed(0.5f)
            Speed.ONE -> setPlaybackSpeed(1.0f)
            Speed.ONE_HALF -> setPlaybackSpeed(1.5f)
            Speed.TWO -> setPlaybackSpeed(2.0f)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setPlaybackSpeed(speed: Float) {
        if (mediaType == MediaType.SMIL) {
            if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                mediaPlayer!!.playbackParams = mediaPlayer!!.playbackParams.setSpeed(speed)
            }
        } else {
            mTextToSpeech!!.setSpeechRate(speed)
        }
    }

    fun stateChanged(event: MediaOverlayPlayPauseEvent) {
        if (event.isStateChanged) {
            if (mediaPlayer != null) {
                mediaPlayer!!.pause()
            }
            if (mTextToSpeech != null && mTextToSpeech!!.isSpeaking) {
                mTextToSpeech!!.stop()
            }
        } else {
            if (event.isPlay) {
                UiUtil.keepScreenAwake(true, context)
            } else {
                UiUtil.keepScreenAwake(false, context)
            }
            if (mediaType == MediaType.SMIL) {
                playSMIL()
            } else {
                if (mTextToSpeech!!.isSpeaking) {
                    mTextToSpeech!!.stop()
                    mIsSpeaking = false
                    callbacks.resetCurrentIndex()
                } else {
                    mIsSpeaking = true
                    callbacks.highLightTTS()
                }
            }
        }
    }

    private fun playSMIL() {
        if (mediaPlayer != null && isMediaPlayerReady) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
            } else {
                currentClip = mediaItems[mediaItemPosition].id?.let { mediaOverlays!!.clip(it) }
                if (currentClip != null) {
                    mediaPlayer!!.start()
                    mediaHandler!!.post(mHighlightTask)
                } else {
                    mediaItemPosition++
                    mediaPlayer!!.start()
                    mediaHandler!!.post(mHighlightTask)
                }
            }
        }
    }

    fun speakAudio(sentence: String?) {
        if (mediaType == MediaType.TTS) {
            val params = HashMap<String, String>()
            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "stringId"
            mTextToSpeech!!.speak(sentence, TextToSpeech.QUEUE_FLUSH, params)
        }
    }

    fun stop() {
        if (mTextToSpeech != null) {
            if (mTextToSpeech!!.isSpeaking) {
                mTextToSpeech!!.stop()
            }
            mTextToSpeech!!.shutdown()
        }
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
            mediaHandler!!.removeCallbacks(mHighlightTask)
        }
    }

    companion object {
        private val TAG = MediaController::class.java.simpleName
    }
}