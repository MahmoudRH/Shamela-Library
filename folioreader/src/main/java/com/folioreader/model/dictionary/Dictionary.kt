package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * class is object representation of JSON received from
 * open source dictionary API "pearson"
 * ref => http://developer.pearson.com/apis/dictionaries
 *
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Dictionary {
    @JsonProperty
    var status = 0

    @JsonProperty
    var url: String? = null

    @JsonProperty
    var results: List<DictionaryResults>? = null
    override fun toString(): String {
        return "Dictionary{" +
                "status=" + status +
                ", url='" + url + '\'' +
                ", results=" + results +
                '}'
    }
}