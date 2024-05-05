package pt.isel.pc

import pt.isel.pc.protocol.ClientRequest
import pt.isel.pc.protocol.ClientRequestError
import pt.isel.pc.protocol.ClientResponse
import pt.isel.pc.protocol.ServerPush
import pt.isel.pc.protocol.parseClientRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseTests {

    @Test
    fun `test success parse cases`() {
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), "hello world"),
            parseClientRequest("PUBLISH t1 hello world").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), "hello world"),
            parseClientRequest(" PUBLISH t1 hello world").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), " hello world"),
            parseClientRequest("PUBLISH t1  hello world").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), " hello world  "),
            parseClientRequest("PUBLISH t1  hello world  ").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), ""),
            parseClientRequest("PUBLISH t1").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), ""),
            parseClientRequest("PUBLISH t1 ").successOrThrow,
        )
        assertEquals(
            ClientRequest.Publish(TopicName("t1"), ""),
            parseClientRequest("  PUBLISH t1 ").successOrThrow,
        )
    }

    @Test
    fun `test error parse cases`() {
        assertEquals(
            ClientRequestError.MissingCommandName,
            parseClientRequest("").errorOrThrow,
        )
        assertEquals(
            ClientRequestError.MissingCommandName,
            parseClientRequest(" ").errorOrThrow,
        )
        assertEquals(
            ClientRequestError.UnknownCommandName,
            parseClientRequest("NOT-A-COMMAND").errorOrThrow,
        )
        assertEquals(
            ClientRequestError.InvalidArguments,
            parseClientRequest("PUBLISH").errorOrThrow,
        )
        assertEquals(
            ClientRequestError.InvalidArguments,
            parseClientRequest("SUBSCRIBE").errorOrThrow,
        )
    }

    @Test
    fun `test toString`() {
        assertEquals(
            "+",
            pt.isel.pc.protocol.serialize(ClientResponse.OkPublish),
        )

        assertEquals(
            "+",
            pt.isel.pc.protocol.serialize(ClientResponse.OkSubscribe),
        )

        assertEquals(
            "+",
            pt.isel.pc.protocol.serialize(ClientResponse.OkUnsubscribe),
        )

        assertEquals(
            "-INVALID_ARGUMENTS",
            pt.isel.pc.protocol.serialize(ClientResponse.Error(ClientRequestError.InvalidArguments)),
        )

        assertEquals(
            ">the-topic the content",
            pt.isel.pc.protocol.serialize(
                ServerPush.PublishedMessage(
                    PublishedMessage(TopicName("the-topic"), "the content"),
                ),
            ),
        )
    }
}
