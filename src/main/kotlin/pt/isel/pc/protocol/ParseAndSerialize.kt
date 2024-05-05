package pt.isel.pc.protocol

import pt.isel.pc.TopicName
import pt.isel.pc.utils.SuccessOrError

interface ClientRequestParser {
    val name: String
    fun parse(args: List<String>, line: String): SuccessOrError<ClientRequest, ClientRequestError>
}

object PublishParser : ClientRequestParser {
    override val name = "PUBLISH"
    override fun parse(args: List<String>, line: String): SuccessOrError<ClientRequest, ClientRequestError> {
        if (args.isEmpty()) {
            return SuccessOrError.error(ClientRequestError.InvalidArguments)
        }
        val topicName = args[0]
        var index = name.length
        // skip spaces
        while (line[index] == ' ') {
            index += 1
        }
        // skip topic name
        index += topicName.length
        val message = if (index + 1 >= line.length) {
            ""
        } else {
            line.substring(index + 1)
        }
        return SuccessOrError.success(ClientRequest.Publish(TopicName(topicName), message))
    }
}

object SubscribeParser : ClientRequestParser {
    override val name = "SUBSCRIBE"

    override fun parse(args: List<String>, line: String): SuccessOrError<ClientRequest, ClientRequestError> {
        if (args.isEmpty()) {
            return SuccessOrError.error(ClientRequestError.InvalidArguments)
        }
        return SuccessOrError.success(ClientRequest.Subscribe(args.map { TopicName(it) }))
    }
}

object UnsubscribeParser : ClientRequestParser {
    override val name = "UNSUBSCRIBE"
    override fun parse(args: List<String>, line: String): SuccessOrError<ClientRequest, ClientRequestError> {
        if (args.isEmpty()) {
            return SuccessOrError.error(ClientRequestError.InvalidArguments)
        }
        return SuccessOrError.success(ClientRequest.Unsubscribe(args.map { TopicName(it) }))
    }
}

private val clientRequestParsers = listOf(
    UnsubscribeParser,
    SubscribeParser,
    PublishParser,
).associateBy { it.name }

fun parseClientRequest(line: String): SuccessOrError<ClientRequest, ClientRequestError> {
    val trimmedLine = line.trimStart()
    val parts = trimmedLine.split(" ")
    if (parts.isEmpty() || parts[0].isEmpty()) {
        return SuccessOrError.error(ClientRequestError.MissingCommandName)
    }
    val commandName = parts[0]
    val args = parts.subList(1, parts.size)
    return clientRequestParsers[commandName]?.parse(args, trimmedLine)
        ?: SuccessOrError.error(ClientRequestError.UnknownCommandName)
}

fun serialize(error: ClientRequestError): String =
    when (error) {
        is ClientRequestError.InvalidArguments -> "INVALID_ARGUMENTS"
        ClientRequestError.MissingCommandName -> "MISSING_COMMAND_NAME"
        ClientRequestError.UnknownCommandName -> "UNKNOWN_COMMAND_NAME"
    }

fun serialize(response: ClientResponse): String =
    when (response) {
        is ClientResponse.Error -> "-${serialize(response.error)}"
        is ClientResponse.OkPublish -> "+"
        ClientResponse.OkSubscribe -> "+"
        ClientResponse.OkUnsubscribe -> "+"
    }

fun serialize(serverPush: ServerPush): String =
    when (serverPush) {
        is ServerPush.PublishedMessage -> ">${serverPush.message.topicName.value} ${serverPush.message.content}"
        is ServerPush.Hi -> "!hi"
        is ServerPush.Bye -> "!bye"
    }
