package pt.isel.pc.protocol

/**
 * Sealed hierarchy to represent success or error responses to client requests
 */
sealed interface ClientResponse {
    data object OkPublish : ClientResponse
    data object OkSubscribe : ClientResponse
    data object OkUnsubscribe : ClientResponse
    data class Error(val error: ClientRequestError) : ClientResponse
}
