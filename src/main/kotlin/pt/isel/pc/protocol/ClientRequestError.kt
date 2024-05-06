package pt.isel.pc.protocol

/**
 * Sealed hierarchy to represent error responses to client requests.
 */
sealed interface ClientRequestError {
    data object MissingCommandName : ClientRequestError
    data object UnknownCommandName : ClientRequestError
    data object InvalidArguments : ClientRequestError
}
