package com.folioreader.util

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created by Hrishikesh Kadam on 21/04/2018.
 */
object ObjectMapperSingleton {
    @Volatile
    var objectMapper: ObjectMapper? = null
        get() {
            if (field == null) {
                synchronized(ObjectMapperSingleton::class.java) {
                    if (field == null) {
                        field = ObjectMapper()
                    }
                }
            }
            return field
        }
        private set
}