package xyz.rfsfernandes.albumlist.data.util

import xyz.rfsfernandes.albumlist.data.remote.RemoteException

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String? = null, val exception: RemoteException? = null) :
        Resource<T>(message = message)

    class CachedSuccess<T>(data: T? = null, val cacheReason: CacheReason? = null) :
        Resource<T>(data)

    class Default<T>(data: T? = null) : Resource<T>(data)
}
