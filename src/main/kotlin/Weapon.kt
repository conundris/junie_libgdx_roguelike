package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.atan2
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

    fun isOutOfBounds(): Boolean {
        return position.x < -size || position.x > GameScreen.WORLD_WIDTH ||
               position.y < -size || position.y > GameScreen.WORLD_HEIGHT
    }
}

class MeleeAttack(
    val position: Vector2,
    val size: Float,
    val damage: Int,
    var duration: Float = 0.2f
) {
    val bounds = Rectangle(position.x - size/2, position.y - size/2, size, size)

    fun update(delta: Float): Boolean {
        duration -= delta
        return duration <= 0
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)
        shapeRenderer.setColor(0.8f, 0.8f, 0.8f, duration * 5f)  // White color with fade
        shapeRenderer.circle(position.x, position.y, size)
        shapeRenderer.end()
    }
}

enum class WeaponType {
    SIMPLE, // Single projectile straight ahead
    SPREAD, // Multiple projectiles in a spread pattern
    BEAM,   // Continuous beam attack
    BURST,  // Rapid-fire burst of projectiles
    MELEE   // Close-range melee attack
}

class Weapon(private val player: Player, initialType: WeaponType = WeaponType.SIMPLE) {
    private val projectiles = mutableListOf<Projectile>()
    private val meleeAttacks = mutableListOf<MeleeAttack>()
    private var attackTimer = 0f
    private var chargeTimer = 0f
    private var isCharging = false
    private val targetingRange = 300f  // Range for auto-targeting
    private var currentTarget: Enemy? = null

    // Current weapon configuration
    private var currentType = initialType

    fun getCurrentWeaponType(): WeaponType = currentType

    fun getProjectiles(): List<Projectile> = projectiles.toList()

    private var specialAttackCooldown = 0f
    private val maxSpecialCooldown = 5f

    // Upgradeable properties
    private var projectileDamage = 20
    private var projectileSpeed = 400f
    private var attackInterval = 0.5f
    private var projectilesPerAttack = 4
    private var chargeMultiplier = 2f    // Damage multiplier for charged attacks
    private var damageMultiplier = 1f    // Additional multiplier for power-ups

    // Weapon-specific properties
    private var spreadAngle = Math.PI / 4  // 45-degree spread for SPREAD weapon
    private var beamWidth = 10f            // Width of BEAM weapon
    private var burstRate = 0.8f           // Rate multiplier for BURST weapon
    private var meleeRange = 1.5f          // Range multiplier for MELEE weapon
    private var meleeDuration = 0.2f       // Duration of MELEE attacks

    fun applyDamageBoost(multiplier: Float) {
        damageMultiplier = multiplier
    }

    fun resetDamageMultiplier() {
        damageMultiplier = 1f
    }

    private fun getCurrentDamage(isCharged: Boolean): Int {
        val baseDamage = if (isCharged) {
            (projectileDamage.toFloat() * chargeMultiplier).toInt()
        } else {
            projectileDamage
        }
        return (baseDamage.toFloat() * damageMultiplier).toInt()
    }

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

    // Weapon-specific upgrade methods
    fun increaseSpreadAngle(percentage: Float) {
        spreadAngle *= (1f + percentage)
    }

    fun increaseBeamWidth(amount: Float) {
        beamWidth += amount
    }

    fun increaseBurstRate(percentage: Float) {
        burstRate *= (1f + percentage)
    }

    fun increaseMeleeRange(percentage: Float) {
        meleeRange *= (1f + percentage)
    }

    fun increaseMeleeDuration(amount: Float) {
        meleeDuration += amount
    }

    fun update(delta: Float, enemies: List<Enemy>) {
        // Update timers
        attackTimer += delta
        if (specialAttackCooldown > 0) {
            specialAttackCooldown -= delta
        }

        // Update current target if auto-targeting is enabled
        if (player.isAutoTargeting) {
            currentTarget = findNearestEnemy(enemies)
        } else {
            currentTarget = null
        }

        // Handle charging
        if (isCharging) {
            chargeTimer += delta
        }

        // Regular attack
        if (attackTimer >= (if (currentType == WeaponType.MELEE) attackInterval * 0.5f else attackInterval) && !isCharging) {
            attackTimer = 0f
            attack(false)
        }

        // Update projectiles and melee attacks
        projectiles.forEach { it.update(delta) }
        meleeAttacks.removeAll { it.update(delta) }

        // Remove out-of-bounds projectiles
        projectiles.removeAll { it.isOutOfBounds() }
    }

