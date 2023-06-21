package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Audio {
    @JsonProperty
    var lang: String? = null

    @JsonProperty
    var type: String? = null

    @JsonProperty
    var url: String? = null
    override fun toString(): String {
        return "Audio{" +
                "lang='" + lang + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}'
    }
}