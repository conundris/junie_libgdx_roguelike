package org.example.network

import com.badlogic.gdx.Gdx
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import org.example.DifficultyLevel
import org.example.GameScreen
import org.example.MapType
import org.example.Player
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Interface for receiving network messages.
 */
interface NetworkMessageListener {
    fun onMessageReceived(message: NetworkMessage)
}

/**
 * Central manager for all network operations.
 */
class NetworkManager {
    companion object {
        const val DEFAULT_TCP_PORT = 54555
        const val DEFAULT_UDP_PORT = 54777
        const val CONNECTION_TIMEOUT = 5000
        const val DISCOVERY_PORT = 54778
    }

    // Network components
    private var server: Server? = null
    private var client: Client? = null

    // State tracking
    private var isServer = false
    private var isClient = false
    private var isConnected = false
    private var clientId = -1
    private var playerName = "Player"

    // Client discovery
    private val discoveredServers = mutableListOf<InetAddress>()

    // Connected clients (server-side)
    private val connectedClients = ConcurrentHashMap<Int, String>() // clientId -> playerName

    // Message handlers
    private val messageListeners = CopyOnWriteArrayList<NetworkMessageListener>()

    // Entity ID tracking
    private var nextEntityId = 1
    private val entityIdMap = ConcurrentHashMap<Any, Int>() // Entity -> ID

    // Start server
    fun startServer(playerName: String): Boolean {
        if (isServer || isClient) return false

        try {
            server = Server().apply {
                NetworkSerializer.registerClasses(kryo)
                start()
                bind(DEFAULT_TCP_PORT, DEFAULT_UDP_PORT)
                addListener(createServerListener())
            }

            isServer = true
            this.playerName = playerName
            clientId = 0 // Server is always client 0
            connectedClients[clientId] = playerName

            return true
        } catch (e: IOException) {
            Gdx.app.error("NetworkManager", "Failed to start server: ${e.message}")
            stopServer()
            return false
        }
    }

    // Stop server
    fun stopServer() {
        server?.stop()
        server = null
        isServer = false
        connectedClients.clear()
        clientId = -1
    }

    // Discover servers
    fun discoverServers(): List<InetAddress> {
        if (isServer || isClient) return emptyList()

        discoveredServers.clear()

        try {
            val tempClient = Client()
            tempClient.start()
            val addresses = tempClient.discoverHosts(DISCOVERY_PORT, 5000)
            discoveredServers.addAll(addresses)
            tempClient.stop()
            return discoveredServers
        } catch (e: IOException) {
            Gdx.app.error("NetworkManager", "Failed to discover servers: ${e.message}")
            return emptyList()
        }
    }

    // Connect to server
    fun connectToServer(address: InetAddress, playerName: String): Boolean {
        if (isServer || isClient) return false

        try {
            client = Client().apply {
                NetworkSerializer.registerClasses(kryo)
                start()
                addListener(createClientListener())
            }

            client?.connect(CONNECTION_TIMEOUT, address, DEFAULT_TCP_PORT, DEFAULT_UDP_PORT)

            isClient = true
            this.playerName = playerName

            val connectionMsg = ConnectionMessage().apply {
                this.playerName = playerName
            }
            sendMessage(connectionMsg)

            return true
        } catch (e: IOException) {
            Gdx.app.error("NetworkManager", "Failed to connect to server: ${e.message}")
            disconnectFromServer()
            return false
        }
    }

    // Disconnect from server
    fun disconnectFromServer() {
        if (isClient) {
            val disconnectionMsg = DisconnectionMessage().apply {
                clientId = this@NetworkManager.clientId
                reason = "Client disconnected"
            }
            sendMessage(disconnectionMsg)

            client?.stop()
            client = null
            isClient = false
            isConnected = false
            clientId = -1
        }
    }

    // Send message
    fun sendMessage(message: NetworkMessage) {
        if (isServer) {
            server?.sendToAllTCP(message)
        } else if (isClient) {
            client?.sendTCP(message)
        }
    }

    // Send message to specific client
    fun sendMessageToClient(clientId: Int, message: NetworkMessage) {
        if (isServer) {
            server?.connections?.find { it.id == clientId }?.sendTCP(message)
        }
    }

