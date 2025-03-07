package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

enum class PowerUpType {
    HEALTH,      // Restores health
    EXPERIENCE,  // Grants bonus experience
    SPEED,       // Temporary speed boost
    DAMAGE       // Temporary damage boost
}

class PowerUp(
    private val position: Vector2,
    val type: PowerUpType
) {
    val size = 16f
    val bounds = Rectangle(position.x, position.y, size, size)
    private var lifetime = 10f  // Power-up disappears after 10 seconds
    private var pulseTimer = 0f

    fun update(delta: Float): Boolean {
        lifetime -= delta
        pulseTimer += delta * 3f  // Control pulse animation speed
        return lifetime > 0
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)

        // Set color based on power-up type
        when (type) {
            PowerUpType.HEALTH -> shapeRenderer.setColor(0f, 1f, 0f, 0.8f)      // Green
            PowerUpType.EXPERIENCE -> shapeRenderer.setColor(1f, 1f, 0f, 0.8f)  // Yellow
            PowerUpType.SPEED -> shapeRenderer.setColor(0f, 1f, 1f, 0.8f)       // Cyan
            PowerUpType.DAMAGE -> shapeRenderer.setColor(1f, 0f, 0f, 0.8f)      // Red
        }

        // Pulse animation
        val scale = 1f + (kotlin.math.sin(pulseTimer) * 0.2f)
        val scaledSize = size * scale
        val offset = (scaledSize - size) / 2

        shapeRenderer.rect(
            position.x - offset,
            position.y - offset,
            scaledSize,
            scaledSize
        )
        shapeRenderer.end()
    }

    fun applyEffect(player: Player) {
        when (type) {
            PowerUpType.HEALTH -> {
                player.health = (player.health + 30).coerceAtMost(100)
            }
            PowerUpType.EXPERIENCE -> {
                player.gainExperience(50)
            }
            PowerUpType.SPEED -> {
                player.applySpeedBoost()
            }
            PowerUpType.DAMAGE -> {
                player.applyDamageBoost()
            }
        }
    }

    fun overlaps(player: Player): Boolean {
        return bounds.overlaps(player.bounds)
    }
}
