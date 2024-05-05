package pt.isel.pc.utils

/**
 * Sealed hierarchy to represent success or error results.
 */
sealed interface SuccessOrError<out TSuccess, out TError> {

    val successOrThrow: TSuccess
    val errorOrThrow: TError

    data class Success<TSuccess>(val value: TSuccess) : SuccessOrError<TSuccess, Nothing> {
        override val successOrThrow: TSuccess
            get() = value
        override val errorOrThrow: Nothing
            get() = throw IllegalStateException()
    }

    data class Error<TError>(val error: TError) : SuccessOrError<Nothing, TError> {
        override val successOrThrow: Nothing
            get() = throw IllegalStateException()
        override val errorOrThrow: TError
            get() = error
    }

    companion object {
        fun <TSuccess> success(value: TSuccess) = Success(value)
        fun <TError> error(error: TError) = Error(error)
    }
}
