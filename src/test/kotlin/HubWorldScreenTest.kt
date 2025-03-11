package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.mockito.ArgumentMatchers.anyFloat
import org.example.Player

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
    private lateinit var camera: OrthographicCamera
    private lateinit var font: BitmapFont
    private lateinit var layout: GlyphLayout
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var screen: HubWorldScreen
    private lateinit var batch: SpriteBatch

    @BeforeEach
    fun setUp() {
        // Mock game and its dependencies
        game = mock()
        camera = mock()
        batch = mock()
        font = mock()
        layout = mock()
        shapeRenderer = mock()

        // Set up game's shared resources
        whenever(game.getBatch()).thenReturn(batch)
        whenever(game.getFont()).thenReturn(font)
        whenever(game.getLayout()).thenReturn(layout)
        whenever(game.getShapeRenderer()).thenReturn(shapeRenderer)
        whenever(batch.projectionMatrix).thenReturn(mock())

        // Mock camera position and viewport
        val position = mock<Vector3>()
        whenever(camera.position).thenReturn(position)
        whenever(camera.viewportWidth).thenReturn(HubWorldScreen.WORLD_WIDTH)
        whenever(camera.viewportHeight).thenReturn(HubWorldScreen.WORLD_HEIGHT)
        whenever(camera.combined).thenReturn(mock())

        screen = HubWorldScreen.createForTesting(
            game = game,
            camera = camera,
            font = font,
            layout = layout,
            shapeRenderer = shapeRenderer
        )
    }

    @Test
    fun `test B button triggers map selection`() {
        // Mock input
        whenever(Gdx.input.isKeyJustPressed(Keys.B)).thenReturn(true)

        // Create mock MapSelectionScreen
        val mockMapSelectionScreen = mock<MapSelectionScreen>()

        // Capture the screen that was set
        var capturedScreen: Screen? = null
        whenever(game.setScreen(any())).then { invocation ->
            capturedScreen = invocation.arguments[0] as Screen
            Unit
        }

        // Handle input
        screen.handleInput(0.016f)  // typical delta time

        // Verify MapSelectionScreen was created and set
        verify(game).setScreen(any())
        assert(capturedScreen is MapSelectionScreen)
    }

    @Test
    fun `test player movement`() {
        val delta = 0.016f  // typical delta time
        val initialPosition = screen.getPlayerPosition().cpy()

        // Test right movement
        whenever(Gdx.input.isKeyPressed(Keys.RIGHT)).thenReturn(true)
        screen.handleInput(delta)
        assert(screen.getPlayerPosition().x > initialPosition.x) { 
            "Player should move right. Expected x > ${initialPosition.x}, but was ${screen.getPlayerPosition().x}" 
        }

        // Reset position
        screen.getPlayerPosition().set(initialPosition)

        // Test up movement
        whenever(Gdx.input.isKeyPressed(Keys.RIGHT)).thenReturn(false)
        whenever(Gdx.input.isKeyPressed(Keys.UP)).thenReturn(true)
        screen.handleInput(delta)
        assert(screen.getPlayerPosition().y > initialPosition.y) { 
            "Player should move up. Expected y > ${initialPosition.y}, but was ${screen.getPlayerPosition().y}" 
        }

        // Test diagonal movement
        screen.getPlayerPosition().set(initialPosition)
        whenever(Gdx.input.isKeyPressed(Keys.RIGHT)).thenReturn(true)
        whenever(Gdx.input.isKeyPressed(Keys.UP)).thenReturn(true)
        screen.handleInput(delta)
        assert(screen.getPlayerPosition().x > initialPosition.x && screen.getPlayerPosition().y > initialPosition.y) {
            "Player should move diagonally. Position: ${screen.getPlayerPosition()}, Initial: $initialPosition"
        }
    }

    @Test
    fun `test hint display`() {
        // Mock layout behavior
        whenever(layout.width).thenReturn(100f)

        // Render with hint timer active
        screen.render(0f)

        // Verify hint text was drawn
        verify(font).draw(eq(batch), eq("Press B to open map selection"), anyFloat(), anyFloat())

        // Render after hint timer expires
        screen.render(6f)  // HINT_DISPLAY_TIME is 5f

        // Verify hint text was not drawn
        verify(font, times(1)).draw(eq(batch), eq("Press B to open map selection"), anyFloat(), anyFloat())
    }

    @Test
    fun `test camera follows player`() {
        val delta = 0.016f
        val initialPosition = screen.getPlayerPosition().cpy()

        // Move player right
        whenever(Gdx.input.isKeyPressed(Keys.RIGHT)).thenReturn(true)
        screen.handleInput(delta)

        // Camera should be centered on player
        val expectedX = screen.getPlayerPosition().x + Player.SIZE / 2
        val expectedY = screen.getPlayerPosition().y + Player.SIZE / 2

        // Verify camera position was updated
        verify(camera).update()
        verify(camera.position).set(
            eq(expectedX.coerceIn(camera.viewportWidth / 2, HubWorldScreen.WORLD_WIDTH - camera.viewportWidth / 2)),
            eq(expectedY.coerceIn(camera.viewportHeight / 2, HubWorldScreen.WORLD_HEIGHT - camera.viewportHeight / 2)),
            eq(0f)
        )
    }
}
