package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Pronunciations {
    @JsonProperty
    var audio: List<Audio?>? = null
    override fun toString(): String {
        return "Pronunciations{" +
                "audio=" + audio +
                '}'
    }
}