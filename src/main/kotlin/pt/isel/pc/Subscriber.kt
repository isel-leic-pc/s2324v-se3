package pt.isel.pc

/**
 * A subscriber of messages sent to topics.
 */
interface Subscriber {
    fun send(message: PublishedMessage)
}
