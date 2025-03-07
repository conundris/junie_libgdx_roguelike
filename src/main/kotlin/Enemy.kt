package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Enemy(startX: Float, startY: Float) {
    val position = Vector2(startX, startY)
    val size = 24f
    val speed = 100f
    var health = 30
    val bounds = Rectangle()
    val damage = 10
    val expValue = 20  // Experience points dropped when killed

    init {
        updateBounds()
    }

    private fun updateBounds() {
        bounds.set(position.x, position.y, size, size)
    }

    fun update(delta: Float, playerPosition: Vector2) {
        // Calculate direction to player
        val dx = playerPosition.x - position.x
        val dy = playerPosition.y - position.y
        val angle = atan2(dy, dx)

        // Move towards player
        position.x += cos(angle) * speed * delta
        position.y += sin(angle) * speed * delta

        updateBounds()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(1f, 0f, 0f, 1f)  // Red color
        shapeRenderer.rect(position.x, position.y, size, size)
        shapeRenderer.end()
    }

    fun takeDamage(damage: Int) {
        health -= damage
        if (health < 0) health = 0
    }

    fun isAlive(): Boolean = health > 0

    fun overlaps(player: Player): Boolean {
        return bounds.overlaps(player.bounds)
    }
}
