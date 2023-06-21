package com.folioreader.model.dictionary

/**
 * Created by gautam on 7/7/17.
 */
class Wikipedia {
    var word: String? = null
    var definition: String? = null
    var link: String? = null
    override fun toString(): String {
        return "Wikipedia{" +
                "word='" + word + '\'' +
                ", definition='" + definition + '\'' +
                ", link='" + link + '\'' +
                '}'
    }
}