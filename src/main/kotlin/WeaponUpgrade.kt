package org.example

enum class UpgradeType {
    DAMAGE,
    PROJECTILE_COUNT,
    ATTACK_SPEED,
    PROJECTILE_SPEED,
    SPREAD_ANGLE,    // For SPREAD weapon
    BEAM_WIDTH,      // For BEAM weapon
    BURST_RATE,      // For BURST weapon
    MELEE_RANGE,     // For MELEE weapon
    MELEE_DURATION   // For MELEE weapon
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
        // Generic upgrades (applicable to all weapons)
        WeaponUpgrade(
            UpgradeType.DAMAGE,
            "Increased Damage",
            "Increases damage by 10",
            { weapon -> weapon.increaseDamage(10) }
        ),
        WeaponUpgrade(
            UpgradeType.ATTACK_SPEED,
            "Faster Attacks",
            "Decreases time between attacks by 10%",
            { weapon -> weapon.increaseAttackSpeed(0.1f) }
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
            "Increases projectile speed by 20%",
            { weapon -> weapon.increaseProjectileSpeed(0.2f) },
            setOf(WeaponType.SIMPLE, WeaponType.SPREAD, WeaponType.BEAM, WeaponType.BURST)
        ),

        // Spread weapon specific
        WeaponUpgrade(
            UpgradeType.SPREAD_ANGLE,
            "Wider Spread",
            "Increases spread angle by 15%",
            { weapon -> weapon.increaseSpreadAngle(0.15f) },
            setOf(WeaponType.SPREAD)
        ),

        // Beam weapon specific
        WeaponUpgrade(
            UpgradeType.BEAM_WIDTH,
            "Wider Beam",
            "Increases beam width by 2",
            { weapon -> weapon.increaseBeamWidth(2f) },
            setOf(WeaponType.BEAM)
        ),

        // Burst weapon specific
        WeaponUpgrade(
            UpgradeType.BURST_RATE,
            "Enhanced Burst",
            "Increases burst damage rate by 20%",
            { weapon -> weapon.increaseBurstRate(0.2f) },
            setOf(WeaponType.BURST)
        ),

        // Melee weapon specific
        WeaponUpgrade(
            UpgradeType.MELEE_RANGE,
            "Extended Reach",
            "Increases melee range by 25%",
            { weapon -> weapon.increaseMeleeRange(0.25f) },
            setOf(WeaponType.MELEE)
        ),
        WeaponUpgrade(
            UpgradeType.MELEE_DURATION,
            "Lasting Strike",
            "Increases melee attack duration by 0.1 seconds",
            { weapon -> weapon.increaseMeleeDuration(0.1f) },
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
