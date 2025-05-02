package xyz.rfsfernandes.albumlist

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.rfsfernandes.albumlist.di.DI

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
    }

    private fun initializeKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(DI.modules)
        }
    }
}
