package org.example.network

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.Input.Keys
import org.example.DifficultyLevel
import org.example.GameScreen
import org.example.MapType
import org.example.VampireSurvivorsGame
import org.example.WeaponType
import java.net.InetAddress

/**
 * Screen for network game setup.
 * Allows players to host a game, discover and join existing games,
 * and configure network settings.
 */
class NetworkScreen private constructor(
    private val game: VampireSurvivorsGame,
    private val font: BitmapFont,
    private val layout: GlyphLayout,
    private val shapeRenderer: ShapeRenderer
) : Screen {
    companion object {
        fun create(game: VampireSurvivorsGame): NetworkScreen {
            return NetworkScreen(
                game = game,
                font = game.getFont(),
                layout = game.getLayout(),
                shapeRenderer = game.getShapeRenderer()
            )
        }
    }

    // Network components
    private val networkManager = NetworkManager()

    // UI state
    private enum class ScreenState {
        MAIN_MENU,
        HOST_SETUP,
        JOIN_GAME,
        LOBBY
    }

    private var currentState = ScreenState.MAIN_MENU
    private var playerName = "Player"

    // Game settings
    private var selectedWeaponType = WeaponType.SIMPLE
    private var selectedMapType = MapType.FOREST
    private var selectedDifficulty = DifficultyLevel.NORMAL

    init {
        // Network manager is initialized automatically
    }

    override fun render(delta: Float) {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Render UI
        game.getBatch().begin()

        // Title
        font.getData().setScale(2f)
        val titleText = "Multiplayer Setup"
        layout.setText(font, titleText)
        font.setColor(Color.WHITE)
        font.draw(game.getBatch(), titleText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.8f)

        // Options
        font.getData().setScale(1.5f)

        val hostText = "1. Host a Game"
        layout.setText(font, hostText)
        font.draw(game.getBatch(), hostText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.6f)

        val joinText = "2. Join a Game"
        layout.setText(font, joinText)
        font.draw(game.getBatch(), joinText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.5f)

        val backText = "ESC. Back to Main Menu"
        layout.setText(font, backText)
        font.draw(game.getBatch(), backText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.3f)

        // Player name
        font.getData().setScale(1.2f)
        val nameText = "Your Name: $playerName"
        layout.setText(font, nameText)
        font.draw(game.getBatch(), nameText,
            (Gdx.graphics.width - layout.width) / 2,
            Gdx.graphics.height * 0.4f)

        font.getData().setScale(1f)

        game.getBatch().end()

        // Handle input
        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            // Return to main menu
            // In a real implementation, we would need to clean up network resources
            game.setScreen(org.example.MainMenuScreen.create(game))
            dispose()
        }
    }

    override fun resize(width: Int, height: Int) {
        // Nothing to do
    }

    override fun pause() {
        // Nothing to do
    }

    override fun resume() {
        // Nothing to do
    }

    override fun show() {
        // Nothing to do
    }

    override fun hide() {
        // Nothing to do
    }

    override fun dispose() {
        // Clean up network resources
        networkManager.disconnectFromServer()
    }
}
