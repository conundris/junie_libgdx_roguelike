package org.example

class Experience(private val player: Player) {
    var currentExp = 0
    var level = 1
    private val baseExpNeeded = 100
    private val upgradeSystem = UpgradeSystem()
    var isSelectingUpgrade = false
    var availableUpgrades: List<WeaponUpgrade> = emptyList()

    val expToNextLevel: Int
        get() = baseExpNeeded * level

    val currentLevelProgress: Float
        get() = currentExp.toFloat() / expToNextLevel

    fun addExp(amount: Int) {
        if (!isSelectingUpgrade) {
            currentExp += amount
            checkLevelUp()
        }
    }

    private fun checkLevelUp() {
        while (currentExp >= expToNextLevel) {
            currentExp -= expToNextLevel
            level++
            onLevelUp()
        }
    }

    private fun onLevelUp() {
        isSelectingUpgrade = true
        availableUpgrades = upgradeSystem.getRandomUpgrades(player.weapon)
    }

    fun selectUpgrade(index: Int) {
        if (isSelectingUpgrade && index in availableUpgrades.indices) {
            availableUpgrades[index].apply(player.weapon)
            isSelectingUpgrade = false
            availableUpgrades = emptyList()
        }
    }
}
