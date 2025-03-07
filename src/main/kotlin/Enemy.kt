package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
