package pt.isel.pc

import org.slf4j.LoggerFactory
import pt.isel.pc.protocol.ClientRequest
import pt.isel.pc.protocol.ClientResponse
import pt.isel.pc.protocol.ServerPush
import pt.isel.pc.protocol.parseClientRequest
import pt.isel.pc.protocol.serialize
import pt.isel.pc.utils.SuccessOrError
import pt.isel.pc.utils.sendLine
import java.io.BufferedWriter
import java.io.Writer
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * The component responsible to interact with a remote client, via a [Socket].
 */
class RemoteClient private constructor(
    private val server: Server,
    val clientId: String,
    private val clientSocket: Socket,
) : Subscriber {
    private val controlQueue = LinkedBlockingQueue<ControlMessage>()
    private val controlThread: Thread
    private val readThread: Thread
    private var state = State.RUNNING

    init {
        controlThread = thread(isDaemon = true) {
            logger.info("[{}] Remote client started main thread", clientId)
            controlLoop()
        }
        readThread = thread(isDaemon = true) {
            logger.info("[{}] Remote client started read thread", clientId)
            readLoop()
        }
    }

    fun shutdown() {
        controlQueue.put(ControlMessage.Shutdown)
    }

    override fun send(message: PublishedMessage) {
        controlQueue.put(ControlMessage.Message(message))
    }

    private fun handleShutdown(writer: Writer) {
        if (state != State.RUNNING) {
            return
        }
        writer.sendLine(serialize(ServerPush.Bye))
        clientSocket.close()
        state = State.SHUTTING_DOWN
    }

    private fun handleMessage(writer: BufferedWriter, message: PublishedMessage) {
        if (state != State.RUNNING) {
            return
        }
        writer.sendLine(serialize(ServerPush.PublishedMessage(message)))
    }

    private fun handleClientSocketLine(writer: BufferedWriter, line: String) {
        if (state != State.RUNNING) {
            return
        }
        val response = when (val res = parseClientRequest(line)) {
            is SuccessOrError.Success -> when (val request = res.value) {
                is ClientRequest.Publish -> {
                    server.publish(PublishedMessage(request.topic, request.message))
                    ClientResponse.OkPublish
                }

                is ClientRequest.Subscribe -> {
                    request.topics.forEach {
                        server.subscribe(it, this)
                    }
                    ClientResponse.OkSubscribe
                }

                is ClientRequest.Unsubscribe -> {
                    request.topics.forEach {
                        server.unsubscribe(it, this)
                    }
                    ClientResponse.OkUnsubscribe
                }
            }

            is SuccessOrError.Error -> {
                ClientResponse.Error(res.error)
            }
        }
        writer.sendLine(serialize(response))
    }

    private fun handleClientSocketError(throwable: Throwable) {
        logger.info("Client socket operation thrown: {}", throwable.message)
    }

    private fun handleClientSocketEnded() {
        if (state != State.RUNNING) {
            return
        }
        state = State.SHUTTING_DOWN
    }

    private fun handleReadLoopEnded() {
        state = State.SHUTDOWN
    }

    private fun controlLoop() {
        try {
            clientSocket.getOutputStream().bufferedWriter().use { writer ->
                writer.sendLine(serialize(ServerPush.Hi))
                while (state != State.SHUTDOWN) {
                    val controlMessage = controlQueue.take()
                    logger.info("[{}] main thread received {}", clientId, controlMessage)
                    when (controlMessage) {
                        ControlMessage.Shutdown -> handleShutdown(writer)
                        is ControlMessage.Message -> handleMessage(writer, controlMessage.value)
                        is ControlMessage.ClientSocketLine -> handleClientSocketLine(writer, controlMessage.value)
                        ControlMessage.ClientSocketEnded -> handleClientSocketEnded()

                        is ControlMessage.ClientSocketError -> handleClientSocketError(controlMessage.throwable)

                        ControlMessage.ReadLoopEnded -> handleReadLoopEnded()
                    }
                }
            }
        } finally {
            logger.info("[{}] remote client ending", clientId)
            server.remoteClientEnded(this)
        }
    }

    private fun readLoop() {
        clientSocket.getInputStream().bufferedReader().use { reader ->
            try {
                while (true) {
                    val line: String? = reader.readLine()
                    if (line == null) {
                        logger.info("[{}] end of input stream reached", clientId)
                        controlQueue.put(ControlMessage.ClientSocketEnded)
                        return
                    }
                    logger.info("[{}] line received: {}", clientId, line)
                    controlQueue.put(ControlMessage.ClientSocketLine(line))
                }
            } catch (ex: Throwable) {
                logger.info("[{}]Exception on read loop: {}, {}", clientId, ex.javaClass.name, ex.message)
                controlQueue.put(ControlMessage.ClientSocketError(ex))
            } finally {
                logger.info("[{}] client loop ending", clientId)
                controlQueue.put(ControlMessage.ReadLoopEnded)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RemoteClient::class.java)
        fun start(server: Server, clientId: String, socket: Socket): RemoteClient {
            return RemoteClient(
                server,
                clientId,
                socket,
            )
        }
    }

    private sealed interface ControlMessage {
        data class Message(val value: PublishedMessage) : ControlMessage
        data object Shutdown : ControlMessage
        data object ClientSocketEnded : ControlMessage
        data class ClientSocketError(val throwable: Throwable) : ControlMessage
        data class ClientSocketLine(val value: String) : ControlMessage
        data object ReadLoopEnded : ControlMessage
    }

    private enum class State {
        RUNNING,
        SHUTTING_DOWN,
        SHUTDOWN,
    }
}
