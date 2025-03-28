package org.example.network

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.kryo.Kryo
import org.example.BaseEnemy
import org.example.BasicEnemy
import org.example.BossEnemy
import org.example.DifficultyLevel
import org.example.FastEnemy
import org.example.MapType
import org.example.PowerUpType
import org.example.RangedEnemy
import org.example.TankEnemy
import org.example.WeaponType

/**
 * Handles serialization and deserialization of network messages.
 * This class registers all message types with KryoNet and provides
 * utility methods for working with network messages.
 */
class NetworkSerializer {
    companion object {
        /**
         * Registers all classes that need to be serialized/deserialized with KryoNet.
         * This method must be called on both client and server before any network communication.
         *
         * @param kryo The Kryo instance to register classes with
         */
        fun registerClasses(kryo: Kryo) {
            // Register message base class and enum
            kryo.register(NetworkMessage::class.java)
            kryo.register(MessageType::class.java)

            // Register all message types
            kryo.register(ConnectionMessage::class.java)
            kryo.register(DisconnectionMessage::class.java)
            kryo.register(PlayerStateMessage::class.java)
            kryo.register(EnemyStateMessage::class.java)
            kryo.register(GameStateMessage::class.java)
            kryo.register(InputMessage::class.java)
            kryo.register(SpawnMessage::class.java)
            kryo.register(DamageMessage::class.java)
            kryo.register(ExperienceMessage::class.java)
            kryo.register(ChatMessage::class.java)

            // Register game-specific classes and enums
            kryo.register(Vector2::class.java)
            kryo.register(WeaponType::class.java)
            kryo.register(MapType::class.java)
            kryo.register(DifficultyLevel::class.java)
            kryo.register(PowerUpType::class.java)

            // Register arrays and collections that might be used
            kryo.register(Array<Any>::class.java)
            kryo.register(ArrayList::class.java)
            kryo.register(HashMap::class.java)
            kryo.register(HashSet::class.java)

            // Register primitive arrays
            kryo.register(ByteArray::class.java)
            kryo.register(IntArray::class.java)
            kryo.register(FloatArray::class.java)
            kryo.register(BooleanArray::class.java)
        }

        /**
         * Creates a player state message from the current player state.
         *
         * @param clientId The client ID
         * @param player The player object
         * @return A PlayerStateMessage containing the player's current state
         */
        fun createPlayerStateMessage(clientId: Int, player: org.example.Player): PlayerStateMessage {
            return PlayerStateMessage().apply {
                this.clientId = clientId
                this.positionX = player.position.x
                this.positionY = player.position.y
                this.directionX = player.direction.x
                this.directionY = player.direction.y
                this.health = player.health
                this.speed = player.speed
                this.isDashing = false // This would need to be exposed in Player class
                this.isAutoTargeting = player.isAutoTargeting
                this.weaponType = player.weapon.getCurrentWeaponType()
                this.experiencePoints = player.experience.currentExp
                this.level = player.experience.level
            }
        }

        /**
         * Creates an enemy state message from an enemy object.
         *
         * @param enemyId The unique ID for this enemy
         * @param enemy The enemy object
         * @return An EnemyStateMessage containing the enemy's current state
         */
        fun createEnemyStateMessage(enemyId: Int, enemy: BaseEnemy): EnemyStateMessage {
            return EnemyStateMessage().apply {
                this.enemyId = enemyId
                this.enemyType = getEnemyTypeName(enemy)
                this.positionX = enemy.position.x
                this.positionY = enemy.position.y
                this.health = enemy.health
                this.isAlive = enemy.isAlive()
            }
        }

        /**
         * Creates a game state message from the current game state.
         *
         * @param gameScreen The game screen object
         * @param mapType The current map type
         * @param difficulty The current difficulty level
         * @return A GameStateMessage containing the current game state
         */
        fun createGameStateMessage(
            gameScreen: org.example.GameScreen,
            mapType: MapType,
            difficulty: DifficultyLevel
        ): GameStateMessage {
            return GameStateMessage().apply {
                this.gameTime = gameScreen.gameTime
                this.difficultyLevel = gameScreen.difficultyLevel
                this.mapType = mapType
                this.difficulty = difficulty
                this.bossSpawned = gameScreen.bossSpawned
                this.bossAnnounced = gameScreen.bossAnnounced
                this.gameOver = !gameScreen.player.isAlive()
                this.gameWon = false // This would need to be determined by game logic
            }
        }

        /**
         * Gets the type name of an enemy for serialization.
         *
         * @param enemy The enemy object
         * @return A string representing the enemy type
         */
        private fun getEnemyTypeName(enemy: BaseEnemy): String {
            return when (enemy) {
                is BasicEnemy -> "BasicEnemy"
                is FastEnemy -> "FastEnemy"
                is TankEnemy -> "TankEnemy"
                is RangedEnemy -> "RangedEnemy"
                is BossEnemy -> "BossEnemy"
                else -> "UnknownEnemy"
            }
        }
    }
}
