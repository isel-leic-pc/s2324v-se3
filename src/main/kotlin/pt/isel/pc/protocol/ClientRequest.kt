package pt.isel.pc.protocol

import pt.isel.pc.TopicName

/**
 * Sealed hierarchy (i.e. union type) to represent client requests.
 */
sealed interface ClientRequest {
    data class Publish(val topic: TopicName, val message: String) : ClientRequest
    data class Subscribe(val topics: List<TopicName>) : ClientRequest
    data class Unsubscribe(val topics: List<TopicName>) : ClientRequest
}
