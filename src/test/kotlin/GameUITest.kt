package org.example

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameUITest {
    private lateinit var application: Application
    private lateinit var gl20: GL20
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var gameUI: GameUI
    private lateinit var font: BitmapFont
    private lateinit var fontData: BitmapFont.BitmapFontData
    private lateinit var layout: GlyphLayout

    @BeforeAll
    fun setup() {
        // Set up headless application
        val config = HeadlessApplicationConfiguration()
        application = HeadlessApplication(mock(), config)

        // Mock GL20
        gl20 = mock()
        val graphics: Graphics = mock()
        whenever(graphics.width).thenReturn(800)
        whenever(graphics.height).thenReturn(600)

        // Set up Gdx context
        Gdx.gl = gl20
        Gdx.gl20 = gl20
        Gdx.graphics = graphics

        // Mock font and font data
        font = mock<BitmapFont>()
        fontData = mock<BitmapFont.BitmapFontData>()
        layout = mock<GlyphLayout>()
        whenever(font.data).thenReturn(fontData)
        whenever(font.color).thenReturn(Color.WHITE)

        // Set up font draw method to return dummy values
        whenever(font.draw(any(), any<String>(), any<Float>(), any<Float>())).thenReturn(mock())

        // Mock layout behavior
        doNothing().whenever(layout).setText(any(), any<String>(), any(), any(), any(), any())

        // Create mocked components
        batch = mock()
        shapeRenderer = mock()

        // Create GameUI with mocked components
        gameUI = GameUI(batch)
        GameUI::class.java.getDeclaredField("font").apply {
            isAccessible = true
            set(gameUI, font)
        }
        GameUI::class.java.getDeclaredField("layout").apply {
            isAccessible = true
            set(gameUI, layout)
        }
    }

    @AfterAll
    fun teardown() {
        application.exit()
    }

    @Test
    fun `test weapon upgrade screen text rendering`() {
        // Create a player with upgrade selection active
        val player = Player().apply {
            experience.isSelectingUpgrade = true
            experience.availableUpgrades = listOf(
                WeaponUpgrade(
                    type = UpgradeType.DAMAGE,
                    name = "Test Upgrade",
                    description = "Test Description",
                    apply = { weapon -> weapon.increaseDamage(10) }
                )
            )
        }

        // Create empty lists for enemies and projectiles
        val enemies = emptyList<Enemy>()
        val projectiles = emptyList<Projectile>()

        // Render the UI
        gameUI.render(shapeRenderer, player, enemies, projectiles)

        // Verify batch management (multiple begin/end pairs are expected)
        verify(batch, atLeast(1)).begin()
        verify(batch, atLeast(1)).end()

        // Verify the order of operations
        val inOrder = inOrder(font, font.data, batch)

        // Initial batch operations
        inOrder.verify(batch).begin()

        // Health and experience text rendering
        verify(font, atLeastOnce()).draw(
            eq(batch),
            argThat<String> { contains("Health") || contains("Level") },
            any<Float>(),
            any<Float>()
        )

        // Title rendering
        inOrder.verify(font.data).setScale(1.5f)
        inOrder.verify(font).draw(
            eq(batch),
            argThat<String> { contains("Level Up!") },
            any<Float>(),
            any<Float>()
        )

        // Upgrade text rendering
        inOrder.verify(font.data).setScale(1f)
        inOrder.verify(font).draw(
            eq(batch),
            argThat<String> { contains("Test Upgrade") },
            any<Float>(),
            any<Float>()
        )

        // Final reset
        inOrder.verify(font.data).setScale(1f)
        inOrder.verify(batch).end()
    }

    @Test
    fun `test weapon upgrade screen batch management`() {
        val player = Player().apply {
            experience.isSelectingUpgrade = true
        }

        // Create empty lists for enemies and projectiles
        val enemies = emptyList<Enemy>()
        val projectiles = emptyList<Projectile>()

        // Create an ordered verification
        val inOrder = inOrder(batch)

        // Render the UI
        gameUI.render(shapeRenderer, player, enemies, projectiles)

        // Verify proper batch management
        inOrder.verify(batch).begin()
        inOrder.verify(batch).end()
    }
}
