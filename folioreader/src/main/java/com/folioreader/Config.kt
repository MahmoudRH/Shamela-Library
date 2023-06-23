package com.folioreader

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import org.json.JSONObject

/**
 * Configuration class for FolioReader.
 */
class Config : Parcelable {
    var font = 3
    var fontSize = 2
    var isNightMode = false

    @get:ColorInt
    @ColorInt
    var themeColor = DEFAULT_THEME_COLOR_INT
        private set
    var isShowTts = true
        private set
    var allowedDirection: AllowedDirection? = DEFAULT_ALLOWED_DIRECTION
        private set
    var direction: Direction = DEFAULT_DIRECTION

    /**
     * Reading modes available
     */
    enum class AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    /**
     * Reading directions
     */
    enum class Direction {
        VERTICAL, HORIZONTAL
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(font)
        dest.writeInt(fontSize)
        dest.writeByte((if (isNightMode) 1 else 0).toByte())
        dest.writeInt(themeColor)
        dest.writeByte((if (isShowTts) 1 else 0).toByte())
        dest.writeString(allowedDirection.toString())
        dest.writeString(direction.toString())
    }

    private constructor(`in`: Parcel) {
        font = `in`.readInt()
        fontSize = `in`.readInt()
        isNightMode = `in`.readByte().toInt() != 0
        themeColor = `in`.readInt()
        isShowTts = `in`.readByte().toInt() != 0
        allowedDirection = getAllowedDirectionFromString(LOG_TAG, `in`.readString())
        direction = getDirectionFromString(LOG_TAG, `in`.readString())
    }

    constructor()
    constructor(jsonObject: JSONObject) {
        font = jsonObject.optInt(CONFIG_FONT)
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE)
        isNightMode = jsonObject.optBoolean(CONFIG_IS_NIGHT_MODE)
        themeColor = getValidColorInt(jsonObject.optInt(CONFIG_THEME_COLOR_INT))
        isShowTts = jsonObject.optBoolean(CONFIG_IS_TTS)
        allowedDirection = getAllowedDirectionFromString(
            LOG_TAG,
            jsonObject.optString(CONFIG_ALLOWED_DIRECTION)
        )
        direction = getDirectionFromString(LOG_TAG, jsonObject.optString(CONFIG_DIRECTION))
    }
    @ColorInt
    private fun getValidColorInt(@ColorInt colorInt: Int): Int {
        if (colorInt >= 0) {
            Log.w(
                LOG_TAG, "-> getValidColorInt -> Invalid argument colorInt = " + colorInt +
                        ", Returning DEFAULT_THEME_COLOR_INT = " + DEFAULT_THEME_COLOR_INT
            )
            return DEFAULT_THEME_COLOR_INT
        }
        return colorInt
    }

    companion object {
        private val LOG_TAG = Config::class.java.simpleName
        const val INTENT_CONFIG = "config"
        const val EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG"
        const val CONFIG_FONT = "font"
        const val CONFIG_FONT_SIZE = "font_size"
        const val CONFIG_IS_NIGHT_MODE = "is_night_mode"
        const val CONFIG_THEME_COLOR_INT = "theme_color_int"
        const val CONFIG_IS_TTS = "is_tts"
        const val CONFIG_ALLOWED_DIRECTION = "allowed_direction"
        const val CONFIG_DIRECTION = "direction"
        private val DEFAULT_ALLOWED_DIRECTION = AllowedDirection.ONLY_VERTICAL
        private val DEFAULT_DIRECTION = Direction.VERTICAL
        private val DEFAULT_THEME_COLOR_INT =
            ContextCompat.getColor(AppContext.get()!!, R.color.default_theme_accent_color)
        @JvmField
        val CREATOR: Creator<Config> = object : Creator<Config> {
            override fun createFromParcel(`in`: Parcel): Config {
                return Config(`in`)
            }

            override fun newArray(size: Int): Array<Config> {
                return arrayOf()
            }
        }

        fun getDirectionFromString(LOG_TAG: String?, directionString: String?): Direction {
            return when (directionString) {
                "VERTICAL" -> Direction.VERTICAL
                "HORIZONTAL" -> Direction.HORIZONTAL
                else -> {
                    Log.w(
                        LOG_TAG, "-> Illegal argument directionString = `" + directionString
                                + "`, defaulting direction to " + DEFAULT_DIRECTION
                    )
                    DEFAULT_DIRECTION
                }
            }
        }

        fun getAllowedDirectionFromString(
            LOG_TAG: String?,
            allowedDirectionString: String?
        ): AllowedDirection {
            return when (allowedDirectionString) {
                "ONLY_VERTICAL" -> AllowedDirection.ONLY_VERTICAL
                "ONLY_HORIZONTAL" -> AllowedDirection.ONLY_HORIZONTAL
                "VERTICAL_AND_HORIZONTAL" -> AllowedDirection.VERTICAL_AND_HORIZONTAL
                else -> {
                    Log.w(
                        LOG_TAG, "-> Illegal argument allowedDirectionString = `"
                                + allowedDirectionString + "`, defaulting allowedDirection to "
                                + DEFAULT_ALLOWED_DIRECTION
                    )
                    DEFAULT_ALLOWED_DIRECTION
                }
            }
        }
    }
}