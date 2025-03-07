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
        camera = OrthographicCamera()  // Use real camera instead of mock
        camera.position.set(GameScreen.VIEWPORT_WIDTH / 2, GameScreen.VIEWPORT_HEIGHT / 2, 0f)
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

    @Test
    fun `test enemy spawning mechanics`() {
        // Initial state (difficulty level 1)
        screen.gameTime = 0f
        screen.difficultyLevel = 1
        screen.spawnTimer = 0f
        screen.enemies.clear()

        // Trigger spawn at difficulty 1
        screen.updateEnemies(2.0f)  // Ensure we pass spawn interval
        val enemiesAtDiff1 = screen.enemies.size
        println("[DEBUG_LOG] Enemies spawned at difficulty 1: $enemiesAtDiff1")
        assert(enemiesAtDiff1 >= 2) { "Expected at least 2 enemies at difficulty 1, got $enemiesAtDiff1" }

        // Test at difficulty 3
        screen.enemies.clear()
        screen.gameTime = 120f  // 2 minutes = difficulty 3
        screen.updateDifficulty(0f)
        screen.spawnTimer = 0f

        screen.updateEnemies(2.0f)
        val enemiesAtDiff3 = screen.enemies.size
        println("[DEBUG_LOG] Enemies spawned at difficulty 3: $enemiesAtDiff3")
        assert(enemiesAtDiff3 >= 3) { "Expected at least 3 enemies at difficulty 3, got $enemiesAtDiff3" }
        assert(enemiesAtDiff3 > enemiesAtDiff1) { "Expected more enemies at higher difficulty" }

        // Verify spawn interval decreases with difficulty
        val intervalAtDiff1 = 1.5f  // Base interval at difficulty 1
        val intervalAtDiff3 = screen.spawnInterval
        println("[DEBUG_LOG] Spawn interval at difficulty 3: $intervalAtDiff3")
        assert(intervalAtDiff3 < intervalAtDiff1) { "Expected shorter spawn interval at higher difficulty" }
    }
}
