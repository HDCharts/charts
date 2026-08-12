package dev.hdcode.charts.app.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        printLogger()
        appDeclaration?.invoke(this)
        modules(appModule)
    }
}
