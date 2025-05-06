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

    /**
     * Initializes the Coil image loading library with a custom configuration.
     *
     * This function sets up a global ImageLoader instance for Coil, allowing for
     * customized image loading behavior across the application. Specifically, it:
     *
     * 1. **Creates an OkHttpClient:**  Builds an OkHttpClient instance to handle
     *    network requests for images.
     * 2. **Adds a User-Agent Interceptor:**  Configures the OkHttpClient to add a
     *    "User-Agent" header with the value "Android" to all image requests. This
     *    can be useful for server-side analytics or to ensure compatibility with
     *    servers that expect this header.
     * 3. **Builds an ImageLoader:** Creates an ImageLoader using the configured
     *    OkHttpClient.
     * 4. **Sets the Global ImageLoader:** Sets the newly created ImageLoader as
     *    the global Coil ImageLoader. This ensures that all Coil image requests
     *    throughout the application use this customized configuration.
     *
     * Call this function early in your application's lifecycle, typically in the
     * `onCreate()` method of your Application class, to ensure that Coil is
     * properly initialized before any image requests are made.
     */
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
                    }.build()
            }.build()

        Coil.setImageLoader(imageLoader)
    }

    /**
     * Initializes the Koin dependency injection framework.
     *
     * This function configures and starts Koin, making it available throughout the application.
     * It sets up the Android context and loads the defined Koin modules.
     *
     * The Koin modules (defined in `DI.modules`) contain the definitions of dependencies
     * (e.g., repositories, view models, use cases) that can be injected into other components.
     *
     * This function should be called once during the application's lifecycle, typically in the `onCreate()` method of the `Application` class.
     *
     * @see org.koin.core.context.startKoin
     * @see org.koin.android.ext.koin.androidContext
     * @see DI.modules
     */
    private fun initializeKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(DI.modules)
        }
    }
}
