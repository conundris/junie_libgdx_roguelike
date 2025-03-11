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

class HubWorldScreenTest {
    private lateinit var app: Application

    init {
        // Initialize LibGDX headless backend
        val config = HeadlessApplicationConfiguration()
        app = HeadlessApplication(object : com.badlogic.gdx.ApplicationAdapter() {
            override fun create() {
                // Initialize OpenGL context
                Gdx.gl = mock()
                Gdx.gl20 = mock()
                // Mock Gdx.graphics
                Gdx.graphics = mock()
                whenever(Gdx.graphics.width).thenReturn(800)
                whenever(Gdx.graphics.height).thenReturn(600)
                // Mock Gdx.input
                Gdx.input = mock()
            }
        }, config)
    }

    private lateinit var game: VampireSurvivorsGame
    private lateinit var font: BitmapFont
    private lateinit var layout: GlyphLayout
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var screen: HubWorldScreen
    private lateinit var batch: SpriteBatch
    private val weaponType = WeaponType.SIMPLE
    private val difficultyLevel = DifficultyLevel.NORMAL

    @BeforeEach
    fun setUp() {
        // Mock dependencies
        game = mock()
        font = mock()
        layout = mock()
        shapeRenderer = mock()
        batch = mock()

        whenever(game.batch).thenReturn(batch)

        // Mock batch behavior
        whenever(batch.projectionMatrix).thenReturn(com.badlogic.gdx.math.Matrix4())

        screen = HubWorldScreen.createForTesting(
            game = game,
            font = font,
            layout = layout,
            shapeRenderer = shapeRenderer,
            weaponType = weaponType,
            difficultyLevel = difficultyLevel
        )
    }

    @Test
    fun `test map selection cycles through maps`() {
        // Initial map should be FOREST
        assert(screen.getSelectedMap() == MapType.FOREST)

        // Press RIGHT to cycle forward
        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.DESERT)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.DUNGEON)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.CASTLE)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.GRAVEYARD)

        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.FOREST)

        // Press LEFT to cycle backward
        screen.handleInput(Keys.LEFT)
        assert(screen.getSelectedMap() == MapType.GRAVEYARD)
    }

    @Test
    fun `test starting game with selected map`() {
        // Create mock GameScreen
        val mockGameScreen: GameScreen = mock()

        // Set up mock factory
        var capturedWeaponType: WeaponType? = null
        var capturedDifficulty: DifficultyLevel? = null
        var capturedMapType: MapType? = null
        screen.gameScreenFactory = { _, weaponType, difficulty, mapType ->
            capturedWeaponType = weaponType
            capturedDifficulty = difficulty
            capturedMapType = mapType
            mockGameScreen
        }

        // Select DUNGEON map
        screen.handleInput(Keys.RIGHT)
        screen.handleInput(Keys.RIGHT)
        assert(screen.getSelectedMap() == MapType.DUNGEON) {
            "Expected DUNGEON map type, but got ${screen.getSelectedMap()}"
        }

        // Start game
        screen.handleInput(Keys.SPACE)

        // Verify game screen was created with correct parameters
        verify(game).setScreen(mockGameScreen)
        assert(capturedWeaponType == WeaponType.SIMPLE) {
            "Expected SIMPLE weapon type passed to factory, but got $capturedWeaponType"
        }
        assert(capturedDifficulty == DifficultyLevel.NORMAL) {
            "Expected NORMAL difficulty passed to factory, but got $capturedDifficulty"
        }
        assert(capturedMapType == MapType.DUNGEON) {
            "Expected DUNGEON map type passed to factory, but got $capturedMapType"
        }
    }

    @Test
    fun `test returning to main menu`() {
        // Create mock MainMenuScreen
        val mockMainMenuScreen: MainMenuScreen = mock()

        // Set up mock factory
        screen.mainMenuScreenFactory = { game ->
            mockMainMenuScreen
        }

        // Press ESC to return to main menu
        screen.handleInput(Keys.ESCAPE)

        // Verify transition to main menu
        verify(game).setScreen(mockMainMenuScreen)
    }
}
