# s2324v-se3

Reference implementation for the Concurrent Programming final project, Spring 2024.

## Design notes

The code in this repository implements a single-node _publish-subscriber_ system (pub-sub), with a TCP/IP interface.

Client systems interact with the pub-sub system via the establishment of TCP/IP connections and the sending of 
_requests_ through these connections.
The available _request_ types are defined in [ClientRequest](src/main/kotlin/pt/isel/pc/protocol/ClientRequest.kt).
For each _request_, the server must reply with a _response_ - 
[ClientResponse](src/main/kotlin/pt/isel/pc/protocol/ClientResponse.kt).

The server can also take the initiative to send messages to clients, without them being responses to requests.
These messages are called _server pushes_ and are defined in [ServerPush](src/main/kotlin/pt/isel/pc/protocol/ServerPush.kt).

The way strings are transformed into _requests_ (i.e. parsing) 
and how _responses_ and _server pushes_ are transformed into strings (i.e. serialization) is defined in 
[ParseAndSerialize](src/main/kotlin/pt/isel/pc/protocol/ParseAndSerialize.kt).

The design for some components of this system is inspired in the [actor model](https://en.wikipedia.org/wiki/Actor_model).

The [Server](src/main/kotlin/pt/isel/pc/Server.kt) is responsible for:
- Creating a server socket and binding it to an IP address and a port.
- Manage the association between topics and subscribers.
- Accept connections from remote clients and create the data structures and threads required to deal with those clients.
- The server is composed by two threads:
  - a _main_ thread, implementing the main server domain logic and state, namely the list of remote clients and topics.
  - a _accept_ thread, responsible for accepting new connections.
- The _main_ thread runs a loop that
  - retrieves _control messages_ from a _control queue_.
  - changes internal state based on those control messages.
  - produces side effects based on those control message.
- With few exceptions (e.g. the server socket), all server state is exclusively managed by the _main_ thread.
- All interaction with the server is done by sending control messages to the server's _control queue_.
- 
The [RemoteClient](src/main/kotlin/pt/isel/pc/RemoteClient.kt) class is responsible to interact with a 
specific connected client, and follows a similar design.
