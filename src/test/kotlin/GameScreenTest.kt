package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GameScreenTest {
    private lateinit var app: Application
    private lateinit var game: VampireSurvivorsGame
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var ui: GameUI
    private lateinit var batch: SpriteBatch
    private lateinit var screen: GameScreen

    init {
        // Initialize LibGDX headless backend
        val config = HeadlessApplicationConfiguration()
        app = HeadlessApplication(object : com.badlogic.gdx.ApplicationAdapter() {}, config)
    }

    @BeforeEach
    fun setUp() {
        // Mock dependencies
        game = mock()
        camera = mock()
        shapeRenderer = mock()
        ui = mock()
        batch = mock()

        whenever(game.batch).thenReturn(batch)

        // Create GameScreen with BEAM weapon
        screen = GameScreen.createForTesting(game, camera, shapeRenderer, ui)
        screen.player = Player(WeaponType.BEAM)
    }

    @Test
    fun `test weapon type persists through game reset`() {
        // Get initial weapon type
        val initialType = screen.player.weapon.getCurrentWeaponType()
        println("[DEBUG_LOG] Initial weapon type: $initialType")
        assert(initialType == WeaponType.BEAM) { "Expected BEAM weapon type, but got $initialType" }

        // Reset game
        screen.resetGame()

        // Verify weapon type is preserved
        val resetType = screen.player.weapon.getCurrentWeaponType()
        println("[DEBUG_LOG] Weapon type after reset: $resetType")
        assert(resetType == WeaponType.BEAM) { "Expected BEAM weapon type after reset, but got $resetType" }
    }
}
