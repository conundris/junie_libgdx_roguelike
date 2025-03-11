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
import org.mockito.ArgumentMatchers.anyFloat

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

        whenever(game.getBatch()).thenReturn(batch)

        // Mock batch behavior
        whenever(batch.projectionMatrix).thenReturn(com.badlogic.gdx.math.Matrix4())

        screen = MainMenuScreen.createForTesting(game, font, layout, shapeRenderer)
    }

    @Test
    fun `test space key transitions to hub world`() {
        // Create mock HubWorldScreen
        val mockHubWorldScreen: HubWorldScreen = mock()

        // Set up mock factory
        screen.hubWorldScreenFactory = { game: VampireSurvivorsGame ->
            mockHubWorldScreen
        }

        // Press SPACE to start game
        screen.handleInput(Keys.SPACE)

        // Verify hub world screen was created
        verify(game).setScreen(mockHubWorldScreen)
    }

    @Test
    fun `test render displays correct text`() {
        // Mock font behavior for title
        whenever(layout.width).thenReturn(100f)  // arbitrary width

        screen.render(0f)

        // Verify font configuration
        verify(font).getData()
        verify(font).setColor(any())

        // Verify title and play text were drawn
        verify(font).draw(eq(batch), eq("Vampire Survivors Clone"), anyFloat(), anyFloat())
        verify(font).draw(eq(batch), eq("Press SPACE to Play"), anyFloat(), anyFloat())
    }
}
