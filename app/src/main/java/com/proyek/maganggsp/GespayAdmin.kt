package com.proyek.maganggsp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for GesPay Admin
 * Annotated with @HiltAndroidApp to enable dependency injection throughout the app
 */
@HiltAndroidApp
class GespayAdmin : Application()
