package org.example

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType

class MainMenuScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer
) : Screen {
    private val titleText = "Vampire Survivors Clone"
    private val singlePlayerText = "1. Single Player"
    private val multiplayerText = "2. Multiplayer"
    private val instructionText = "Press 1 or 2 to select"

    // For testing and screen transitions
    internal var hubWorldScreenFactory: (VampireSurvivorsGame) -> HubWorldScreen = { game ->
        HubWorldScreen.create(game)
    }

    internal var networkScreenFactory: (VampireSurvivorsGame) -> org.example.network.NetworkScreen = { game ->
        org.example.network.NetworkScreen.create(game)
    }

    fun handleInput(key: Int) {
        when (key) {
            com.badlogic.gdx.Input.Keys.NUM_1 -> {
                // Single player mode
                game.setScreen(hubWorldScreenFactory(game))
                dispose()
            }
            com.badlogic.gdx.Input.Keys.NUM_2 -> {
                // Multiplayer mode
                game.setScreen(networkScreenFactory(game))
                dispose()
            }
            com.badlogic.gdx.Input.Keys.SPACE -> {
                // For backward compatibility, SPACE defaults to single player
                game.setScreen(hubWorldScreenFactory(game))
                dispose()
            }
        }
    }

    companion object {
        fun create(game: VampireSurvivorsGame): MainMenuScreen {
            return MainMenuScreen(
                game = game,
                font = BitmapFont(),
                layout = GlyphLayout(),
                shapeRenderer = ShapeRenderer()
            )
        }

        // Factory method for testing
        internal fun createForTesting(
            game: VampireSurvivorsGame,
            font: BitmapFont,
            layout: GlyphLayout,
            shapeRenderer: ShapeRenderer
        ): MainMenuScreen {
            return MainMenuScreen(game, font, layout, shapeRenderer)
        }
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Handle input
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_1)) {
            handleInput(com.badlogic.gdx.Input.Keys.NUM_1)
            return
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_2)) {
            handleInput(com.badlogic.gdx.Input.Keys.NUM_2)
            return
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            handleInput(com.badlogic.gdx.Input.Keys.SPACE)
            return
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
            handleInput(com.badlogic.gdx.Input.Keys.LEFT)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
            handleInput(com.badlogic.gdx.Input.Keys.RIGHT)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
            handleInput(com.badlogic.gdx.Input.Keys.UP)
        }
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            handleInput(com.badlogic.gdx.Input.Keys.DOWN)
        }

        // Draw title and play button
        game.getBatch().begin()

        // Draw title
        font.getData().setScale(2f)
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.7f)

        // Draw menu options
        font.getData().setScale(1.5f)

        // Single player option
        layout.setText(font, singlePlayerText)
        font.draw(game.getBatch(), singlePlayerText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        // Multiplayer option
        layout.setText(font, multiplayerText)
        font.draw(game.getBatch(), multiplayerText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        // Instructions
        font.getData().setScale(1.2f)
        layout.setText(font, instructionText)
        font.draw(game.getBatch(), instructionText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)

        font.getData().setScale(1f)  // Reset font scale
        game.getBatch().end()
    }

    override fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
}