    fun startCharging() {
        if (!isCharging && specialAttackCooldown <= 0) {
            isCharging = true
            chargeTimer = 0f
        }
    }

    fun releaseCharge() {
        if (isCharging) {
            isCharging = false
            if (chargeTimer >= 1f) {  // Minimum charge time
                attack(true)
                specialAttackCooldown = maxSpecialCooldown
            } else {
                attack(false)
            }
            chargeTimer = 0f
        }
    }

    fun switchWeapon() {
        currentType = when (currentType) {
            WeaponType.SIMPLE -> WeaponType.SPREAD
            WeaponType.SPREAD -> WeaponType.BEAM
            WeaponType.BEAM -> WeaponType.BURST
            WeaponType.BURST -> WeaponType.MELEE
            WeaponType.MELEE -> WeaponType.SIMPLE
        }
    }

    private fun findNearestEnemy(enemies: List<Enemy>): Enemy? {
        var nearest: Enemy? = null
        var minDistance = Float.MAX_VALUE
        val playerCenter = Vector2(player.position).add(player.size / 2, player.size / 2)

        for (enemy in enemies) {
            val enemyCenter = Vector2(enemy.position).add(enemy.size / 2, enemy.size / 2)
            val distance = playerCenter.dst(enemyCenter)
            if (distance <= targetingRange && distance < minDistance) {
                minDistance = distance
                nearest = enemy
            }
        }
        return nearest
    }

    private fun getTargetDirection(position: Vector2): Float {
        return if (player.isAutoTargeting && currentTarget != null) {
            val targetCenter = Vector2(currentTarget!!.position).add(currentTarget!!.size / 2, currentTarget!!.size / 2)
            val dx = targetCenter.x - position.x
            val dy = targetCenter.y - position.y
            atan2(dy, dx)
        } else {
            atan2(player.direction.y, player.direction.x)
        }
    }

