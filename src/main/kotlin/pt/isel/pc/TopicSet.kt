package pt.isel.pc

/**
 * Represents a set of topics, as well as the subscribers to those topics.
 */
class TopicSet {

    private val topicsByName = mutableMapOf<TopicName, Topic>()
    private val topicsBySubscriber = mutableMapOf<Subscriber, MutableSet<Topic>>()

    fun subscribe(name: TopicName, subscriber: Subscriber) {
        val topic = topicsByName.computeIfAbsent(name) { Topic(name) }
        topic.subscribers.add(subscriber)
        topicsBySubscriber.computeIfAbsent(subscriber) { mutableSetOf() }.add(topic)
    }

    fun unsubscribe(name: TopicName, subscriber: Subscriber) {
        val topic = topicsByName[name] ?: return
        topic.subscribers.remove(subscriber)
        if (topic.subscribers.isEmpty()) {
            topicsByName.remove(name)
        }
        val topicSet = topicsBySubscriber[subscriber] ?: return
        topicSet.remove(topic)
        if (topicSet.isEmpty()) {
            topicsBySubscriber.remove(subscriber)
        }
    }

    fun unsubscribe(subscriber: Subscriber) {
        val topicSet = topicsBySubscriber[subscriber] ?: return
        topicSet.toList().forEach {
            unsubscribe(it.name, subscriber)
        }
    }

    fun getSubscribersFor(name: TopicName): Set<Subscriber> =
        topicsByName[name]?.subscribers?.toSet() ?: setOf()

    fun getTopicWithName(topicName: TopicName): Topic? = topicsByName[topicName]
}
