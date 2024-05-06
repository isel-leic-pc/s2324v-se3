package pt.isel.pc

/**
 * Represents a topic and the subscribers to those topics.
 *
 */
class Topic(
    val name: TopicName,
) {
    val subscribers = mutableSetOf<Subscriber>()
}
