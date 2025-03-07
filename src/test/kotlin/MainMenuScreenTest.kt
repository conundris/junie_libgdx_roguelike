package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class MainMenuScreenTest {
    private lateinit var app: Application

    init {
        // Initialize LibGDX headless backend
        val config = HeadlessApplicationConfiguration()
        app = HeadlessApplication(object : com.badlogic.gdx.ApplicationAdapter() {
            override fun create() {
                // Initialize OpenGL context
                Gdx.gl = mock()
                Gdx.gl20 = mock()
            }
        }, config)
    }
    private lateinit var game: VampireSurvivorsGame
    private lateinit var font: BitmapFont
    private lateinit var layout: GlyphLayout
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var screen: MainMenuScreen
    private lateinit var batch: SpriteBatch

    @BeforeEach
    fun setUp() {
        // Mock dependencies
        game = mock()
        font = mock()
        layout = mock()
        shapeRenderer = mock()
        batch = mock()

        whenever(game.batch).thenReturn(batch)

        screen = MainMenuScreen.createForTesting(game, font, layout, shapeRenderer)
    }

    @Test
    fun `test weapon selection cycles through types`() {
        // Initial weapon should be SIMPLE
        assert(screen.getSelectedWeaponType() == WeaponType.SIMPLE)

        // Press RIGHT to cycle forward
        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.SPREAD)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.BEAM)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.BURST)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.MELEE)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.SIMPLE)

        // Press LEFT to cycle backward
        screen.handleInput(Keys.LEFT)
        assert(screen.getSelectedWeaponType() == WeaponType.MELEE)
    }

    @Test
    fun `test weapon selection before starting game`() {
        // Create mock GameScreen
        val mockGameScreen: GameScreen = mock()

        // Set up mock factory
        var capturedWeaponType: WeaponType? = null
        var capturedDifficulty: DifficultyLevel? = null
        screen.gameScreenFactory = { _, weaponType, difficulty ->
            capturedWeaponType = weaponType
            capturedDifficulty = difficulty
            mockGameScreen
        }

        // Select BEAM weapon
        screen.handleInput(Keys.RIGHT)
        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedWeaponType() == WeaponType.BEAM) {
            "Expected BEAM weapon type, but got ${screen.getSelectedWeaponType()}"
        }

        // Start game
        screen.handleInput(Keys.SPACE)

        // Verify game screen was created with correct weapon type and default difficulty
        verify(game).setScreen(mockGameScreen)
        assert(capturedWeaponType == WeaponType.BEAM) {
            "Expected BEAM weapon type passed to factory, but got $capturedWeaponType"
        }
        assert(capturedDifficulty == DifficultyLevel.NORMAL) {
            "Expected NORMAL difficulty passed to factory, but got $capturedDifficulty"
        }
    }

    @Test
    fun `test difficulty selection cycles through levels`() {
        // Initial difficulty should be NORMAL
        assert(screen.getSelectedDifficulty() == DifficultyLevel.NORMAL)

        // Press UP to cycle forward
        screen.handleInput(Keys.UP)
        assert(screen.getSelectedDifficulty() == DifficultyLevel.HARD)

        screen.handleInput(Keys.UP)
        assert(screen.getSelectedDifficulty() == DifficultyLevel.EASY)

        screen.handleInput(Keys.UP)
        assert(screen.getSelectedDifficulty() == DifficultyLevel.NORMAL)

        // Press DOWN to cycle backward
        screen.handleInput(Keys.DOWN)
        assert(screen.getSelectedDifficulty() == DifficultyLevel.EASY)
    }

    @Test
    fun `test difficulty selection before starting game`() {
        // Create mock GameScreen
        val mockGameScreen: GameScreen = mock()

        // Set up mock factory
        var capturedWeaponType: WeaponType? = null
        var capturedDifficulty: DifficultyLevel? = null
        screen.gameScreenFactory = { _, weaponType, difficulty ->
            capturedWeaponType = weaponType
            capturedDifficulty = difficulty
            mockGameScreen
        }

        // Select HARD difficulty
        screen.handleInput(Keys.UP)
        assert(screen.getSelectedDifficulty() == DifficultyLevel.HARD) {
            "Expected HARD difficulty, but got ${screen.getSelectedDifficulty()}"
        }

        // Start game
        screen.handleInput(Keys.SPACE)

        // Verify game screen was created with correct difficulty
        verify(game).setScreen(mockGameScreen)
        assert(capturedDifficulty == DifficultyLevel.HARD) {
            "Expected HARD difficulty passed to factory, but got $capturedDifficulty"
        }
    }
}
