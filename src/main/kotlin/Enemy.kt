package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

abstract class BaseEnemy(startX: Float, startY: Float) {
    val position = Vector2(startX, startY)
    var size = 24f
    var speed = 100f
    var health = 30
    val bounds = Rectangle()
    var damage = 10
    var expValue = 20  // Experience points dropped when killed

    init {
        updateBounds()
    }

    abstract fun updateBehavior(delta: Float, playerPosition: Vector2)
    abstract val color: Triple<Float, Float, Float>  // RGB color values

    fun updateBounds() {
        bounds.set(position.x, position.y, size, size)
    }

    open fun update(delta: Float, playerPosition: Vector2) {
        updateBehavior(delta, playerPosition)
        updateBounds()
    }

    open fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(color.first, color.second, color.third, 1f)
        shapeRenderer.rect(position.x, position.y, size, size)
        shapeRenderer.end()
    }

    open fun takeDamage(damage: Int) {
        health -= damage
        if (health < 0) health = 0
    }

    open fun isAlive(): Boolean = health > 0

    open fun overlaps(player: Player): Boolean {
        return bounds.overlaps(player.bounds)
    }
}

open class BasicEnemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(1f, 0f, 0f)  // Red

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Basic following behavior
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx)
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta
    }
}

open class FastEnemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(0f, 1f, 0f)  // Green

    init {
        size = 16f      // Smaller size
        speed = 150f    // Faster speed
        health = 20     // Less health
        damage = 5      // Less damage
        expValue = 30   // More experience
    }

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Fast, erratic movement
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx) + (sin(System.currentTimeMillis() * 0.01f) * 0.5f)
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta
    }
}

open class TankEnemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(0f, 0f, 1f)  // Blue

    init {
        size = 32f      // Larger size
        speed = 50f     // Slower speed
        health = 60     // More health
        damage = 15     // More damage
        expValue = 40   // More experience
    }

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Slow, steady movement
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx)
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta
    }
}

// Legacy enemy type, now implements the base behavior directly
class Enemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(1f, 0f, 0f)  // Red

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Basic following behavior
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx)
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta
    }
}

// Ranged enemy that attacks from a distance
class RangedEnemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(0.8f, 0.4f, 0f)  // Orange
    private var attackTimer = 0f
    private val attackInterval = 2f  // Seconds between attacks
    private val preferredDistance = 200f  // Distance at which this enemy prefers to stay

    init {
        size = 20f
        speed = 120f
        health = 25
        damage = 8
        expValue = 35
    }

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Calculate distance and direction to player
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val distanceToPlayer = Vector2(dx, dy).len()
        val angle = atan2(dy, dx)

        // Move toward or away from player to maintain preferred distance
        if (distanceToPlayer > preferredDistance + 20) {
            // Move closer
            position.x += cos(angle) * speed * delta
            position.y += sin(angle) * speed * delta
        } else if (distanceToPlayer < preferredDistance - 20) {
            // Move away
            position.x -= cos(angle) * speed * delta
            position.y -= sin(angle) * speed * delta
        } else {
            // Strafe around player
            val strafeAngle = angle + (PI.toFloat() / 2)
            position.x += cos(strafeAngle) * speed * 0.5f * delta
            position.y += sin(strafeAngle) * speed * 0.5f * delta
        }

        // Update attack timer
        attackTimer += delta
        if (attackTimer >= attackInterval) {
            attackTimer = 0f
            // Attack logic would go here (e.g., spawn projectile)
            // For now, we'll just simulate the attack
        }
    }
}

// Boss enemy with special mechanics
class BossEnemy(startX: Float, startY: Float) : BaseEnemy(startX, startY) {
    override val color = Triple(0.7f, 0f, 0.7f)  // Purple

    // Boss state tracking
    private var attackTimer = 0f
    private var attackPhase = 0
    private val attackInterval = 3f  // Time between special attacks
    private var chargeTimer = 0f
    private var isCharging = false
    private var chargeDirection = Vector2()
    private var summonTimer = 0f
    private val summonInterval = 10f  // Time between summoning minions

    // Boss phases based on health percentage
    private var maxHealth = 0
    private var currentPhase = 0
    private val phaseThresholds = listOf(0.75f, 0.5f, 0.25f)  // Phase changes at 75%, 50%, and 25% health

    init {
        size = 48f  // Much larger than regular enemies
        speed = 70f  // Moderate speed
        health = 500  // Very high health
        damage = 25   // High damage
        expValue = 500  // Large experience reward
        maxHealth = health
    }

    override fun updateBehavior(delta: Float, playerPosition: Vector2) {
        // Update timers
        attackTimer += delta
        summonTimer += delta

        // Check for phase transitions
        val healthPercentage = health.toFloat() / maxHealth
        for (i in phaseThresholds.indices) {
            if (healthPercentage <= phaseThresholds[i] && currentPhase <= i) {
                currentPhase = i + 1
                // Increase stats for each phase
                speed += 10f
                damage += 5
                // Reset attack timer to immediately perform a special attack
                attackTimer = attackInterval
            }
        }

        // Handle charging state
        if (isCharging) {
            chargeTimer += delta
            // Move quickly in charge direction
            position.x += chargeDirection.x * speed * 3f * delta
            position.y += chargeDirection.y * speed * 3f * delta

            // End charge after 1 second
            if (chargeTimer >= 1f) {
                isCharging = false
                chargeTimer = 0f
            }
            return  // Skip normal movement while charging
        }

        // Normal movement when not charging
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx)
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta

        // Special attacks based on timer
        if (attackTimer >= attackInterval) {
            attackTimer = 0f
            attackPhase = (attackPhase + 1) % 3

            when (attackPhase) {
                0 -> {
                    // Charge attack
                    isCharging = true
                    chargeTimer = 0f
                    chargeDirection = Vector2(cos(angle), sin(angle)).nor()
                }
                1 -> {
                    // Area attack (would damage player if in range)
                    // This would be implemented in GameScreen with collision detection
                }
                2 -> {
                    // Teleport near player
                    val teleportDistance = 150f
                    val teleportAngle = Random.nextDouble(0.0, 2 * PI).toFloat()
                    position.x = playerPosition.x + cos(teleportAngle) * teleportDistance
                    position.y = playerPosition.y + sin(teleportAngle) * teleportDistance
                }
            }
        }

        // Summon minions periodically
        if (summonTimer >= summonInterval) {
            summonTimer = 0f
            // Summoning logic would be implemented in GameScreen
            // We'll just set a flag or trigger an event here
        }
    }

    // Override render to make the boss more distinctive
    override fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(color.first, color.second, color.third, 1f)

        // Draw a larger, more complex shape for the boss
        shapeRenderer.rect(position.x, position.y, size, size)

        // Add some details to make it look more boss-like
        shapeRenderer.setColor(1f, 0f, 0f, 1f)  // Red details
        val innerSize = size * 0.6f
        val offset = (size - innerSize) / 2
        shapeRenderer.rect(position.x + offset, position.y + offset, innerSize, innerSize)

        shapeRenderer.end()
    }
}
