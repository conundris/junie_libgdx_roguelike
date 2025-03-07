package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Player {
    val position = Vector2(400f, 300f)
    val size = 32f
    val speed = 200f
    var health = 100
    val bounds = Rectangle()
    val weapon: Weapon
    val experience: Experience

    init {
        updateBounds()
        weapon = Weapon(this)
        experience = Experience(this)
    }

    fun gainExperience(amount: Int) {
        experience.addExp(amount)
        // We'll add upgrade handling here later
    }

    private fun updateBounds() {
        bounds.set(position.x, position.y, size, size)
    }

    fun update(delta: Float) {
        handleInput(delta)
        weapon.update(delta)
    }

    private fun handleInput(delta: Float) {
        // Handle player movement
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            position.x -= speed * delta
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            position.x += speed * delta
        }
        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            position.y += speed * delta
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            position.y -= speed * delta
        }

        // Keep player within screen bounds
        position.x = position.x.coerceIn(0f, 800f - size)
        position.y = position.y.coerceIn(0f, 600f - size)

        updateBounds()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        // Render player
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(0f, 1f, 0f, 1f)  // Green color
        shapeRenderer.rect(position.x, position.y, size, size)
        shapeRenderer.end()

        // Render weapon
        weapon.render(shapeRenderer)
    }

    fun takeDamage(damage: Int) {
        health -= damage
        if (health < 0) health = 0
    }

    fun isAlive(): Boolean = health > 0
}
