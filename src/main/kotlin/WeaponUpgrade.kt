package org.example

enum class UpgradeType {
    DAMAGE,
    PROJECTILE_COUNT,
    ATTACK_SPEED,
    PROJECTILE_SPEED
}

data class WeaponUpgrade(
    val type: UpgradeType,
    val name: String,
    val description: String,
    val apply: (Weapon) -> Unit
)

class UpgradeSystem {
    private val availableUpgrades = listOf(
        WeaponUpgrade(
            UpgradeType.DAMAGE,
            "Increased Damage",
            "Increases projectile damage by 10",
            { weapon -> weapon.increaseDamage(10) }
        ),
        WeaponUpgrade(
            UpgradeType.PROJECTILE_COUNT,
            "Additional Projectile",
            "Adds one more projectile to each attack",
            { weapon -> weapon.increaseProjectileCount(1) }
        ),
        WeaponUpgrade(
            UpgradeType.ATTACK_SPEED,
            "Faster Attacks",
            "Decreases time between attacks by 10%",
            { weapon -> weapon.increaseAttackSpeed(0.1f) }
        ),
        WeaponUpgrade(
            UpgradeType.PROJECTILE_SPEED,
            "Faster Projectiles",
            "Increases projectile speed by 20%",
            { weapon -> weapon.increaseProjectileSpeed(0.2f) }
        )
    )
    
    fun getRandomUpgrades(count: Int = 3): List<WeaponUpgrade> {
        return availableUpgrades.shuffled().take(count)
    }
}