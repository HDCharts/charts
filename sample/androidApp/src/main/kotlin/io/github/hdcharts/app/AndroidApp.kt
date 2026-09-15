package io.github.hdcharts.app

import android.app.Application
import io.github.hdcharts.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@AndroidApp)
            androidLogger()
        }
    }
}
