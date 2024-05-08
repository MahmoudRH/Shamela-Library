package com.folioreader.util

import com.folioreader.model.locators.ReadLocator

/**
 * Created by Hrishikesh Kadam on 17/04/2018.
 */
interface ReadLocatorListener {
    fun saveReadLocator(readLocator: ReadLocator)
}