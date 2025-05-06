package xyz.rfsfernandes.albumlist

import android.app.Application
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.rfsfernandes.albumlist.di.DI

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        initializeCoil()
    }

    private fun initializeCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("User-Agent", "Android")
                                .build()
                        )
                    }
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }

    private fun initializeKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(DI.modules)
        }
    }
}
