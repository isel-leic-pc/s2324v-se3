package pt.isel.pc

/**
 * Represents a message published to a topic.
 */
data class PublishedMessage(
    val topicName: TopicName,
    val content: String,
)
