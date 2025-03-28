package org.example.network

import com.badlogic.gdx.math.Vector2
import org.example.BaseEnemy
import org.example.DifficultyLevel
import org.example.MapType
import org.example.WeaponType

/**
 * Base class for all network messages.
 * All message classes must extend this class to be properly serialized/deserialized.
 */
abstract class NetworkMessage {
    // Message type identifier
    abstract val messageType: MessageType
    
    // Timestamp for ordering and handling latency
    var timestamp: Long = System.currentTimeMillis()
}

/**
 * Enum defining all possible message types.
 * Each message type corresponds to a specific message class.
 */
enum class MessageType {
    CONNECTION,
    DISCONNECTION,
    PLAYER_STATE,
    ENEMY_STATE,
    GAME_STATE,
    INPUT,
    SPAWN,
    DAMAGE,
    EXPERIENCE,
    CHAT
}

/**
 * Message sent when a client connects to the server or when the server
 * acknowledges a client connection.
 */
class ConnectionMessage : NetworkMessage() {
    override val messageType = MessageType.CONNECTION
    
    var clientId: Int = -1
    var playerName: String = ""
    var isHost: Boolean = false
    var accepted: Boolean = false
    var serverMessage: String = ""
}

/**
 * Message sent when a client disconnects from the server.
 */
class DisconnectionMessage : NetworkMessage() {
    override val messageType = MessageType.DISCONNECTION
    
    var clientId: Int = -1
    var reason: String = ""
}

/**
 * Message for synchronizing player state.
 */
class PlayerStateMessage : NetworkMessage() {
    override val messageType = MessageType.PLAYER_STATE
    
    var clientId: Int = -1
    var positionX: Float = 0f
    var positionY: Float = 0f
    var directionX: Float = 0f
    var directionY: Float = 0f
    var health: Int = 100
    var speed: Float = 200f
    var isDashing: Boolean = false
    var isAutoTargeting: Boolean = false
    var weaponType: WeaponType = WeaponType.SIMPLE
    var experiencePoints: Int = 0
    var level: Int = 1
}

/**
 * Message for synchronizing enemy state.
 */
class EnemyStateMessage : NetworkMessage() {
    override val messageType = MessageType.ENEMY_STATE
    
    var enemyId: Int = -1
    var enemyType: String = ""
    var positionX: Float = 0f
    var positionY: Float = 0f
    var health: Int = 0
    var isAlive: Boolean = true
}

/**
 * Message for synchronizing overall game state.
 */
class GameStateMessage : NetworkMessage() {
    override val messageType = MessageType.GAME_STATE
    
    var gameTime: Float = 0f
    var difficultyLevel: Int = 1
    var mapType: MapType = MapType.FOREST
    var difficulty: DifficultyLevel = DifficultyLevel.NORMAL
    var bossSpawned: Boolean = false
    var bossAnnounced: Boolean = false
    var gameOver: Boolean = false
    var gameWon: Boolean = false
}

/**
 * Message for sending player inputs to the server.
 */
class InputMessage : NetworkMessage() {
    override val messageType = MessageType.INPUT
    
    var clientId: Int = -1
    var moveX: Float = 0f
    var moveY: Float = 0f
    var isFiring: Boolean = false
    var isCharging: Boolean = false
    var isDashing: Boolean = false
    var switchWeapon: Boolean = false
    var toggleAutoTarget: Boolean = false
}

/**
 * Message for notifying about new entity spawns.
 */
class SpawnMessage : NetworkMessage() {
    override val messageType = MessageType.SPAWN
    
    var entityType: String = ""  // "player", "enemy", "projectile", "powerup"
    var entityId: Int = -1
    var positionX: Float = 0f
    var positionY: Float = 0f
    var enemyType: String = ""  // For enemy spawns
    var powerUpType: String = ""  // For power-up spawns
}

/**
 * Message for synchronizing damage events.
 */
class DamageMessage : NetworkMessage() {
    override val messageType = MessageType.DAMAGE
    
    var sourceId: Int = -1  // ID of damage source (player, projectile)
    var targetId: Int = -1  // ID of damage target (player, enemy)
    var damage: Int = 0
    var isCritical: Boolean = false
}

/**
 * Message for synchronizing experience and level-ups.
 */
class ExperienceMessage : NetworkMessage() {
    override val messageType = MessageType.EXPERIENCE
    
    var clientId: Int = -1
    var experienceGained: Int = 0
    var totalExperience: Int = 0
    var newLevel: Int = 0
    var isLevelUp: Boolean = false
}

/**
 * Message for chat functionality.
 */
class ChatMessage : NetworkMessage() {
    override val messageType = MessageType.CHAT
    
    var clientId: Int = -1
    var playerName: String = ""
    var message: String = ""
    var isSystemMessage: Boolean = false
}