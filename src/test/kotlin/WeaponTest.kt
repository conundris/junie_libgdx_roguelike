package org.example

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeaponTest {
    @Test
    fun `test melee attack creation`() {
        val player = mock(Player::class.java)
        `when`(player.position).thenReturn(Vector2(100f, 100f))
        `when`(player.size).thenReturn(32f)
        `when`(player.direction).thenReturn(Vector2(1f, 0f))  // Default direction: right
        `when`(player.isAutoTargeting).thenReturn(false)

        val weapon = Weapon(player, WeaponType.MELEE)

        // Set attack timer to ready state through reflection
        val attackTimerField = weapon.javaClass.getDeclaredField("attackTimer")
        attackTimerField.isAccessible = true
        attackTimerField.set(weapon, 1000f) // Large value to ensure attack triggers

        weapon.update(0.1f, emptyList()) // Update to trigger attack

        // Access meleeAttacks through reflection
        val meleeAttacksField = weapon.javaClass.getDeclaredField("meleeAttacks")
        meleeAttacksField.isAccessible = true
        val meleeAttacks = meleeAttacksField.get(weapon) as List<*>

        assertTrue(meleeAttacks.isNotEmpty(), "Melee attack should be created")

        val meleeAttack = meleeAttacks[0] as MeleeAttack
        assertEquals(48f, meleeAttack.size, "Melee attack size should be 1.5x player size")
        assertTrue(meleeAttack.damage > 0, "Melee attack should have positive damage")
    }

    @Test
    fun `test melee attack collision`() {
        val player = mock(Player::class.java)
        `when`(player.position).thenReturn(Vector2(100f, 100f))
        `when`(player.size).thenReturn(32f)
        `when`(player.direction).thenReturn(Vector2(1f, 0f))  // Default direction: right
        `when`(player.isAutoTargeting).thenReturn(false)

        val enemy = mock(Enemy::class.java)
        // Position enemy closer to player to be within melee range
        `when`(enemy.position).thenReturn(Vector2(110f, 110f))
        `when`(enemy.bounds).thenReturn(Rectangle(110f, 110f, 32f, 32f))

        val weapon = Weapon(player, WeaponType.MELEE)

        // Set attack timer to ready state through reflection
        val attackTimerField = weapon.javaClass.getDeclaredField("attackTimer")
        attackTimerField.isAccessible = true
        attackTimerField.set(weapon, 1000f) // Large value to ensure attack triggers

        weapon.update(0.1f, listOf(enemy)) // Create melee attack
        weapon.checkCollisions(listOf(enemy))

        verify(enemy, times(1)).takeDamage(any())
    }

    @Test
    fun `test weapon type switching`() {
        val player = mock(Player::class.java)
        val weapon = Weapon(player, WeaponType.SIMPLE)

        weapon.switchWeapon() // SIMPLE -> SPREAD
        assertEquals(WeaponType.SPREAD, weapon.getCurrentWeaponType())

        weapon.switchWeapon() // SPREAD -> BEAM
        assertEquals(WeaponType.BEAM, weapon.getCurrentWeaponType())

        weapon.switchWeapon() // BEAM -> BURST
        assertEquals(WeaponType.BURST, weapon.getCurrentWeaponType())

        weapon.switchWeapon() // BURST -> MELEE
        assertEquals(WeaponType.MELEE, weapon.getCurrentWeaponType())

        weapon.switchWeapon() // MELEE -> SIMPLE
        assertEquals(WeaponType.SIMPLE, weapon.getCurrentWeaponType())
    }

    @Test
    fun `test weapon specific upgrades`() {
        val player = mock(Player::class.java)
        `when`(player.position).thenReturn(Vector2(100f, 100f))
        `when`(player.size).thenReturn(32f)
        `when`(player.direction).thenReturn(Vector2(1f, 0f))
        `when`(player.isAutoTargeting).thenReturn(false)

        // Test SPREAD weapon upgrades
        val spreadWeapon = Weapon(player, WeaponType.SPREAD)
        val spreadAngleField = spreadWeapon.javaClass.getDeclaredField("spreadAngle")
        spreadAngleField.isAccessible = true
        val initialSpreadAngle = spreadAngleField.get(spreadWeapon) as Double
        spreadWeapon.increaseSpreadAngle(0.15f)
        val updatedSpreadAngle = spreadAngleField.get(spreadWeapon) as Double
        assertTrue(updatedSpreadAngle > initialSpreadAngle, "Spread angle should increase")

        // Test BEAM weapon upgrades
        val beamWeapon = Weapon(player, WeaponType.BEAM)
        val beamWidthField = beamWeapon.javaClass.getDeclaredField("beamWidth")
        beamWidthField.isAccessible = true
        val initialBeamWidth = beamWidthField.get(beamWeapon) as Float
        beamWeapon.increaseBeamWidth(2f)
        val updatedBeamWidth = beamWidthField.get(beamWeapon) as Float
        assertEquals(initialBeamWidth + 2f, updatedBeamWidth, "Beam width should increase by 2")

        // Test BURST weapon upgrades
        val burstWeapon = Weapon(player, WeaponType.BURST)
        val burstRateField = burstWeapon.javaClass.getDeclaredField("burstRate")
        burstRateField.isAccessible = true
        val initialBurstRate = burstRateField.get(burstWeapon) as Float
        burstWeapon.increaseBurstRate(0.2f)
        val updatedBurstRate = burstRateField.get(burstWeapon) as Float
        assertTrue(updatedBurstRate > initialBurstRate, "Burst rate should increase")

        // Test MELEE weapon upgrades
        val meleeWeapon = Weapon(player, WeaponType.MELEE)
        val meleeRangeField = meleeWeapon.javaClass.getDeclaredField("meleeRange")
        meleeRangeField.isAccessible = true
        val initialMeleeRange = meleeRangeField.get(meleeWeapon) as Float
        meleeWeapon.increaseMeleeRange(0.25f)
        val updatedMeleeRange = meleeRangeField.get(meleeWeapon) as Float
        assertTrue(updatedMeleeRange > initialMeleeRange, "Melee range should increase")
    }

    @Test
    fun `test upgrade system weapon type filtering`() {
        val player = mock(Player::class.java)
        val upgradeSystem = UpgradeSystem()

        // Test MELEE weapon upgrades
        val meleeWeapon = Weapon(player, WeaponType.MELEE)
        val meleeUpgrades = upgradeSystem.getRandomUpgrades(meleeWeapon)
        assertTrue(meleeUpgrades.all { it.applicableWeaponTypes.contains(WeaponType.MELEE) },
            "All upgrades should be applicable to MELEE weapon")
        assertTrue(meleeUpgrades.any { it.type == UpgradeType.MELEE_RANGE || it.type == UpgradeType.MELEE_DURATION },
            "Should include MELEE-specific upgrades")

        // Test SPREAD weapon upgrades
        val spreadWeapon = Weapon(player, WeaponType.SPREAD)
        val spreadUpgrades = upgradeSystem.getRandomUpgrades(spreadWeapon)
        assertTrue(spreadUpgrades.all { it.applicableWeaponTypes.contains(WeaponType.SPREAD) },
            "All upgrades should be applicable to SPREAD weapon")
        assertTrue(spreadUpgrades.any { it.type == UpgradeType.SPREAD_ANGLE },
            "Should include SPREAD-specific upgrades")
    }
}
