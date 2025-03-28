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
    private var direction: Float,
    private val speed: Float,
    val damage: Int
) {
    val size = 8f
    val bounds = Rectangle(position.x, position.y, size, size)

    // Enhanced projectile properties
    private var isPiercing = false
    private var isHoming = false
    private var targetEnemy: BaseEnemy? = null
    private var isBouncing = false
    private var bounceCount = 0
    private var bouncesRemaining = 0
    private var isChaining = false
    private var chainCount = 0
    private var chainsRemaining = 0
    private var chainedEnemies = mutableSetOf<BaseEnemy>()
    private var isExplosive = false
    private var explosionRadius = 0f
    private var explosionDamage = 0
    private var isDelayed = false
    private var delayTimer = 0f
    private var isActive = true

    fun setPiercing(piercing: Boolean) {
        isPiercing = piercing
    }

    fun setHoming(homing: Boolean, target: BaseEnemy?) {
        isHoming = homing
        targetEnemy = target
    }

    fun setBouncing(bouncing: Boolean, maxBounces: Int) {
        isBouncing = bouncing
        bounceCount = maxBounces
        bouncesRemaining = maxBounces
    }

    fun setChaining(chaining: Boolean, maxChains: Int) {
        isChaining = chaining
        chainCount = maxChains
        chainsRemaining = maxChains
    }

    fun setExplosive(explosive: Boolean, radius: Float, damage: Int) {
        isExplosive = explosive
        explosionRadius = radius
        explosionDamage = damage
    }

    fun setDelayed(delay: Float) {
        isDelayed = true
        delayTimer = delay
        isActive = false
    }

    fun isActive(): Boolean {
        return isActive
    }

    fun canPierce(): Boolean {
        return isPiercing
    }

    fun canChain(): Boolean {
        return isChaining && chainsRemaining > 0
    }

    fun isExplosive(): Boolean {
        return isExplosive
    }

    fun getExplosionRadius(): Float {
        return explosionRadius
    }

    fun getExplosionDamage(): Int {
        return explosionDamage
    }

    fun chainToEnemy(enemy: BaseEnemy) {
        if (canChain() && !chainedEnemies.contains(enemy)) {
            chainedEnemies.add(enemy)
            chainsRemaining--

            // Update direction to target the new enemy
            val enemyCenter = Vector2(enemy.position).add(enemy.size / 2, enemy.size / 2)
            val dx = enemyCenter.x - position.x
            val dy = enemyCenter.y - position.y
            direction = atan2(dy, dx)
        }
    }

    fun explode(): List<BaseEnemy> {
        // This would be called when the projectile hits something and should explode
        // Returns a list of enemies affected by the explosion
        // The actual implementation would need to find all enemies within explosionRadius
        return emptyList() // Placeholder
    }

    fun update(delta: Float) {
        // Handle delay if projectile is delayed
        if (isDelayed && !isActive) {
            delayTimer -= delta
            if (delayTimer <= 0) {
                isActive = true
                isDelayed = false
            }
            return
        }

        // Only update active projectiles
        if (!isActive) return

        // Update homing direction if applicable
        if (isHoming && targetEnemy != null && targetEnemy!!.isAlive()) {
            val targetCenter = Vector2(targetEnemy!!.position).add(targetEnemy!!.size / 2, targetEnemy!!.size / 2)
            val dx = targetCenter.x - position.x
            val dy = targetCenter.y - position.y
            val targetDirection = atan2(dy, dx)

            // Gradually adjust direction towards target (not instant)
            val turnSpeed = 0.1f
            val angleDiff = normalizeAngle(targetDirection - direction)
            direction += angleDiff * turnSpeed
        }

        // Update position based on direction
        position.x += cos(direction) * speed * delta
        position.y += sin(direction) * speed * delta
        bounds.setPosition(position)

        // Handle bouncing if projectile hits world boundary
        if (isBouncing && bouncesRemaining > 0) {
            var bounced = false

            // Check left/right boundaries
            if (position.x < 0 || position.x > GameScreen.WORLD_WIDTH - size) {
                direction = Math.PI.toFloat() - direction
                bounced = true
            }

            // Check top/bottom boundaries
            if (position.y < 0 || position.y > GameScreen.WORLD_HEIGHT - size) {
                direction = -direction
                bounced = true
            }

            if (bounced) {
                bouncesRemaining--
                // Ensure position is within bounds after bounce
                position.x = position.x.coerceIn(0f, GameScreen.WORLD_WIDTH - size)
                position.y = position.y.coerceIn(0f, GameScreen.WORLD_HEIGHT - size)
            }
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        var result = angle
        while (result > Math.PI) result -= (2 * Math.PI).toFloat()
        while (result < -Math.PI) result += (2 * Math.PI).toFloat()
        return result
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!isActive) return

        shapeRenderer.begin(ShapeType.Filled)

        // Different colors based on projectile properties
        when {
            isPiercing -> shapeRenderer.setColor(1f, 0.5f, 0f, 1f)  // Orange for piercing
            isHoming -> shapeRenderer.setColor(0f, 1f, 1f, 1f)      // Cyan for homing
            isBouncing -> shapeRenderer.setColor(0f, 1f, 0.5f, 1f)  // Green for bouncing
            isChaining -> shapeRenderer.setColor(0.5f, 0f, 1f, 1f)  // Purple for chaining
            isExplosive -> shapeRenderer.setColor(1f, 0f, 0f, 1f)   // Red for explosive
            else -> shapeRenderer.setColor(1f, 1f, 0f, 1f)          // Default yellow
        }

        // Different shapes based on projectile properties
        if (isPiercing) {
            // Diamond shape for piercing
            val halfSize = size / 2
            shapeRenderer.triangle(
                position.x + halfSize, position.y,
                position.x + size, position.y + halfSize,
                position.x + halfSize, position.y + size
            )
            shapeRenderer.triangle(
                position.x + halfSize, position.y,
                position.x, position.y + halfSize,
                position.x + halfSize, position.y + size
            )
        } else {
            // Regular square for other types
            shapeRenderer.rect(position.x, position.y, size, size)
        }

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

    // Enhanced melee attack properties
    private var hasKnockback = false
    private var knockbackForce = 0f
    private var knockbackDirection = 0f

    fun setKnockback(knockback: Boolean, force: Float, direction: Float) {
        hasKnockback = knockback
        knockbackForce = force
        knockbackDirection = direction
    }

    fun hasKnockback(): Boolean {
        return hasKnockback
    }

    fun getKnockbackForce(): Float {
        return knockbackForce
    }

    fun getKnockbackDirection(): Float {
        return knockbackDirection
    }

    fun update(delta: Float): Boolean {
        duration -= delta
        return duration <= 0
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeType.Filled)

        // Different color if has knockback
        if (hasKnockback) {
            shapeRenderer.setColor(0.9f, 0.7f, 0.2f, duration * 5f)  // Gold color with fade
        } else {
            shapeRenderer.setColor(0.8f, 0.8f, 0.8f, duration * 5f)  // White color with fade
        }

        shapeRenderer.circle(position.x, position.y, size)

        // Draw knockback direction indicator if has knockback
        if (hasKnockback) {
            shapeRenderer.setColor(1f, 0.5f, 0f, duration * 5f)  // Orange color with fade
            val indicatorLength = size * 0.8f
            val endX = position.x + cos(knockbackDirection) * indicatorLength
            val endY = position.y + sin(knockbackDirection) * indicatorLength
            shapeRenderer.rectLine(position.x, position.y, endX, endY, size * 0.2f)
        }

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
    private var currentTarget: BaseEnemy? = null

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

    // New offensive properties
    private var criticalChance = 0.05f   // 5% base chance for critical hits
    private var criticalMultiplier = 2f  // Critical hits do 2x damage by default

    // Weapon-specific properties
    private var spreadAngle = Math.PI / 4  // 45-degree spread for SPREAD weapon
    private var beamWidth = 10f            // Width of BEAM weapon
    private var burstRate = 0.8f           // Rate multiplier for BURST weapon
    private var meleeRange = 1.5f          // Range multiplier for MELEE weapon
    private var meleeDuration = 0.2f       // Duration of MELEE attacks

    // New weapon-specific properties
    // SIMPLE weapon
    private var simplePiercing = false     // Whether projectiles pierce through enemies
    private var simpleHoming = false       // Whether projectiles home in on enemies
    private var simpleBouncing = false     // Whether projectiles bounce off walls
    private var simpleBounceCount = 0      // Number of bounces

    // SPREAD weapon
    private var spreadCoverage = 1f        // Coverage multiplier
    private var spreadDensity = 1f         // Density multiplier

    // BEAM weapon
    private var beamPenetration = false    // Whether beam penetrates through enemies
    private var beamChain = false          // Whether beam chains to nearby enemies
    private var beamChainCount = 0         // Number of chain targets

    // BURST weapon
    private var burstVolleyCount = 1       // Number of volleys per burst
    private var burstExplosion = false     // Whether projectiles explode on impact
    private var burstExplosionRadius = 0f  // Explosion radius

    // MELEE weapon
    private var meleeKnockback = false     // Whether melee attacks have knockback
    private var meleeKnockbackForce = 0f   // Knockback force
    private var meleeSpin = false          // Whether melee attack spins around player

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

        // Apply critical hit if roll succeeds
        val isCritical = Math.random() < criticalChance
        val criticalDamage = if (isCritical) {
            (baseDamage.toFloat() * criticalMultiplier).toInt()
        } else {
            baseDamage
        }

        return (criticalDamage.toFloat() * damageMultiplier).toInt()
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

    // New offensive upgrade methods
    fun increaseCriticalChance(amount: Float) {
        criticalChance += amount
        criticalChance = criticalChance.coerceAtMost(1f) // Cap at 100%
    }

    fun increaseCriticalMultiplier(amount: Float) {
        criticalMultiplier += amount
    }

    // New defensive upgrade methods (these will be applied to the player)

    // New utility upgrade methods (these will be applied to the player)

    // New weapon-specific upgrade methods - SIMPLE
    fun enableSimplePiercing() {
        simplePiercing = true
    }

    fun enableSimpleHoming() {
        simpleHoming = true
    }

    fun enableSimpleBouncing(bounceCount: Int = 1) {
        simpleBouncing = true
        simpleBounceCount += bounceCount
    }

    // New weapon-specific upgrade methods - SPREAD
    fun increaseSpreadCoverage(amount: Float) {
        spreadCoverage += amount
    }

    fun increaseSpreadDensity(amount: Float) {
        spreadDensity += amount
    }

    // New weapon-specific upgrade methods - BEAM
    fun enableBeamPenetration() {
        beamPenetration = true
    }

    fun enableBeamChain(chainCount: Int = 1) {
        beamChain = true
        beamChainCount += chainCount
    }

    // New weapon-specific upgrade methods - BURST
    fun increaseBurstVolleyCount(amount: Int) {
        burstVolleyCount += amount
    }

    fun enableBurstExplosion(radius: Float = 50f) {
        burstExplosion = true
        burstExplosionRadius = radius
    }

    // New weapon-specific upgrade methods - MELEE
    fun enableMeleeKnockback(force: Float = 100f) {
        meleeKnockback = true
        meleeKnockbackForce = force
    }

    fun enableMeleeSpin() {
        meleeSpin = true
    }

    fun update(delta: Float, enemies: List<BaseEnemy>) {
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

    private fun findNearestEnemy(enemies: List<BaseEnemy>): BaseEnemy? {
        var nearest: BaseEnemy? = null
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
                // Create a single projectile straight ahead with enhanced properties
                val proj = Projectile(
                    position = Vector2(position),
                    direction = baseAngle,
                    speed = projectileSpeed,
                    damage = damage
                )

                // Apply SIMPLE weapon upgrades
                if (simplePiercing) {
                    proj.setPiercing(true)
                }
                if (simpleHoming) {
                    proj.setHoming(true, currentTarget)
                }
                if (simpleBouncing) {
                    proj.setBouncing(true, simpleBounceCount)
                }

                projectiles.add(proj)
            }
            WeaponType.SPREAD -> {
                // Create projectiles in a spread pattern with enhanced coverage and density
                val effectiveProjectileCount = (projectilesPerAttack * spreadDensity).toInt().coerceAtLeast(1)
                val effectiveSpreadAngle = spreadAngle * spreadCoverage
                val angleStep = if (effectiveProjectileCount > 1) {
                    ((effectiveSpreadAngle * 2) / (effectiveProjectileCount - 1)).toFloat()
                } else {
                    0f
                }
                val startAngle = baseAngle - effectiveSpreadAngle

                for (i in 0 until effectiveProjectileCount) {
                    val angle = startAngle + (i.toFloat() * angleStep)
                    projectiles.add(Projectile(
                        position = Vector2(position),
                        direction = angle.toFloat(),
                        speed = projectileSpeed,
                        damage = damage
                    ))
                }
            }
            WeaponType.BEAM -> {
                // Create a focused beam of projectiles with enhanced properties
                val beamDamage = (damage.toFloat() * 1.5f).toInt()
                val beamCount = if (beamPenetration) 5 else 3 // More projectiles if penetration is enabled

                for (i in 0 until beamCount) {
                    val offsetX = cos(baseAngle) * (i * beamWidth)
                    val offsetY = sin(baseAngle) * (i * beamWidth)
                    val proj = Projectile(
                        position = Vector2(position).add(offsetX, offsetY),
                        direction = baseAngle,
                        speed = projectileSpeed * 1.5f,
                        damage = beamDamage
                    )

                    // Apply BEAM weapon upgrades
                    if (beamPenetration) {
                        proj.setPiercing(true)
                    }
                    if (beamChain) {
                        proj.setChaining(true, beamChainCount)
                    }

                    projectiles.add(proj)
                }
            }
            WeaponType.BURST -> {
                // Rapid-fire burst of projectiles with enhanced properties
                val burstDamage = (damage.toFloat() * burstRate).toInt()
                val burstSpread = Math.PI / 6  // 30-degree spread

                // Create multiple volleys if upgraded
                for (volley in 0 until burstVolleyCount) {
                    // Add a slight delay between volleys
                    val delayTimer = volley * 0.1f

                    for (i in 0..2) {
                        val angle = baseAngle - burstSpread + (burstSpread * i)
                        val proj = Projectile(
                            position = Vector2(position),
                            direction = angle.toFloat(),
                            speed = projectileSpeed * 1.2f,
                            damage = burstDamage
                        )

                        // Apply BURST weapon upgrades
                        if (burstExplosion) {
                            proj.setExplosive(true, burstExplosionRadius, burstDamage / 2)
                        }

                        // Add delay for volleys after the first one
                        if (volley > 0) {
                            proj.setDelayed(delayTimer)
                        }

                        projectiles.add(proj)
                    }
                }
            }
            WeaponType.MELEE -> {
                // Create a melee attack with configurable range and damage
                val meleeDamage = (damage.toFloat() * 2f).toInt()

                if (meleeSpin) {
                    // Create multiple melee attacks in a circle around the player
                    val spinCount = 8
                    for (i in 0 until spinCount) {
                        val spinAngle = (i.toFloat() / spinCount) * (Math.PI * 2)
                        val spinX = cos(spinAngle) * (player.size * meleeRange / 2)
                        val spinY = sin(spinAngle) * (player.size * meleeRange / 2)
                        val spinPos = Vector2(position).add(spinX.toFloat(), spinY.toFloat())

                        val attack = MeleeAttack(
                            position = spinPos,
                            size = player.size * meleeRange / 2,
                            damage = meleeDamage,
                            duration = meleeDuration
                        )

                        if (meleeKnockback) {
                            attack.setKnockback(true, meleeKnockbackForce, spinAngle.toFloat())
                        }

                        meleeAttacks.add(attack)
                    }
                } else {
                    // Create a single melee attack in front of the player
                    val attack = MeleeAttack(
                        position = position,
                        size = player.size * meleeRange,
                        damage = meleeDamage,
                        duration = meleeDuration
                    )

                    if (meleeKnockback) {
                        attack.setKnockback(true, meleeKnockbackForce, baseAngle)
                    }

                    meleeAttacks.add(attack)
                }
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

    fun checkCollisions(enemies: List<BaseEnemy>) {
        // Check projectile collisions
        val projectilesToRemove = mutableListOf<Projectile>()
        val explosions = mutableListOf<Pair<Vector2, Float>>() // Position and radius pairs

        for (projectile in projectiles) {
            // Skip inactive projectiles
            if (!projectile.isActive()) continue

            var hitEnemy = false

            for (enemy in enemies) {
                if (projectile.bounds.overlaps(enemy.bounds)) {
                    // Apply damage to enemy
                    enemy.takeDamage(projectile.damage)
                    hitEnemy = true

                    // Handle chaining to nearby enemies
                    if (projectile.canChain()) {
                        // Find nearest enemy that's not the current one
                        val nearestEnemy = findNearestEnemyExcluding(enemies, enemy, projectile.position, 200f)
                        if (nearestEnemy != null) {
                            projectile.chainToEnemy(nearestEnemy)
                            continue // Don't remove projectile, it will chain
                        }
                    }

                    // Handle explosive projectiles
                    if (projectile.isExplosive()) {
                        explosions.add(Pair(Vector2(projectile.position), projectile.getExplosionRadius()))

                        // Find all enemies in explosion radius
                        for (targetEnemy in enemies) {
                            if (targetEnemy != enemy) { // Skip the enemy that was directly hit
                                val distance = Vector2(targetEnemy.position).dst(projectile.position)
                                if (distance <= projectile.getExplosionRadius()) {
                                    // Apply explosion damage with falloff based on distance
                                    val falloff = 1f - (distance / projectile.getExplosionRadius())
                                    val explosionDamage = (projectile.getExplosionDamage() * falloff).toInt()
                                    targetEnemy.takeDamage(explosionDamage)
                                }
                            }
                        }
                    }

                    // Remove projectile if it doesn't pierce
                    if (!projectile.canPierce()) {
                        projectilesToRemove.add(projectile)
                        break
                    }
                }
            }

            // If projectile hit an enemy and can pierce, don't remove it
            if (hitEnemy && projectile.canPierce()) {
                continue
            }
        }

        projectiles.removeAll(projectilesToRemove)

        // Check melee attack collisions
        for (meleeAttack in meleeAttacks) {
            for (enemy in enemies) {
                if (meleeAttack.bounds.overlaps(enemy.bounds)) {
                    // Apply damage
                    enemy.takeDamage(meleeAttack.damage)

                    // Apply knockback if enabled
                    if (meleeAttack.hasKnockback()) {
                        val knockbackForce = meleeAttack.getKnockbackForce()
                        val knockbackDirection = meleeAttack.getKnockbackDirection()

                        // Apply knockback to enemy by directly modifying position
                        val knockbackX = cos(knockbackDirection) * knockbackForce * 0.01f // Scale down for reasonable effect
                        val knockbackY = sin(knockbackDirection) * knockbackForce * 0.01f

                        enemy.position.x += knockbackX
                        enemy.position.y += knockbackY

                        // Ensure enemy stays within world bounds
                        enemy.position.x = enemy.position.x.coerceIn(0f, GameScreen.WORLD_WIDTH - enemy.size)
                        enemy.position.y = enemy.position.y.coerceIn(0f, GameScreen.WORLD_HEIGHT - enemy.size)

                        // Update enemy bounds
                        enemy.updateBounds()
                    }
                }
            }
        }

        // Render explosion effects (this would be better in a separate method)
        if (explosions.isNotEmpty()) {
            val shapeRenderer = ShapeRenderer()
            shapeRenderer.begin(ShapeType.Filled)
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.5f) // Orange semi-transparent

            for ((position, radius) in explosions) {
                shapeRenderer.circle(position.x, position.y, radius)
            }

            shapeRenderer.end()
        }
    }

    private fun findNearestEnemyExcluding(enemies: List<BaseEnemy>, excludeEnemy: BaseEnemy, position: Vector2, maxDistance: Float): BaseEnemy? {
        var nearest: BaseEnemy? = null
        var minDistance = Float.MAX_VALUE

        for (enemy in enemies) {
            if (enemy != excludeEnemy) {
                val distance = Vector2(enemy.position).dst(position)
                if (distance <= maxDistance && distance < minDistance) {
                    minDistance = distance
                    nearest = enemy
                }
            }
        }

        return nearest
    }
}
