package xyz.rfsfernandes.albumlist.data.remote

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.rfsfernandes.albumlist.network.ConnectivityObserver
import xyz.rfsfernandes.albumlist.network.NetworkManager
import java.io.File

/**
 * RetrofitBuilder is a utility class responsible for creating and configuring a Retrofit instance
 * for network communication. It manages caching, provides offline support, and handles
 * network status checks.
 */
class RetrofitBuilder(
    private val context: Context,
    baseUrl: String,
    moshi: Moshi,
    private val networkManager: NetworkManager
) {

    private val isNetworkConnected: Boolean
        get() = networkManager.networkStatus.value == ConnectivityObserver.Status.Available

    /**
     * Provides an HTTP cache for OkHttp.
     *
     * This function creates and configures an [Cache] instance that will be used by OkHttp
     * to cache HTTP responses. The cache is stored in the application's cache directory
     * under "http_cache". The maximum size of the cache is set to 10 MB.
     *
     * @return A configured [Cache] instance.
     */
    private fun provideCache(): Cache {
        val cacheSize = 10 * 1024 * 1024L // 10 MB
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, cacheSize)
    }

    /**
     * Provides an OkHttpClient instance configured for network requests with caching and offline support.
     *
     * This function sets up an OkHttpClient with the following features:
     * - **Caching:** Uses a disk cache (provided by [provideCache]) to store HTTP responses.
     * - **Offline Support:** When the network is unavailable, it serves cached responses (up to 7 days old).
     * - **Online Caching:** When online, it caches responses for a short duration (1 minute).
     * - **Logging:** Logs HTTP request and response bodies for debugging purposes.
     *
     * The client is configured with two main interceptors:
     * - **offlineInterceptor:** This interceptor is responsible for handling requests when the device is offline.
     *   If the network is not connected (checked by [isNetworkConnected]), it modifies the request to use the cache exclusively
     *   and allows stale responses up to 7 days old (max-stale=604800 seconds).
     * - **onlineInterceptor:** This interceptor is responsible for caching responses when the device is online.
     *   It adds a "Cache-Control" header to the response, instructing clients (including OkHttp's cache) to store the response
     *   for 1 minute (max-age=60 seconds).
     *
     * @return An configured [OkHttpClient] instance.
     * @see provideCache
     * @see isNetworkConnected
     */
    private fun provideClient(): OkHttpClient {
        val cache = provideCache()

        // Interceptor to force cache when offline
        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkConnected) {
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=604800") // 7 days
                    .build()
            }
            chain.proceed(request)
        }

        // Interceptor to cache responses
        val onlineInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val maxAge = 60 // read from cache for 1 minute
            response.newBuilder().header("Cache-Control", "public, max-age=$maxAge").build()
        }

        return OkHttpClient.Builder().cache(cache).addInterceptor(offlineInterceptor)
            .addNetworkInterceptor(onlineInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    private var instance: Retrofit = Retrofit.Builder().baseUrl(baseUrl).client(provideClient())
        .addConverterFactory(MoshiConverterFactory.create(moshi)).build()

    val leBonCoinService: LeBonCoinService =
        instance.create(LeBonCoinService::class.java)
}
