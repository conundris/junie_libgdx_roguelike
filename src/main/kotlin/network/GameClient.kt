package org.example.network

import com.badlogic.gdx.Gdx
import org.example.BaseEnemy
import org.example.GameScreen
import org.example.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Handles client-side game logic and state synchronization.
 * This class is responsible for sending player inputs to the server
 * and updating the local game state based on messages from the server.
 */
class GameClient(
    private val networkManager: NetworkManager,
    private val gameScreen: GameScreen
) {
    // Remote players (clientId -> Player)
    private val remotePlayers = ConcurrentHashMap<Int, Player>()
    
    // Input synchronization scheduler
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    
    // Sync intervals
    private val inputSyncInterval = 50L // 20 times per second
    
    // Client state
    private var isRunning = false
    
    /**
     * Starts the game client and begins input synchronization.
     */
    fun start() {
        if (isRunning) return
        
        isRunning = true
        
        // Start input synchronization
        startInputSync()
        
        // Listen for network messages
        networkManager.addMessageListener(createMessageListener())
        
        Gdx.app.log("GameClient", "Client started")
    }
    
    /**
     * Stops the game client and cleans up resources.
     */
    fun stop() {
        if (!isRunning) return
        
        isRunning = false
        
        // Stop synchronization tasks
        scheduler.shutdown()
        
        // Remove message listener
        networkManager.removeMessageListener(createMessageListener())
        
        // Clear remote players
        remotePlayers.clear()
        
        Gdx.app.log("GameClient", "Client stopped")
    }
    
    /**
     * Starts the input synchronization task.
     */
    private fun startInputSync() {
        scheduler.scheduleAtFixedRate({
            if (!isRunning) return@scheduleAtFixedRate
            
            // Create input message based on current player state
            val inputMessage = createInputMessage()
            
            // Send input to server
            networkManager.sendMessage(inputMessage)
            
        }, 0, inputSyncInterval, TimeUnit.MILLISECONDS)
    }
    
    /**
     * Creates an input message based on the current player state.
     */
    private fun createInputMessage(): InputMessage {
        // In a real implementation, we would capture actual player input
        // For now, we'll just create a simple message with the player's direction
        return InputMessage().apply {
            clientId = networkManager.getClientId()
            moveX = gameScreen.player.direction.x
            moveY = gameScreen.player.direction.y
            isFiring = true // Assume always firing for simplicity
            isCharging = false
            isDashing = false
            switchWeapon = false
            toggleAutoTarget = false
        }
    }
    
    /**
     * Creates a listener for network messages.
     */
    private fun createMessageListener(): NetworkMessageListener {
        return object : NetworkMessageListener {
            override fun onMessageReceived(message: NetworkMessage) {
                when (message) {
                    is PlayerStateMessage -> handlePlayerStateMessage(message)
                    is EnemyStateMessage -> handleEnemyStateMessage(message)
                    is GameStateMessage -> handleGameStateMessage(message)
                    is SpawnMessage -> handleSpawnMessage(message)
                    is DamageMessage -> handleDamageMessage(message)
                    // Other message types can be handled as needed
                }
            }
        }
    }
    
    /**
     * Handles a player state message from the server.
     */
    private fun handlePlayerStateMessage(message: PlayerStateMessage) {
        val clientId = message.clientId
        
        // Ignore messages about our own player
        if (clientId == networkManager.getClientId()) return
        
        // Get or create remote player
        val player = remotePlayers.getOrPut(clientId) {
            // Create a new player for this client
            // In a real implementation, we would need to add this player to the game world
            Player().apply {
                // Set initial position
                position.set(message.positionX, message.positionY)
            }
        }
        
        // Update player state based on message
        player.position.set(message.positionX, message.positionY)
        player.direction.set(message.directionX, message.directionY)
        player.health = message.health
        player.speed = message.speed
        // Other properties would be updated here
        
        Gdx.app.log("GameClient", "Updated remote player $clientId: pos=(${player.position.x}, ${player.position.y})")
    }
    
    /**
     * Handles an enemy state message from the server.
     */
    private fun handleEnemyStateMessage(message: EnemyStateMessage) {
        val enemyId = message.enemyId
        
        // Find the enemy in the game world
        val enemy = gameScreen.enemies.find { networkManager.getEntityId(it) == enemyId }
        
        if (enemy != null) {
            // Update existing enemy
            if (message.isAlive) {
                enemy.position.set(message.positionX, message.positionY)
                enemy.health = message.health
            } else {
                // Enemy is dead, remove it
                // In a real implementation, we would need to handle this properly
                // For now, we'll just log it
                Gdx.app.log("GameClient", "Enemy $enemyId is dead")
            }
        } else if (message.isAlive) {
            // Enemy doesn't exist locally but is alive on server
            // In a real implementation, we would need to create this enemy
            // For now, we'll just log it
            Gdx.app.log("GameClient", "New enemy $enemyId: type=${message.enemyType}, pos=(${message.positionX}, ${message.positionY})")
        }
    }
    
    /**
     * Handles a game state message from the server.
     */
    private fun handleGameStateMessage(message: GameStateMessage) {
        // Update game state based on message
        // In a real implementation, we would update various game properties
        
        Gdx.app.log("GameClient", "Game state update: time=${message.gameTime}, difficulty=${message.difficultyLevel}")
    }
    
    /**
     * Handles a spawn message from the server.
     */
    private fun handleSpawnMessage(message: SpawnMessage) {
        // Handle entity spawning
        when (message.entityType) {
            "player" -> {
                // A new player has joined
                Gdx.app.log("GameClient", "New player spawned: ID=${message.entityId}")
                // In a real implementation, we would create a new player
            }
            "enemy" -> {
                // A new enemy has spawned
                Gdx.app.log("GameClient", "New enemy spawned: ID=${message.entityId}, type=${message.enemyType}")
                // In a real implementation, we would create a new enemy
            }
            "powerup" -> {
                // A new power-up has spawned
                Gdx.app.log("GameClient", "New power-up spawned: ID=${message.entityId}, type=${message.powerUpType}")
                // In a real implementation, we would create a new power-up
            }
        }
    }
    
    /**
     * Handles a damage message from the server.
     */
    private fun handleDamageMessage(message: DamageMessage) {
        // Handle damage event
        Gdx.app.log("GameClient", "Damage event: source=${message.sourceId}, target=${message.targetId}, damage=${message.damage}")
        
        // In a real implementation, we would apply the damage to the target
    }
    
    /**
     * Gets the number of remote players.
     */
    fun getRemotePlayerCount(): Int = remotePlayers.size
    
    /**
     * Gets the map of remote players.
     */
    fun getRemotePlayers(): Map<Int, Player> = remotePlayers
    
    /**
     * Checks if the client is running.
     */
    fun isRunning(): Boolean = isRunning
}