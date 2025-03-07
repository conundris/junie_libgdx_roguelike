package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class GameUIDebugTest {
    private lateinit var application: Application
    private lateinit var gameUI: GameUI
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var input: com.badlogic.gdx.Input
    private lateinit var debugMetrics: DebugMetrics

    @BeforeEach
    fun setup() {
        // Set up headless application
        val config = HeadlessApplicationConfiguration()
        application = HeadlessApplication(mock(), config)

        // Mock GL20
        val gl20 = mock<com.badlogic.gdx.graphics.GL20>()
        whenever(gl20.glGenTexture()).thenReturn(1)

        // Mock graphics
        val graphics: Graphics = mock()
        whenever(graphics.width).thenReturn(800)
        whenever(graphics.height).thenReturn(600)

        // Mock input
        input = mock()

        // Set up Gdx context
        Gdx.gl = gl20
        Gdx.gl20 = gl20
        Gdx.graphics = graphics
        Gdx.input = input

        // Create mocked components
        batch = mock()
        shapeRenderer = mock()

        // Mock BitmapFont
        val font = mock<com.badlogic.gdx.graphics.g2d.BitmapFont>()
        val fontData = mock<com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData>()
        whenever(font.data).thenReturn(fontData)

        // Create GameUI
        gameUI = GameUI(batch)

        // Replace font in GameUI with mock
        GameUI::class.java.getDeclaredField("font").apply {
            isAccessible = true
            set(gameUI, font)
        }

        // Get debug metrics instance
        debugMetrics = DebugMetrics.getInstance()
    }

    @Test
    fun `test debug metrics toggle with F3 and O keys`() {
        val player = Player()
        val enemies = emptyList<Enemy>()
        val projectiles = emptyList<Projectile>()

        // Initially metrics should be invisible
        assert(!debugMetrics.isVisible()) { "Debug metrics should be initially invisible" }

        // Test F3 key toggle
        whenever(input.isKeyJustPressed(Keys.F3)).thenReturn(true)
        whenever(input.isKeyJustPressed(Keys.O)).thenReturn(false)
        gameUI.render(shapeRenderer, player, enemies, projectiles)
        assert(debugMetrics.isVisible()) { "Debug metrics should be visible after F3 press" }

        // Reset F3 key and test O key toggle
        whenever(input.isKeyJustPressed(Keys.F3)).thenReturn(false)
        whenever(input.isKeyJustPressed(Keys.O)).thenReturn(true)
        gameUI.render(shapeRenderer, player, enemies, projectiles)
        assert(!debugMetrics.isVisible()) { "Debug metrics should be invisible after O press" }

        // Test O key toggle again
        gameUI.render(shapeRenderer, player, enemies, projectiles)
        assert(debugMetrics.isVisible()) { "Debug metrics should be visible after second O press" }
    }
}
