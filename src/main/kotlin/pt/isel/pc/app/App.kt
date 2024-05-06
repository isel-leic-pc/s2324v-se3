package pt.isel.pc.app

import org.slf4j.LoggerFactory
import pt.isel.pc.Server
import java.net.InetSocketAddress

fun main() {
    // start server
    val server = Server.start(InetSocketAddress("0.0.0.0", 8080))
    logger.info("Started server")

    // register shutdown hook
    val shutdownThread = Thread {
        logger.info("Starting shutdown process")
        server.shutdown()
        server.join()
    }
    Runtime.getRuntime().addShutdownHook(shutdownThread)

    // wait for server to end
    logger.info("Waiting for server to end")
    server.join()
    logger.info("main ending")
}

private val logger = LoggerFactory.getLogger("App")
