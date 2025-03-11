package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

class HubWorldScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val camera: OrthographicCamera,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer,
    initialX: Float = WORLD_WIDTH / 2,
    initialY: Float = WORLD_HEIGHT / 2,
    private val playerFactory: (Float?, Float?, Float, Float) -> Player = { x, y, w, h -> Player(initialX = x, initialY = y, worldWidth = w, worldHeight = h) }
) : Screen {
    companion object {
        const val WORLD_WIDTH = 800f
        const val WORLD_HEIGHT = 600f
        const val HINT_DISPLAY_TIME = 5f  // Show hint for 5 seconds

        fun create(game: VampireSurvivorsGame): HubWorldScreen {
            val camera = OrthographicCamera().apply {
                setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT)
            }
            return HubWorldScreen(
                game = game,
                camera = camera,
                font = game.getFont(),
                layout = game.getLayout(),
                shapeRenderer = game.getShapeRenderer(),
                initialX = WORLD_WIDTH / 2,
                initialY = WORLD_HEIGHT / 2
            )
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            camera: OrthographicCamera,
            font: BitmapFont,
            layout: GlyphLayout,
            shapeRenderer: ShapeRenderer,
            initialX: Float = WORLD_WIDTH / 2,
            initialY: Float = WORLD_HEIGHT / 2,
            playerFactory: (Float?, Float?, Float, Float) -> Player = { x, y, w, h -> Player(initialX = x, initialY = y, worldWidth = w, worldHeight = h) }
        ): HubWorldScreen {
            return HubWorldScreen(
                game = game,
                camera = camera,
                font = font,
                layout = layout,
                shapeRenderer = shapeRenderer,
                initialX = initialX,
                initialY = initialY,
                playerFactory = playerFactory
            )
        }
    }

    private val player = playerFactory(initialX, initialY, WORLD_WIDTH, WORLD_HEIGHT)
    private var hintTimer = HINT_DISPLAY_TIME

    // For testing
    internal fun getPlayerPosition() = player.position
    private val hintText = "Press B to open map selection"

    override fun render(delta: Float) {
        // Update
        handleInput(delta)
        updateCamera()

        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.3f, 0.1f, 1f)  // Dark green for hub world
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Set up camera
        game.getBatch().projectionMatrix = camera.combined
        shapeRenderer.projectionMatrix = camera.combined

        // Draw hub world environment
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawHubWorld()
        shapeRenderer.end()

        // Draw player
        player.render(shapeRenderer)

        // Draw UI
        game.getBatch().begin()
        if (hintTimer > 0) {
            hintTimer -= delta
            font.getData().setScale(1.5f)
            layout.setText(font, hintText)
            font.setColor(Color.WHITE)
            font.draw(game.getBatch(), hintText,
                (WORLD_WIDTH - layout.width) / 2,
                WORLD_HEIGHT * 0.9f)
        }
        game.getBatch().end()
    }

    internal fun handleInput(delta: Float) {
        // Check for map selection trigger
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            game.setScreen(MapSelectionScreen.create(
                game = game,
                weaponType = WeaponType.SIMPLE,  // Default weapon for now
                difficultyLevel = DifficultyLevel.getDefault()
            ))
            return
        }

        // Handle player movement
        var moveX = 0f
        var moveY = 0f

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY += 1f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveY -= 1f
        }

        // Update player position
        if (moveX != 0f || moveY != 0f) {
            val movement = Vector2(moveX, moveY).nor()
            player.position.x += movement.x * player.speed * delta
            player.position.y += movement.y * player.speed * delta

            // Keep player within world bounds
            player.position.x = player.position.x.coerceIn(0f, WORLD_WIDTH - player.size)
            player.position.y = player.position.y.coerceIn(0f, WORLD_HEIGHT - player.size)
        }
    }

    private fun updateCamera() {
        // Center camera on player
        camera.position.x = player.position.x + player.size / 2
        camera.position.y = player.position.y + player.size / 2

        // Keep camera within world bounds
        val halfViewportWidth = camera.viewportWidth / 2
        val halfViewportHeight = camera.viewportHeight / 2

        camera.position.x = camera.position.x.coerceIn(
            halfViewportWidth,
            WORLD_WIDTH - halfViewportWidth
        )
        camera.position.y = camera.position.y.coerceIn(
            halfViewportHeight,
            WORLD_HEIGHT - halfViewportHeight
        )

        camera.update()
    }

    private fun drawHubWorld() {
        // Draw ground
        shapeRenderer.setColor(0.2f, 0.4f, 0.2f, 1f)
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)

        // Draw some decorative elements
        shapeRenderer.setColor(0.3f, 0.5f, 0.3f, 1f)
        for (i in 0..10) {
            for (j in 0..10) {
                val x = i * (WORLD_WIDTH / 10)
                val y = j * (WORLD_HEIGHT / 10)
                shapeRenderer.circle(x, y, 5f)
            }
        }
    }

    override fun dispose() {
        // Resources are managed by the game class
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
