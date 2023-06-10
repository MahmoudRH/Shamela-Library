package com.shamela.library.presentaion.screens.settings


sealed class SettingsEvent{
    class SampleEvent(val newText: String): SettingsEvent()
}