    private fun attack(isCharged: Boolean) {
        val damage = getCurrentDamage(isCharged)
        val position = Vector2(player.position).add(player.size / 2, player.size / 2)
        val baseAngle = getTargetDirection(position)

        when (currentType) {
            WeaponType.SIMPLE -> {
                // Create a single projectile straight ahead
                projectiles.add(Projectile(
                    position = Vector2(position),
                    direction = baseAngle,
                    speed = projectileSpeed,
                    damage = damage
                ))
            }
            WeaponType.SPREAD -> {
                // Create projectiles in a spread pattern
                val angleStep = (spreadAngle * 2) / (projectilesPerAttack - 1)
                val startAngle = baseAngle - spreadAngle
                for (i in 0 until projectilesPerAttack) {
                    val angle = startAngle + (i * angleStep)
                    projectiles.add(Projectile(
                        position = Vector2(position),
                        direction = angle.toFloat(),
                        speed = projectileSpeed,
                        damage = damage
                    ))
                }
            }
            WeaponType.BEAM -> {
                // Create a focused beam of projectiles
                val beamDamage = (damage.toFloat() * 1.5f).toInt()
                for (i in 0..2) {  // Create 3 projectiles in a line
                    val offsetX = cos(baseAngle) * (i * beamWidth)
                    val offsetY = sin(baseAngle) * (i * beamWidth)
                    projectiles.add(Projectile(
                        position = Vector2(position).add(offsetX, offsetY),
                        direction = baseAngle,
                        speed = projectileSpeed * 1.5f,
                        damage = beamDamage
                    ))
                }
            }
            WeaponType.BURST -> {
                // Rapid-fire burst of projectiles
                val burstDamage = (damage.toFloat() * burstRate).toInt()
                val burstSpread = Math.PI / 6  // 30-degree spread
                for (i in 0..2) {
                    val angle = baseAngle - burstSpread + (burstSpread * i)
                    projectiles.add(Projectile(
                        position = Vector2(position),
                        direction = angle.toFloat(),
                        speed = projectileSpeed * 1.2f,
                        damage = burstDamage
                    ))
                }
            }
            WeaponType.MELEE -> {
                // Create a melee attack with configurable range and damage
                val meleeDamage = (damage.toFloat() * 2f).toInt()
                meleeAttacks.add(MeleeAttack(
                    position = position,
                    size = player.size * meleeRange,
                    damage = meleeDamage,
                    duration = meleeDuration
                ))
            }
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        // Render projectiles and melee attacks
        projectiles.forEach { it.render(shapeRenderer) }
        meleeAttacks.forEach { it.render(shapeRenderer) }

        // Render auto-targeting range indicator
        if (player.isAutoTargeting) {
            shapeRenderer.begin(ShapeType.Line)
            shapeRenderer.setColor(0.5f, 1f, 0.5f, 0.3f)
            shapeRenderer.circle(
                player.position.x + player.size / 2,
                player.position.y + player.size / 2,
                targetingRange
            )
            shapeRenderer.end()

            // Render line to current target if exists
            currentTarget?.let { target ->
                shapeRenderer.begin(ShapeType.Line)
                shapeRenderer.setColor(1f, 0f, 0f, 0.5f)
                shapeRenderer.line(
                    player.position.x + player.size / 2,
                    player.position.y + player.size / 2,
                    target.position.x + target.size / 2,
                    target.position.y + target.size / 2
                )
                shapeRenderer.end()
            }
        }

        // Render charge indicator
        if (isCharging) {
            shapeRenderer.begin(ShapeType.Filled)
            shapeRenderer.setColor(1f, 1f, 0f, chargeTimer.coerceIn(0f, 1f))
            shapeRenderer.circle(
                player.position.x + player.size / 2,
                player.position.y + player.size / 2,
                player.size * (0.8f + chargeTimer * 0.2f)
            )
            shapeRenderer.end()
        }

        // Render special attack cooldown
        if (specialAttackCooldown > 0) {
            shapeRenderer.begin(ShapeType.Line)
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.7f)
            val cooldownProgress = 1f - (specialAttackCooldown / maxSpecialCooldown)
            shapeRenderer.arc(
                player.position.x + player.size / 2,
                player.position.y + player.size / 2,
                player.size * 0.6f,
                0f,
                cooldownProgress * 360f
            )
            shapeRenderer.end()
        }

        // Render current weapon type indicator
        shapeRenderer.begin(ShapeType.Filled)
        when (currentType) {
            WeaponType.SIMPLE -> shapeRenderer.setColor(1f, 1f, 1f, 0.5f)    // White
            WeaponType.SPREAD -> shapeRenderer.setColor(0.2f, 1f, 0.2f, 0.5f)  // Green
            WeaponType.BEAM -> shapeRenderer.setColor(0.2f, 0.2f, 1f, 0.5f)    // Blue
            WeaponType.BURST -> shapeRenderer.setColor(1f, 0.2f, 0.2f, 0.5f)   // Red
            WeaponType.MELEE -> shapeRenderer.setColor(0.8f, 0.8f, 0.8f, 0.5f) // Gray
        }
        shapeRenderer.circle(
            player.position.x + player.size / 2,
            player.position.y + player.size / 2,
            player.size * 0.3f
        )
        shapeRenderer.end()
    }

    fun handleInput() {
        // Weapon switching (Q key)
        if (Gdx.input.isKeyJustPressed(Keys.Q)) {
            switchWeapon()
        }

        // Charge attack (Right mouse button)
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            startCharging()
        } else if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && isCharging) {
            releaseCharge()
        }
    }

    fun checkCollisions(enemies: List<Enemy>) {
        // Check projectile collisions
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

        // Check melee attack collisions
        for (meleeAttack in meleeAttacks) {
            for (enemy in enemies) {
                if (meleeAttack.bounds.overlaps(enemy.bounds)) {
                    enemy.takeDamage(meleeAttack.damage)
                }
            }
        }
    }
}
