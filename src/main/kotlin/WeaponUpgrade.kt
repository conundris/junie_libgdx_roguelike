package org.example

enum class UpgradeType {
    // Generic offensive upgrades
    DAMAGE,
    PROJECTILE_COUNT,
    ATTACK_SPEED,
    PROJECTILE_SPEED,
    CRITICAL_CHANCE,     // Chance to deal critical damage
    CRITICAL_MULTIPLIER, // Multiplier for critical damage

    // Generic defensive upgrades
    HEALTH_BOOST,        // Increase max health
    DAMAGE_REDUCTION,    // Reduce damage taken
    HEALTH_REGEN,        // Regenerate health over time

    // Generic utility upgrades
    EXPERIENCE_BOOST,    // Increase experience gained
    PICKUP_RANGE,        // Increase range to collect pickups
    MOVEMENT_SPEED,      // Increase player movement speed

    // Weapon-specific upgrades - SIMPLE
    SIMPLE_PIERCING,     // Projectiles pierce through enemies
    SIMPLE_HOMING,       // Projectiles home in on enemies
    SIMPLE_BOUNCING,     // Projectiles bounce off walls

    // Weapon-specific upgrades - SPREAD
    SPREAD_ANGLE,        // Increase spread angle
    SPREAD_COVERAGE,     // Increase coverage area
    SPREAD_DENSITY,      // More projectiles in the same angle

    // Weapon-specific upgrades - BEAM
    BEAM_WIDTH,          // Increase beam width
    BEAM_PENETRATION,    // Beam penetrates through enemies
    BEAM_CHAIN,          // Beam chains to nearby enemies

    // Weapon-specific upgrades - BURST
    BURST_RATE,          // Increase burst damage rate
    BURST_VOLLEY,        // Fire more volleys per burst
    BURST_EXPLOSION,     // Projectiles explode on impact

    // Weapon-specific upgrades - MELEE
    MELEE_RANGE,         // Increase melee range
    MELEE_DURATION,      // Increase melee attack duration
    MELEE_KNOCKBACK,     // Add knockback effect to melee attacks
    MELEE_SPIN           // Melee attack spins around player
}

data class WeaponUpgrade(
    val type: UpgradeType,
    val name: String,
    val description: String,
    val apply: (Weapon) -> Unit,
    val applicableWeaponTypes: Set<WeaponType> = setOf(
        WeaponType.SIMPLE, WeaponType.SPREAD, WeaponType.BEAM, 
        WeaponType.BURST, WeaponType.MELEE
    )
)