    // Add message listener
    fun addMessageListener(listener: NetworkMessageListener) {
        messageListeners.add(listener)
    }

    // Remove message listener
    fun removeMessageListener(listener: NetworkMessageListener) {
        messageListeners.remove(listener)
    }

    // Get entity ID
    fun getEntityId(entity: Any): Int {
        return entityIdMap.computeIfAbsent(entity) { nextEntityId++ }
    }

    // Send player state
    fun sendPlayerState(player: Player) {
        val message = NetworkSerializer.createPlayerStateMessage(clientId, player)
        sendMessage(message)
    }

    // Send game state
    fun sendGameState(gameScreen: GameScreen, mapType: MapType, difficulty: DifficultyLevel) {
        if (isServer) {
            val message = NetworkSerializer.createGameStateMessage(gameScreen, mapType, difficulty)
            sendMessage(message)
        }
    }

    // Create server listener
    private fun createServerListener(): Listener {
        return object : Listener() {
            override fun connected(connection: Connection) {
                Gdx.app.log("NetworkManager", "Client connected: ${connection.id}")
            }

            override fun disconnected(connection: Connection) {
                Gdx.app.log("NetworkManager", "Client disconnected: ${connection.id}")
                val playerName = connectedClients.remove(connection.id)

                val disconnectionMsg = DisconnectionMessage().apply {
                    clientId = connection.id
                    reason = "Client disconnected"
                }
                notifyListeners(disconnectionMsg)
            }

            override fun received(connection: Connection, obj: Any) {
                if (obj is NetworkMessage) {
                    handleServerMessage(connection, obj)
                }
            }
        }
    }

    // Create client listener
    private fun createClientListener(): Listener {
        return object : Listener() {
            override fun connected(connection: Connection) {
                Gdx.app.log("NetworkManager", "Connected to server")
                isConnected = true
            }

            override fun disconnected(connection: Connection) {
                Gdx.app.log("NetworkManager", "Disconnected from server")
                isConnected = false

                val disconnectionMsg = DisconnectionMessage().apply {
                    clientId = clientId
                    reason = "Disconnected from server"
                }
                notifyListeners(disconnectionMsg)

                disconnectFromServer()
            }

            override fun received(connection: Connection, obj: Any) {
                if (obj is NetworkMessage) {
                    handleClientMessage(obj)
                }
            }
        }
    }

    // Handle server message
    private fun handleServerMessage(connection: Connection, message: NetworkMessage) {
        when (message) {
            is ConnectionMessage -> {
                val response = ConnectionMessage().apply {
                    clientId = connection.id
                    playerName = message.playerName
                    isHost = false
                    accepted = true
                    serverMessage = "Welcome to the game!"
                }

                connectedClients[connection.id] = message.playerName
                connection.sendTCP(response)
                notifyListeners(message)
            }
            is DisconnectionMessage -> {
                connectedClients.remove(connection.id)
                notifyListeners(message)
            }
            is InputMessage -> {
                message.clientId = connection.id
                server?.sendToAllExceptTCP(connection.id, message)
                notifyListeners(message)
            }
            else -> {
                server?.sendToAllExceptTCP(connection.id, message)
                notifyListeners(message)
            }
        }
    }

    // Handle client message
    private fun handleClientMessage(message: NetworkMessage) {
        when (message) {
            is ConnectionMessage -> {
                if (message.accepted) {
                    clientId = message.clientId
                    Gdx.app.log("NetworkManager", "Connection accepted, assigned ID: $clientId")
                } else {
                    Gdx.app.log("NetworkManager", "Connection rejected: ${message.serverMessage}")
                    disconnectFromServer()
                }
            }
        }

        notifyListeners(message)
    }

    // Notify listeners
    private fun notifyListeners(message: NetworkMessage) {
        for (listener in messageListeners) {
            listener.onMessageReceived(message)
        }
    }

    // Check if running as server
    fun isServer(): Boolean = isServer

    // Check if running as client
    fun isClient(): Boolean = isClient

    // Check if connected to server
    fun isConnected(): Boolean = isConnected

    // Get client ID
    fun getClientId(): Int = clientId

    // Get player name
    fun getPlayerName(): String = playerName

    // Get connected clients
    fun getConnectedClients(): Map<Int, String> = connectedClients
}
