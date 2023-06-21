package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class DictionaryResults {
    @JsonProperty
    var headword: String = ""

    @JsonProperty
    var partOfSpeech: String = ""

    @JsonProperty
    var pronunciations: List<Pronunciations>? = null

    @JsonProperty
    var senses: List<Senses>? = null
    override fun toString(): String {
        return "DictionaryResults{" +
                "headword='" + headword + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                ", pronunciations=" + pronunciations +
                ", senses=" + senses +
                '}'
    }
}