class UpgradeSystem {
    private val availableUpgrades = listOf(
        // Generic offensive upgrades (applicable to all weapons)
        WeaponUpgrade(
            UpgradeType.DAMAGE,
            "Increased Damage",
            "Increases damage by 15",
            { weapon -> weapon.increaseDamage(15) }
        ),
        WeaponUpgrade(
            UpgradeType.ATTACK_SPEED,
            "Faster Attacks",
            "Decreases time between attacks by 15%",
            { weapon -> weapon.increaseAttackSpeed(0.15f) }
        ),
        WeaponUpgrade(
            UpgradeType.CRITICAL_CHANCE,
            "Critical Strike",
            "Adds 10% chance to deal critical damage",
            { weapon -> weapon.increaseCriticalChance(0.1f) }
        ),
        WeaponUpgrade(
            UpgradeType.CRITICAL_MULTIPLIER,
            "Critical Power",
            "Increases critical damage multiplier by 0.5",
            { weapon -> weapon.increaseCriticalMultiplier(0.5f) }
        ),

        // Projectile-based weapon upgrades
        WeaponUpgrade(
            UpgradeType.PROJECTILE_COUNT,
            "Additional Projectile",
            "Adds one more projectile to each attack",
            { weapon -> weapon.increaseProjectileCount(1) },
            setOf(WeaponType.SIMPLE, WeaponType.SPREAD, WeaponType.BURST)
        ),
        WeaponUpgrade(
            UpgradeType.PROJECTILE_SPEED,
            "Faster Projectiles",
            "Increases projectile speed by 25%",
            { weapon -> weapon.increaseProjectileSpeed(0.25f) },
            setOf(WeaponType.SIMPLE, WeaponType.SPREAD, WeaponType.BEAM, WeaponType.BURST)
        ),

        // SIMPLE weapon specific upgrades
        WeaponUpgrade(
            UpgradeType.SIMPLE_PIERCING,
            "Piercing Shot",
            "Projectiles pierce through enemies",
            { weapon -> weapon.enableSimplePiercing() },
            setOf(WeaponType.SIMPLE)
        ),
        WeaponUpgrade(
            UpgradeType.SIMPLE_HOMING,
            "Homing Shot",
            "Projectiles home in on nearby enemies",
            { weapon -> weapon.enableSimpleHoming() },
            setOf(WeaponType.SIMPLE)
        ),
        WeaponUpgrade(
            UpgradeType.SIMPLE_BOUNCING,
            "Bouncing Shot",
            "Projectiles bounce off walls up to 3 times",
            { weapon -> weapon.enableSimpleBouncing(3) },
            setOf(WeaponType.SIMPLE)
        ),

        // SPREAD weapon specific upgrades
        WeaponUpgrade(
            UpgradeType.SPREAD_ANGLE,
            "Wider Spread",
            "Increases spread angle by 25%",
            { weapon -> weapon.increaseSpreadAngle(0.25f) },
            setOf(WeaponType.SPREAD)
        ),
        WeaponUpgrade(
            UpgradeType.SPREAD_COVERAGE,
            "Enhanced Coverage",
            "Increases spread coverage by 30%",
            { weapon -> weapon.increaseSpreadCoverage(0.3f) },
            setOf(WeaponType.SPREAD)
        ),
        WeaponUpgrade(
            UpgradeType.SPREAD_DENSITY,
            "Dense Spread",
            "Increases projectile density by 50%",
            { weapon -> weapon.increaseSpreadDensity(0.5f) },
            setOf(WeaponType.SPREAD)
        ),

        // BEAM weapon specific upgrades
        WeaponUpgrade(
            UpgradeType.BEAM_WIDTH,
            "Wider Beam",
            "Increases beam width by 3",
            { weapon -> weapon.increaseBeamWidth(3f) },
            setOf(WeaponType.BEAM)
        ),
        WeaponUpgrade(
            UpgradeType.BEAM_PENETRATION,
            "Penetrating Beam",
            "Beam penetrates through enemies",
            { weapon -> weapon.enableBeamPenetration() },
            setOf(WeaponType.BEAM)
        ),
        WeaponUpgrade(
            UpgradeType.BEAM_CHAIN,
            "Chain Beam",
            "Beam chains to 2 additional nearby enemies",
            { weapon -> weapon.enableBeamChain(2) },
            setOf(WeaponType.BEAM)
        ),

        // BURST weapon specific upgrades
        WeaponUpgrade(
            UpgradeType.BURST_RATE,
            "Enhanced Burst",
            "Increases burst damage rate by 30%",
            { weapon -> weapon.increaseBurstRate(0.3f) },
            setOf(WeaponType.BURST)
        ),
        WeaponUpgrade(
            UpgradeType.BURST_VOLLEY,
            "Double Volley",
            "Fires an additional volley of projectiles",
            { weapon -> weapon.increaseBurstVolleyCount(1) },
            setOf(WeaponType.BURST)
        ),
        WeaponUpgrade(
            UpgradeType.BURST_EXPLOSION,
            "Explosive Burst",
            "Projectiles explode on impact, damaging nearby enemies",
            { weapon -> weapon.enableBurstExplosion(75f) },
            setOf(WeaponType.BURST)
        ),

        // MELEE weapon specific upgrades
        WeaponUpgrade(
            UpgradeType.MELEE_RANGE,
            "Extended Reach",
            "Increases melee range by 35%",
            { weapon -> weapon.increaseMeleeRange(0.35f) },
            setOf(WeaponType.MELEE)
        ),
        WeaponUpgrade(
            UpgradeType.MELEE_DURATION,
            "Lasting Strike",
            "Increases melee attack duration by 0.15 seconds",
            { weapon -> weapon.increaseMeleeDuration(0.15f) },
            setOf(WeaponType.MELEE)
        ),
        WeaponUpgrade(
            UpgradeType.MELEE_KNOCKBACK,
            "Forceful Strike",
            "Melee attacks knock enemies back",
            { weapon -> weapon.enableMeleeKnockback(150f) },
            setOf(WeaponType.MELEE)
        ),
        WeaponUpgrade(
            UpgradeType.MELEE_SPIN,
            "Whirlwind Strike",
            "Melee attack spins around you, hitting enemies in all directions",
            { weapon -> weapon.enableMeleeSpin() },
            setOf(WeaponType.MELEE)
        )
    )

    fun getRandomUpgrades(weapon: Weapon, count: Int = 3): List<WeaponUpgrade> {
        val weaponType = weapon.getCurrentWeaponType()
        val applicableUpgrades = availableUpgrades.filter { 
            it.applicableWeaponTypes.contains(weaponType) 
        }

        // Get weapon-specific upgrades
        val specificUpgrades = applicableUpgrades.filter { 
            it.applicableWeaponTypes.size == 1 && it.applicableWeaponTypes.contains(weaponType)
        }

        // Get generic upgrades
        val genericUpgrades = applicableUpgrades.filter { 
            it.applicableWeaponTypes.size > 1
        }

        // Always include at least one weapon-specific upgrade if available
        val result = mutableListOf<WeaponUpgrade>()
        if (specificUpgrades.isNotEmpty()) {
            result.add(specificUpgrades.random())
        }

        // Fill the remaining slots with random applicable upgrades
        val remainingUpgrades = (specificUpgrades + genericUpgrades).toMutableList()
        remainingUpgrades.removeAll(result)
        result.addAll(remainingUpgrades.shuffled().take(count - result.size))

        return result.shuffled()
    }
}
