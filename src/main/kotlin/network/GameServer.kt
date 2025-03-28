package org.example.network

import com.badlogic.gdx.Gdx
import org.example.BaseEnemy
import org.example.DifficultyLevel
import org.example.GameScreen
import org.example.MapType
import org.example.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Handles server-side game logic and state synchronization.
 * This class manages connected clients, spawns enemies, and ensures
 * all clients have a consistent view of the game state.
 */
class GameServer(
    private val networkManager: NetworkManager,
    private val gameScreen: GameScreen,
    private val mapType: MapType,
    private val difficulty: DifficultyLevel
) {
    // Connected players (clientId -> Player)
    private val players = ConcurrentHashMap<Int, Player>()
    
    // Enemy tracking (enemyId -> Enemy)
    private val enemies = ConcurrentHashMap<Int, BaseEnemy>()
    
    // Synchronization scheduler
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    // Sync intervals
    private val playerSyncInterval = 100L // 10 times per second
    private val enemySyncInterval = 200L  // 5 times per second
    private val gameSyncInterval = 1000L  // Once per second
    
    // Server state
    private var isRunning = false
    
    /**
     * Starts the game server and begins synchronization tasks.
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        
        // Add local player
        players[networkManager.getClientId()] = gameScreen.player
        
        // Start synchronization tasks
        startPlayerSync()
        startEnemySync()
        startGameStateSync()
        
        // Listen for network messages
        networkManager.addMessageListener(createMessageListener())
        
        Gdx.app.log("GameServer", "Server started")
    }
    
    /**
     * Stops the game server and cleans up resources.
     */
    fun stop() {
        if (!isRunning) return
        
        isRunning = false
        
        // Stop synchronization tasks
        scheduler.shutdown()
        
        // Remove message listener
        networkManager.removeMessageListener(createMessageListener())
        
        Gdx.app.log("GameServer", "Server stopped")
    }
    
    /**
     * Starts the player synchronization task.
     */
    private fun startPlayerSync() {
        scheduler.scheduleAtFixedRate({
            if (!isRunning) return@scheduleAtFixedRate
            
            // Synchronize local player state
            val localPlayer = gameScreen.player
            val message = NetworkSerializer.createPlayerStateMessage(
                networkManager.getClientId(),
                localPlayer
            )
            networkManager.sendMessage(message)
            
        }, 0, playerSyncInterval, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Starts the enemy synchronization task.
     */
    private fun startEnemySync() {
        scheduler.scheduleAtFixedRate({
            if (!isRunning) return@scheduleAtFixedRate
            
            // Synchronize all enemies
            gameScreen.enemies.forEach { enemy ->
                val enemyId = networkManager.getEntityId(enemy)
                enemies[enemyId] = enemy
                
                val message = NetworkSerializer.createEnemyStateMessage(enemyId, enemy)
                networkManager.sendMessage(message)
            }
            
            // Remove enemies that no longer exist
            val currentEnemyIds = gameScreen.enemies.map { networkManager.getEntityId(it) }.toSet()
            val removedEnemyIds = enemies.keys.filter { it !in currentEnemyIds }
            
            removedEnemyIds.forEach { enemyId ->
                enemies.remove(enemyId)
                
                // Send message that enemy is dead
                val message = EnemyStateMessage().apply {
                    this.enemyId = enemyId
                    this.isAlive = false
                }
                networkManager.sendMessage(message)
            }
            
        }, 0, enemySyncInterval, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Starts the game state synchronization task.
     */
    private fun startGameStateSync() {
        scheduler.scheduleAtFixedRate({
            if (!isRunning) return@scheduleAtFixedRate
            
            // Synchronize game state
            val message = NetworkSerializer.createGameStateMessage(gameScreen, mapType, difficulty)
            networkManager.sendMessage(message)
            
        }, 0, gameSyncInterval, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Creates a listener for network messages.
     */
    private fun createMessageListener(): NetworkMessageListener {
        return object : NetworkMessageListener {
            override fun onMessageReceived(message: NetworkMessage) {
                when (message) {
                    is ConnectionMessage -> handleConnectionMessage(message)
                    is DisconnectionMessage -> handleDisconnectionMessage(message)
                    is PlayerStateMessage -> handlePlayerStateMessage(message)
                    is InputMessage -> handleInputMessage(message)
                    // Other message types can be handled as needed
                }
            }
        }
    }
    
    /**
     * Handles a connection message from a client.
     */
    private fun handleConnectionMessage(message: ConnectionMessage) {
        // A new client has connected
        val clientId = message.clientId
        
        // Create a new player for this client
        // In a real implementation, we would need to spawn the player at a valid location
        val player = Player()
        players[clientId] = player
        
        Gdx.app.log("GameServer", "Player connected: ${message.playerName} (ID: $clientId)")
        
        // Send current game state to the new client
        val gameStateMessage = NetworkSerializer.createGameStateMessage(gameScreen, mapType, difficulty)
        networkManager.sendMessageToClient(clientId, gameStateMessage)
        
        // Send all existing enemies to the new client
        enemies.forEach { (enemyId, enemy) ->
            val enemyStateMessage = NetworkSerializer.createEnemyStateMessage(enemyId, enemy)
            networkManager.sendMessageToClient(clientId, enemyStateMessage)
        }
        
        // Send all existing players to the new client
        players.forEach { (playerId, player) ->
            val playerStateMessage = NetworkSerializer.createPlayerStateMessage(playerId, player)
            networkManager.sendMessageToClient(clientId, playerStateMessage)
        }
    }
    
    /**
     * Handles a disconnection message from a client.
     */
    private fun handleDisconnectionMessage(message: DisconnectionMessage) {
        // A client has disconnected
        val clientId = message.clientId
        
        // Remove the player
        players.remove(clientId)
        
        Gdx.app.log("GameServer", "Player disconnected: ID $clientId")
    }
    
    /**
     * Handles a player state message from a client.
     */
    private fun handlePlayerStateMessage(message: PlayerStateMessage) {
        // Update the player state
        val clientId = message.clientId
        val player = players[clientId] ?: return
        
        // Update player state based on message
        // In a real implementation, we would validate the state and apply it
        // For now, we just forward the message to all other clients
        networkManager.sendMessage(message)
    }
    
    /**
     * Handles an input message from a client.
     */
    private fun handleInputMessage(message: InputMessage) {
        // Process client input
        val clientId = message.clientId
        val player = players[clientId] ?: return
        
        // Apply input to player
        // In a real implementation, we would validate and apply the input
        // For now, we just forward the message to all other clients
        networkManager.sendMessage(message)
    }
    
    /**
     * Gets the number of connected players.
     */
    fun getPlayerCount(): Int = players.size
    
    /**
     * Gets the map of connected players.
     */
    fun getPlayers(): Map<Int, Player> = players
    
    /**
     * Gets the map of active enemies.
     */
    fun getEnemies(): Map<Int, BaseEnemy> = enemies
    
    /**
     * Checks if the server is running.
     */
    fun isRunning(): Boolean = isRunning
}