package pt.isel.pc

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class TopicSetTests {

    @Test
    fun `basic functionality tests`() {
        // given: a topic set
        val topicSet = TopicSet()

        // and: arrays of topic names and subscriber
        val topicNames = Array(3) {
            TopicName(it.toString())
        }
        val subscribers = Array(3) {
            TestSubscriber()
        }

        // when: adding some subscribers to topics
        repeat(3) {
            topicSet.subscribe(topicNames[0], subscribers[it])
        }
        repeat(2) {
            topicSet.subscribe(topicNames[1], subscribers[it])
        }
        repeat(1) {
            topicSet.subscribe(topicNames[2], subscribers[it])
        }

        // then:
        assertEquals(3, topicSet.getSubscribersFor(topicNames[0]).size)
        assertEquals(2, topicSet.getSubscribersFor(topicNames[1]).size)
        assertEquals(1, topicSet.getSubscribersFor(topicNames[2]).size)

        // when: removing a subscriber from a topic 0
        topicSet.unsubscribe(topicNames[0], subscribers[2])

        // then: topic 0 has only two subscribers
        assertEquals(2, topicSet.getSubscribersFor(topicNames[0]).size)

        // when: removing a subscriber 2
        topicSet.unsubscribe(subscribers[2])

        // then: topic 2 does not exist anymore
        topicSet.getTopicWithName(topicNames[2])
    }

    private class TestSubscriber : Subscriber {
        override fun send(message: PublishedMessage) {
            // Nothing, for testing purposes only
        }
    }
}
