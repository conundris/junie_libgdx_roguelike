package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

open class Player(
    private val initialWeaponType: WeaponType = WeaponType.SIMPLE,
    initialX: Float? = null,
    initialY: Float? = null,
    private val worldWidth: Float = GameScreen.WORLD_WIDTH,
    private val worldHeight: Float = GameScreen.WORLD_HEIGHT
) {
    companion object {
        const val SIZE = 32f
    }

    // Network-related properties
    var networkId: Int = -1
    var isRemotePlayer: Boolean = false

    open val position = Vector2(
        initialX ?: worldWidth / 2,
        initialY ?: worldHeight / 2
    )
    open val direction = Vector2(1f, 0f)  // Default facing right
    open val size = SIZE
    protected val baseSpeed = 200f
    open var speed = baseSpeed
    open var health = 100
    open val bounds = Rectangle()
    open val weapon: Weapon
    open val experience: Experience
    open var isAutoTargeting = false

    // Power-up effect timers
    private var speedBoostTimer = 0f
    private var damageBoostTimer = 0f
    private val speedBoostDuration = 5f  // 5 seconds
    private val damageBoostDuration = 5f  // 5 seconds
    private val speedBoostMultiplier = 1.5f
    private val damageBoostMultiplier = 1.5f

    // Dash mechanics
    private val dashSpeed = 800f
    private val dashDuration = 0.2f
    private val dashCooldown = 1f
    private var isDashing = false
    private var dashTimer = 0f
    private var dashCooldownTimer = 0f
    private var dashDirection = Vector2()

    init {
        updateBounds()
        weapon = createWeapon()
        experience = createExperience()
    }

    // Factory methods that can be overridden in tests
    protected open fun createWeapon(): Weapon = Weapon(this, initialWeaponType)
    protected open fun createExperience(): Experience = Experience(this)

    fun gainExperience(amount: Int) {
        experience.addExp(amount)
        // We'll add upgrade handling here later
    }

    fun applySpeedBoost() {
        speed = baseSpeed * speedBoostMultiplier
        speedBoostTimer = speedBoostDuration
    }

    fun applyDamageBoost() {
        weapon.applyDamageBoost(damageBoostMultiplier)
        damageBoostTimer = damageBoostDuration
    }

    private fun updateBounds() {
        bounds.set(position.x, position.y, size, size)
    }

    fun update(delta: Float, enemies: List<BaseEnemy>) {
        // Only handle input for local players
        if (!isRemotePlayer) {
            handleInput(delta)
        }

        weapon.update(delta, enemies)

        // Update dash cooldown
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= delta
        }

        // Update power-up effects
        if (speedBoostTimer > 0) {
            speedBoostTimer -= delta
            if (speedBoostTimer <= 0) {
                speed = baseSpeed  // Reset speed when boost expires
            }
        }

        if (damageBoostTimer > 0) {
            damageBoostTimer -= delta
            if (damageBoostTimer <= 0) {
                weapon.resetDamageMultiplier()  // Reset damage when boost expires
            }
        }

        // Update dash state
        if (isDashing) {
            dashTimer += delta
            if (dashTimer >= dashDuration) {
                isDashing = false
                dashTimer = 0f
                dashCooldownTimer = dashCooldown
            } else {
                // Apply dash movement
                position.x += dashDirection.x * dashSpeed * delta
                position.y += dashDirection.y * dashSpeed * delta
            }
        }

        // Keep player within world bounds
        position.x = position.x.coerceIn(0f, worldWidth - size)
        position.y = position.y.coerceIn(0f, worldHeight - size)

        updateBounds()
    }

    /**
     * Updates the player's state from a network message.
     * This is used for remote players to synchronize their state with the server.
     */
    fun updateFromNetwork(
        posX: Float,
        posY: Float,
        dirX: Float,
        dirY: Float,
        playerHealth: Int,
        playerSpeed: Float,
        playerIsDashing: Boolean,
        playerIsAutoTargeting: Boolean
    ) {
        if (!isRemotePlayer) return // Only update remote players

        position.set(posX, posY)
        direction.set(dirX, dirY)
        health = playerHealth
        speed = playerSpeed
        isDashing = playerIsDashing
        isAutoTargeting = playerIsAutoTargeting

        updateBounds()
    }

    /**
     * Returns whether the player is currently dashing.
     * This is used for network synchronization.
     */
    fun isDashing(): Boolean = isDashing

    private fun handleInput(delta: Float) {
        // Get movement direction
        var moveX = 0f
        var moveY = 0f

        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
            moveX -= 1f
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
            moveX += 1f
        }
        if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
            moveY += 1f
        }
        if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
            moveY -= 1f
        }

        // Update direction if moving
        if (moveX != 0f || moveY != 0f) {
            direction.set(moveX, moveY).nor()
        }

        // Toggle auto-targeting with F key
        if (Gdx.input.isKeyJustPressed(Keys.F)) {
            isAutoTargeting = !isAutoTargeting
        }

        // Handle dash input (SPACE key)
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && !isDashing && dashCooldownTimer <= 0) {
            if (moveX != 0f || moveY != 0f) {
                isDashing = true
                dashTimer = 0f
                // Normalize dash direction
                dashDirection.set(moveX, moveY).nor()
            }
        }

        // Apply normal movement if not dashing
        if (!isDashing) {
            position.x += moveX * speed * delta
            position.y += moveY * speed * delta
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        // Render dash trail when dashing
        if (isDashing) {
            shapeRenderer.begin(ShapeType.Filled)
            shapeRenderer.setColor(0f, 1f, 0f, 0.3f)  // Transparent green
            for (i in 1..3) {
                val trailPos = Vector2(
                    position.x - dashDirection.x * (i * 10),
                    position.y - dashDirection.y * (i * 10)
                )
                shapeRenderer.rect(trailPos.x, trailPos.y, size, size)
            }
            shapeRenderer.end()
        }

        // Render player
        shapeRenderer.begin(ShapeType.Filled)
        if (isDashing) {
            shapeRenderer.setColor(0f, 1f, 1f, 1f)  // Cyan color during dash
        } else {
            shapeRenderer.setColor(0f, 1f, 0f, 1f)  // Normal green color
        }
        shapeRenderer.rect(position.x, position.y, size, size)
        shapeRenderer.end()

        // Render dash cooldown indicator
        if (dashCooldownTimer > 0) {
            shapeRenderer.begin(ShapeType.Line)
            shapeRenderer.setColor(1f, 1f, 1f, 0.5f)
            val cooldownProgress = 1f - (dashCooldownTimer / dashCooldown)
            shapeRenderer.arc(
                position.x + size/2, position.y + size/2,
                size/2, 0f, cooldownProgress * 360f
            )
            shapeRenderer.end()
        }

        // Render weapon
        weapon.render(shapeRenderer)
    }

    fun takeDamage(damage: Int) {
        health -= damage
        if (health < 0) health = 0
    }

    fun isAlive(): Boolean = health > 0
}
