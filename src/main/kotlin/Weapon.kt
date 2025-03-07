package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin

class Projectile(
    val position: Vector2,
    private val direction: Float,
    private val speed: Float,
    val damage: Int
) {
    val size = 8f
    val bounds = Rectangle(position.x, position.y, size, size)

    fun update(delta: Float) {
        position.x += cos(direction) * speed * delta
        position.y += sin(direction) * speed * delta
        bounds.setPosition(position)
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(1f, 1f, 0f, 1f)  // Yellow color
        shapeRenderer.rect(position.x, position.y, size, size)
        shapeRenderer.end()
    }

    fun isOutOfBounds(screenWidth: Float, screenHeight: Float): Boolean {
        return position.x < -size || position.x > screenWidth ||
               position.y < -size || position.y > screenHeight
    }
}

class Weapon(private val player: Player) {
    private val projectiles = mutableListOf<Projectile>()
    private var attackTimer = 0f

    // Upgradeable properties
    private var projectileDamage = 20
    private var projectileSpeed = 400f
    private var attackInterval = 0.5f
    private var projectilesPerAttack = 4

    // Upgrade methods
    fun increaseDamage(amount: Int) {
        projectileDamage += amount
    }

    fun increaseProjectileCount(amount: Int) {
        projectilesPerAttack += amount
    }

    fun increaseAttackSpeed(percentage: Float) {
        attackInterval *= (1f - percentage)
    }

    fun increaseProjectileSpeed(percentage: Float) {
        projectileSpeed *= (1f + percentage)
    }

    fun update(delta: Float) {
        // Update attack timer
        attackTimer += delta
        if (attackTimer >= attackInterval) {
            attackTimer = 0f
            attack()
        }

        // Update projectiles
        projectiles.forEach { it.update(delta) }

        // Remove out-of-bounds projectiles
        projectiles.removeAll { it.isOutOfBounds(800f, 600f) }
    }

    private fun attack() {
        // Create projectiles in a spread pattern
        val angleStep = (2 * Math.PI) / projectilesPerAttack
        for (i in 0 until projectilesPerAttack) {
            val angle = i * angleStep
            val position = Vector2(player.position).add(player.size / 2, player.size / 2)
            projectiles.add(Projectile(
                position = Vector2(position),
                direction = angle.toFloat(),
                speed = projectileSpeed,
                damage = projectileDamage
            ))
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        projectiles.forEach { it.render(shapeRenderer) }
    }

    fun checkCollisions(enemies: List<Enemy>) {
        val projectilesToRemove = mutableListOf<Projectile>()
        for (projectile in projectiles) {
            for (enemy in enemies) {
                if (projectile.bounds.overlaps(enemy.bounds)) {
                    enemy.takeDamage(projectile.damage)
                    projectilesToRemove.add(projectile)
                    break
                }
            }
        }
        projectiles.removeAll(projectilesToRemove)
    }
}
