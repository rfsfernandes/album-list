package xyz.rfsfernandes.albumlist.data.remote

sealed class RemoteException(val contentMessage: String? = null) : Throwable(contentMessage) {
    class NetworkException(message: String? = null) : RemoteException(message)
    class EmptyBodyException(message: String? = null) : RemoteException(message)
    class DefaultException(message: String? = null) : RemoteException(message)
}
