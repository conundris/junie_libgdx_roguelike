package org.example

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TestPlayer(
    initialX: Float? = null,
    initialY: Float? = null,
    worldWidth: Float = HubWorldScreen.WORLD_WIDTH,
    worldHeight: Float = HubWorldScreen.WORLD_HEIGHT
) : Player(
    initialWeaponType = WeaponType.SIMPLE,
    initialX = initialX,
    initialY = initialY,
    worldWidth = worldWidth,
    worldHeight = worldHeight
) {
    private val mockWeapon: Weapon = mock()
    private val mockExperience: Experience = mock()
    private val mockPosition: Vector2 = mock()
    private var currentX = initialX ?: worldWidth / 2
    private var currentY = initialY ?: worldHeight / 2

    init {
        whenever(mockPosition.x).thenAnswer { currentX }
        whenever(mockPosition.y).thenAnswer { currentY }
        whenever(mockPosition.cpy()).thenAnswer { Vector2(currentX, currentY) }
        whenever(mockPosition.set(any(), any())).thenAnswer { 
            currentX = it.arguments[0] as Float
            currentY = it.arguments[1] as Float
            mockPosition
        }
    }

    override fun createWeapon(): Weapon = mockWeapon
    override fun createExperience(): Experience = mockExperience

    override val position: Vector2 get() = mockPosition
    override val weapon: Weapon get() = mockWeapon
    override val experience: Experience get() = mockExperience
}
