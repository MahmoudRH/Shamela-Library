package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Example {
    @JsonProperty
    var text: String? = null
    override fun toString(): String {
        return "Example{" +
                "text='" + text + '\'' +
                '}'
    }
}